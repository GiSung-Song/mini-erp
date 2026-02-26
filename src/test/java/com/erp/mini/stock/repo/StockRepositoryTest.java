package com.erp.mini.stock.repo;

import com.erp.mini.common.config.JpaConfig;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.stock.domain.Stock;
import com.erp.mini.stock.dto.*;
import com.erp.mini.util.TestAuditorConfig;
import com.erp.mini.util.TestContainerManager;
import com.erp.mini.util.TestQuerydslConfig;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({StockRepositoryImpl.class, JpaConfig.class, TestAuditorConfig.class, TestQuerydslConfig.class})
@ActiveProfiles("integration")
class StockRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StockRepository stockRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestContainerManager.registerMySQL(registry);
    }

    private Item saveItem(String name, String code) {
        Item item = Item.createItem(name, code, BigDecimal.valueOf(15000), ItemStatus.ACTIVE);
        em.persist(item);
        return item;
    }

    private Warehouse saveWarehouse(String name, String location) {
        Warehouse warehouse = Warehouse.createWarehouse(name, location, WarehouseStatus.ACTIVE);
        em.persist(warehouse);
        return warehouse;
    }

    private Stock saveStock(Item item, Warehouse warehouse, long qty) {
        Stock stock = Stock.createStock(item, warehouse);
        stock.increase(qty);
        em.persist(stock);
        return stock;
    }

    @Test
    void getItemInfo_test() {
        Item item = saveItem("설탕", "IC000001");

        em.flush();
        em.clear();

        ItemInfoDto result = stockRepository.getItemInfo(item.getId());

        assertThat(result).isNotNull();
        assertThat(result.itemId()).isEqualTo(item.getId());
        assertThat(result.itemName()).isEqualTo(item.getName());
        assertThat(result.itemCode()).isEqualTo(item.getCode());
    }

    @Test
    void getItemStockInfo_test() {
        Item item = saveItem("설탕", "IC000001");
        Warehouse seoul = saveWarehouse("서울 창고", "서울시 어딘가");
        Warehouse busan = saveWarehouse("부산 창고", "부산시 어딘가");

        saveStock(item, seoul, 10);
        saveStock(item, busan, 30);

        em.flush();
        em.clear();

        Page<ItemStockInfoDto> result
                = stockRepository.getItemStockInfo(item.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void getWarehouseInfo_test() {
        Warehouse warehouse = saveWarehouse("서울 창고", "서울시 어딘가");

        em.flush();
        em.clear();

        WarehouseInfoDto warehouseInfo = stockRepository.getWarehouseInfo(warehouse.getId());

        assertThat(warehouseInfo).isNotNull();
        assertThat(warehouseInfo.warehouseCode()).isEqualTo(warehouse.getCode());
        assertThat(warehouseInfo.warehouseName()).isEqualTo(warehouse.getName());
    }

    @Test
    void getWarehouseStockInfo_test() {
        Item sugar = saveItem("설탕", "IC000001");
        Item salt = saveItem("소금", "IC000002");
        Warehouse warehouse = saveWarehouse("서울 창고", "서울시 어딘가");

        saveStock(sugar, warehouse, 10);
        saveStock(salt, warehouse, 30);

        em.flush();
        em.clear();

        Page<WarehouseStockInfoDto> result
                = stockRepository.getWarehouseStockInfo(warehouse.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findAllByKeysForUpdate_test() {
        Item sugar = saveItem("설탕", "IC000001");
        Item salt = saveItem("소금", "IC000002");
        Warehouse warehouse = saveWarehouse("서울 창고", "서울시 어딘가");

        saveStock(sugar, warehouse, 10);
        saveStock(salt, warehouse, 20);

        em.flush();
        em.clear();

        List<StockKey> keys = List.of(
                new StockKey(sugar.getId(), warehouse.getId()),
                new StockKey(salt.getId(), warehouse.getId())
        );

        List<Stock> result = stockRepository.findAllByKeysForUpdate(keys);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    void findByItemAndWarehouseForUpdate_test() {
        Item salt = saveItem("소금", "IC000002");
        Warehouse warehouse = saveWarehouse("서울 창고", "서울시 어딘가");

        saveStock(salt, warehouse, 10);

        em.flush();
        em.clear();

        Optional<Stock> result
                = stockRepository.findByItemAndWarehouseForUpdate(salt.getId(), warehouse.getId());

        assertThat(result).isNotNull();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getQty()).isEqualTo(10);
    }
}