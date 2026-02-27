package com.erp.mini.stock.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.inventory.domain.InventoryTransaction;
import com.erp.mini.inventory.domain.TransactionType;
import com.erp.mini.inventory.repo.InventoryTransactionRepository;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.stock.domain.Stock;
import com.erp.mini.stock.dto.*;
import com.erp.mini.stock.repo.StockRepository;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Retryable(
            retryFor = {
                    DuplicateKeyException.class, // UNIQUE KEY 충돌 예외 발생 시 재시도(새로운 Stock 생성)
                    PessimisticLockingFailureException.class, // 데드락 + 락 충돌 예외
                    CannotAcquireLockException.class // 락 획득 실패 예외
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, random = true, multiplier = 2)
    )
    @Transactional
    public void increase(Map<StockKey, Long> lineMap, Long purchaseOrderId) {
        apply(lineMap, purchaseOrderId, TransactionType.INBOUND, true);
    }

    @Retryable(
            retryFor = {
                    PessimisticLockingFailureException.class, // 데드락 + 락 충돌 예외
                    CannotAcquireLockException.class // 락 획득 실패 예외
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, random = true, multiplier = 2)
    )
    @Transactional
    public void decrease(Map<StockKey, Long> lineMap, Long salesOrderId) {
        apply(lineMap, salesOrderId, TransactionType.OUTBOUND, false);
    }

    @Retryable(
            retryFor = {
                    PessimisticLockingFailureException.class, // 데드락 + 락 충돌 예외
                    CannotAcquireLockException.class // 락 획득 실패 예외
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, random = true, multiplier = 2)
    )
    @Transactional
    public void restore(Map<StockKey, Long> lineMap, Long salesOrderId) {
        apply(lineMap, salesOrderId, TransactionType.INBOUND, false);
    }

    @Retryable(
            retryFor = {
                    DuplicateKeyException.class, // UNIQUE KEY 충돌 예외 발생 시 재시도(새로운 Stock 생성)
                    PessimisticLockingFailureException.class, // 데드락 + 락 충돌 예외
                    CannotAcquireLockException.class // 락 획득 실패 예외
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 50)
    )
    @Transactional
    public void adjust(AdjustStockRequest request) {
        Stock stock = stockRepository.findByItemAndWarehouseForUpdate(request.itemId(), request.warehouseId())
                .orElse(null);

        // 실제로 필요한건 FK이기 때문에 실제 객체를 가져오지 않기 위해 reference 사용
        // 그대신 retry 재시도 전략을 세분화하여 실제 객체가 없으면 재시도 하지 않음
        Item referenceItem = itemRepository.getReferenceById(request.itemId());
        Warehouse referenceWarehouse = warehouseRepository.getReferenceById(request.warehouseId());

        if (stock == null) {
            stock = Stock.createStock(referenceItem, referenceWarehouse);
            stockRepository.saveAndFlush(stock);
        }

        long deltaQty = request.actualQty() - stock.getQty();

        InventoryTransaction tx = InventoryTransaction.adjust(referenceItem, referenceWarehouse, deltaQty, request.reason());

        stock.adjust(deltaQty);

        inventoryTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public ItemStockResponse getItemStock(Long itemId, Pageable pageable) {
        ItemInfoDto itemInfo = stockRepository.getItemInfo(itemId);
        Page<ItemStockInfoDto> stocks = stockRepository.getItemStockInfo(itemId, pageable);
        PageResponse<ItemStockInfoDto> pages = PageResponse.from(stocks);

        return new ItemStockResponse(itemInfo, pages);
    }

    @Transactional(readOnly = true)
    public WarehouseStockResponse getWarehouseStock(Long warehouseId, Pageable pageable) {
        WarehouseInfoDto warehouseInfo = stockRepository.getWarehouseInfo(warehouseId);
        Page<WarehouseStockInfoDto> stocks = stockRepository.getWarehouseStockInfo(warehouseId, pageable);
        PageResponse<WarehouseStockInfoDto> pages = PageResponse.from(stocks);

        return new WarehouseStockResponse(warehouseInfo, pages);
    }

    private void apply(Map<StockKey, Long> lineMap, Long refId, TransactionType type, boolean allowCreateStock) {
        if (refId == null || lineMap == null || lineMap.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "해당 거래건이 올바르지 않습니다.");
        }

        // 쿼리용 key pair로 변경
        List<StockKey> sortedKeys = getSortedKeys(lineMap);

        // 기존 재고 조회 및 락
        List<Stock> stocks = stockRepository.findAllByKeysForUpdate(sortedKeys);

        // 재고 Map 생성
        Map<StockKey, Stock> stockMap = stocks.stream()
                .collect(Collectors.toMap(
                        s -> new StockKey(s.getItem().getId(), s.getWarehouse().getId()),
                        Function.identity()
                ));

        if (!allowCreateStock && stockMap.size() != lineMap.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "재고가 존재하지 않습니다.");
        }

        if (allowCreateStock) {
            Set<Stock> createStock = new HashSet<>();

            for (StockKey key : lineMap.keySet()) {
                if (!stockMap.containsKey(key)) {
                    // FK만 알고 있으면 되기 때문에 getReferenceById 사용 (프록시 객체, 실제 다른 필드 알 필요X)
                    Item referenceItem = itemRepository.getReferenceById(key.getItemId());
                    Warehouse referenceWarehouse = warehouseRepository.getReferenceById(key.getWarehouseId());

                    Stock newStock = Stock.createStock(referenceItem, referenceWarehouse);

                    createStock.add(newStock);
                    stockMap.put(key, newStock);
                }
            }

            if (!createStock.isEmpty()) {
                stockRepository.saveAll(createStock);
                stockRepository.flush(); // 여기서 충돌 감지하여 빠른 retry 가능
            }
        }

        // 재고 이력 저장할 List
        List<InventoryTransaction> inventoryTransactions = new ArrayList<>();

        // save 이후라 dirty checking으로 update
        for (var entry : lineMap.entrySet()) {
            Stock stock = stockMap.get(entry.getKey());
            Long qty = entry.getValue();

            InventoryTransaction tx;

            switch (type) {
                case INBOUND -> {
                    stock.increase(qty);

                    if (allowCreateStock) {
                        tx = InventoryTransaction.purchaseInbound(
                                stock.getItem(),
                                stock.getWarehouse(),
                                qty,
                                refId
                        );
                    } else {
                        tx = InventoryTransaction.cancelSalesInbound(
                                stock.getItem(),
                                stock.getWarehouse(),
                                qty,
                                refId
                        );
                    }
                }
                case OUTBOUND -> {
                    stock.decrease(qty);
                    tx = InventoryTransaction.salesOutbound(
                            stock.getItem(),
                            stock.getWarehouse(),
                            qty,
                            refId
                    );
                }
                default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "잘못된 거래 타입입니다.");
            }

            inventoryTransactions.add(tx);
        }

        inventoryTransactionRepository.saveAll(inventoryTransactions);
    }

    private List<StockKey> getSortedKeys(Map<StockKey, Long> lineMap) {
        // key 정렬 (데드락 1차 방지)
        return lineMap.keySet().stream()
                .sorted(Comparator
                        .comparing(StockKey::getItemId)
                        .thenComparing(StockKey::getWarehouseId))
                .toList();
    }
}