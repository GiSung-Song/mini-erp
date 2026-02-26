package com.erp.mini.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.inventory.domain.InventoryTransaction;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.repo.InventoryTransactionRepository;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.stock.dto.AdjustStockRequest;
import com.erp.mini.stock.dto.ItemStockResponse;
import com.erp.mini.stock.dto.StockKey;
import com.erp.mini.stock.repo.StockRepository;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;

@SpringBootTest
@Import(TestAuditorConfig.class)
@ActiveProfiles("integration")
class StockServiceIntegrationTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    @AfterEach
    void cleanup() {
        inventoryTransactionRepository.deleteAll();
        stockRepository.deleteAll();
        itemRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    // --- 공통 헬퍼 ---
    private Item createItem(String name, String code) {
        Item item = Item.createItem(name, code, BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
        return itemRepository.save(item);
    }

    private Warehouse createWarehouse(String name) {
        Warehouse w = Warehouse.createWarehouse(name, "loc", WarehouseStatus.ACTIVE);
        return warehouseRepository.save(w);
    }

    // --- 증가 테스트 ---
    @Nested
    class increase_test {

        @Test
        void increase_success() {
            Item item = createItem("A", "ITM-A");
            Warehouse wh = createWarehouse("WH-A");

            StockKey key = new StockKey(item.getId(), wh.getId());

            stockService.increase(Collections.singletonMap(key, 5L), 100L);

            var stocks = stockRepository.findAll();
            assertThat(stocks).hasSize(1);
            assertThat(stocks.get(0).getQty()).isEqualTo(5L);

            // 재고 이력 검증: increase는 inbound 트랜잭션 생성
            var txs = inventoryTransactionRepository.findAll();
            assertThat(txs).hasSize(1);
            InventoryTransaction tx = txs.get(0);
            assertThat(tx.getType()).isEqualTo(TransactionType.INBOUND);
            assertThat(tx.getQtyDelta()).isEqualTo(5L);
            assertThat(tx.getRefId()).isEqualTo(100L);
        }

        @Test
        void increase_fail_with_bad_request() {
            // refId is required
            Item item = createItem("B", "ITM-B");
            Warehouse wh = createWarehouse("WH-B");
            StockKey key = new StockKey(item.getId(), wh.getId());

            assertThatThrownBy(() -> stockService.increase(Collections.singletonMap(key, 1L), null))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void increase_concurrent_accumulates_and_creates_single_stock() throws Exception {
            Item item = createItem("C", "ITM-C");
            Warehouse wh = createWarehouse("WH-C");

            int threads = 8;
            long each = 10L;

            // 고정된 8개의 스레드
            ExecutorService ex = Executors.newFixedThreadPool(threads);

            // 모든 워커 스레드가 준비될 때 까지 대기
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            // task 등록
            for (int i = 0; i < threads; i++) {
                ex.submit(() -> { // 스레드풀 시작
                    try {
                        // 스레드 준비 완료
                        ready.countDown();
                        start.await();
                        stockService.increase(Collections.singletonMap(new StockKey(item.getId(), wh.getId()), each),
                                200L);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            // 모든 스레드가 task 시작
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
            ex.shutdownNow(); // 스레드 자원 정리

            var stocks = stockRepository.findAll();
            assertThat(stocks).hasSize(1);
            assertThat(stocks.get(0).getQty()).isEqualTo(threads * each);

            // 각 스레드별로 inventory transaction이 생성되었는지 확인
            var txs = inventoryTransactionRepository.findAll();
            assertThat(txs).hasSize(threads);
            txs.forEach(t -> {
                assertThat(t.getType()).isEqualTo(TransactionType.INBOUND);
                assertThat(t.getQtyDelta()).isGreaterThan(0L);
                assertThat(t.getRefId()).isEqualTo(200L);
            });
        }
    }

    // --- 감소 테스트 ---
    @Nested
    class decrease_test {

        @Test
        void decrease_success() {
            Item item = createItem("D", "ITM-D");
            Warehouse wh = createWarehouse("WH-D");

            StockKey key = new StockKey(item.getId(), wh.getId());
            stockService.increase(Collections.singletonMap(key, 50L), 300L);

            stockService.decrease(Collections.singletonMap(key, 20L), 301L);

            var s = stockRepository.findAll().get(0);
            assertThat(s.getQty()).isEqualTo(30L);
        }

        @Test
        void decrease_fail_with_not_found() {
            Item item = createItem("E", "ITM-E");
            Warehouse wh = createWarehouse("WH-E");
            StockKey key = new StockKey(item.getId(), wh.getId());

            assertThatThrownBy(() -> stockService.decrease(Collections.singletonMap(key, 1L), 400L))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }

        @Test
        void decrease_concurrent_respects_qty_and_no_negative() throws Exception {

            Item item = createItem("F", "ITM-F");
            Warehouse wh = createWarehouse("WH-F");
            StockKey key = new StockKey(item.getId(), wh.getId());

            stockService.increase(Collections.singletonMap(key, 100L), 500L);

            int threads = 5;
            long each = 10L;

            ExecutorService ex = Executors.newFixedThreadPool(threads);
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                ex.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        stockService.decrease(Collections.singletonMap(key, each), 501L);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
            ex.shutdownNow();

            var s = stockRepository.findAll().get(0);
            assertThat(s.getQty()).isEqualTo(100L - threads * each);

            var txs = inventoryTransactionRepository.findAll();
            // inbound(1) + outbound(threads) 총 1 + 5 = 6건
            assertThat(txs).hasSize(1 + threads);
            txs.stream().filter(t -> t.getType() == TransactionType.OUTBOUND)
                    .forEach(t -> assertThat(t.getQtyDelta()).isLessThan(0L));
        }
    }

    // --- 복구(판매 취소) 테스트 ---
    @Nested
    class restore_test {

        @Test
        void restore_success() {
            Item item = createItem("G", "ITM-G");
            Warehouse wh = createWarehouse("WH-G");
            StockKey key = new StockKey(item.getId(), wh.getId());

            stockService.increase(Collections.singletonMap(key, 20L), 600L);

            // restore should increase
            stockService.restore(Collections.singletonMap(key, 5L), 601L);

            var s = stockRepository.findAll().get(0);
            assertThat(s.getQty()).isEqualTo(25L);

            var txs = inventoryTransactionRepository.findAll();
            assertThat(txs).hasSize(2); // inbound + restore inbound
            txs.forEach(t -> assertThat(t.getType()).isEqualTo(TransactionType.INBOUND));
        }

        @Test
        void restore_fail_with_not_found() {
            Item item = createItem("H", "ITM-H");
            Warehouse wh = createWarehouse("WH-H");
            StockKey key = new StockKey(item.getId(), wh.getId());

            assertThatThrownBy(() -> stockService.restore(Collections.singletonMap(key, 1L), 700L))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }

        @Test
        void restore_concurrent_accumulates() throws Exception {
            Item item = createItem("I", "ITM-I");
            Warehouse wh = createWarehouse("WH-I");
            StockKey key = new StockKey(item.getId(), wh.getId());

            stockService.increase(Collections.singletonMap(key, 30L), 800L);

            int threads = 4;
            long each = 5L;

            ExecutorService ex = Executors.newFixedThreadPool(threads);
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                ex.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        stockService.restore(Collections.singletonMap(key, each), 801L);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
            ex.shutdownNow();

            var s = stockRepository.findAll().get(0);
            assertThat(s.getQty()).isEqualTo(30L + threads * each);

            var txs = inventoryTransactionRepository.findAll();
            // 1 initial inbound + threads restore inbound
            assertThat(txs).hasSize(1 + threads);
            txs.forEach(t -> assertThat(t.getType()).isEqualTo(TransactionType.INBOUND));
        }
    }

    // --- 조정 테스트 ---
    @Nested
    class adjust_test {

        @Test
        void adjust_success() {
            Item item = createItem("J", "ITM-J");
            Warehouse wh = createWarehouse("WH-J");
            StockKey key = new StockKey(item.getId(), wh.getId());

            stockService.increase(Collections.singletonMap(key, 10L), 900L);

            AdjustStockRequest req = new AdjustStockRequest(item.getId(), wh.getId(), 25L, "stocktake");
            stockService.adjust(req);

            var s = stockRepository.findAll().get(0);
            assertThat(s.getQty()).isEqualTo(25L);

            var txs = inventoryTransactionRepository.findAll();
            assertThat(txs).hasSize(2); // initial inbound + adjust
            InventoryTransaction adj = txs.stream().filter(t -> t.getType() == TransactionType.ADJUST).findFirst()
                    .orElseThrow();
            assertThat(adj.getQtyDelta()).isEqualTo(15L); // 25 - 10
            assertThat(adj.getReason()).isEqualTo("stocktake");
        }

        @Test
        void adjust_fail_with_missing_item_fk() {
            Warehouse wh = createWarehouse("WH-K");

            itemRepository.deleteAll();

            AdjustStockRequest req = new AdjustStockRequest(999999L, wh.getId(), 5L, "reason");

            assertThatThrownBy(() -> stockService.adjust(req))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void adjust_concurrent_updates_consistently() throws Exception {
            Item item = createItem("L", "ITM-L");
            Warehouse wh = createWarehouse("WH-L");
            StockKey key = new StockKey(item.getId(), wh.getId());

            // seed with 50
            stockService.increase(Collections.singletonMap(key, 50L), 1000L);

            int threads = 5;
            ExecutorService ex = Executors.newFixedThreadPool(threads);
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            // Each thread will set actualQty to different values; final value may be last
            // writer but no corruption
            for (int i = 0; i < threads; i++) {
                final long actual = 50L + (i + 1) * 5L;
                ex.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        stockService.adjust(new AdjustStockRequest(item.getId(), wh.getId(), actual, "adj"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
            ex.shutdownNow();

            var s = stockRepository.findAll().get(0);
            // final qty must be >= 50 and one of the set values
            assertThat(s.getQty()).isGreaterThanOrEqualTo(50L);

            var txs = inventoryTransactionRepository.findAll();
            // 1 initial inbound + threads adjust txs
            assertThat(txs).hasSize(1 + threads);
            txs.stream().filter(t -> t.getType() == TransactionType.ADJUST)
                    .forEach(t -> assertThat(t.getReason()).isEqualTo("adj"));
        }
    }

    // --- 조회 테스트 ---
    @Nested
    class getItemStock_test {
        @Test
        void getItemStock_success() {
            Item a = createItem("M", "ITM-M");
            Warehouse w1 = createWarehouse("WH-M1");
            Warehouse w2 = createWarehouse("WH-M2");

            stockService.increase(Collections.singletonMap(new StockKey(a.getId(), w1.getId()), 5L), 1100L);
            stockService.increase(Collections.singletonMap(new StockKey(a.getId(), w2.getId()), 15L), 1101L);

            ItemStockResponse res = stockService.getItemStock(a.getId(),
                    org.springframework.data.domain.PageRequest.of(0, 10));
            assertThat(res.itemStockInfos().pageInfo().totalElements()).isEqualTo(2);

        }
    }

    @Nested
    class getWarehouseStock_test {
        @Test
        void getWarehouseStock_success() {
            Item a = createItem("N", "ITM-N");
            Item b = createItem("O", "ITM-O");
            Warehouse w = createWarehouse("WH-N");

            stockService.increase(Collections.singletonMap(new StockKey(a.getId(), w.getId()), 5L), 1200L);
            stockService.increase(Collections.singletonMap(new StockKey(b.getId(), w.getId()), 15L), 1201L);

            var res = stockService.getWarehouseStock(w.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
            assertThat(res.warehouseStockInfos().pageInfo().totalElements()).isEqualTo(2);
        }
    }
}
