package com.erp.mini.purchase.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.domain.PurchaseOrder;
import com.erp.mini.purchase.domain.PurchaseOrderLine;
import com.erp.mini.purchase.dto.*;
import com.erp.mini.purchase.repo.PurchaseOrderRepository;
import com.erp.mini.stock.dto.StockKey;
import com.erp.mini.stock.service.StockService;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final PartnerRepository partnerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    private final StockService stockService;

    // 구매 생성
    @Transactional
    public void createPurchase(PurchaseOrderRequest request) {
        Partner partner = partnerRepository.findById(request.partnerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 거래처가 존재하지 않습니다."));

        partner.validateSupplier();

        PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);

        Set<Long> itemIds = request.purchaseLines().stream()
                .map(PurchaseOrderRequest.PurchaseLine::itemId)
                .collect(Collectors.toSet());

        Set<Long> warehouseIds = request.purchaseLines().stream()
                .map(PurchaseOrderRequest.PurchaseLine::warehouseId)
                .collect(Collectors.toSet());

        List<Item> itemList = itemRepository.findAllById(itemIds);
        List<Warehouse> warehouseList = warehouseRepository.findAllById(warehouseIds);

        Map<Long, Item> itemMap = itemList.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        if (itemMap.size() != itemIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 상품이 포함되어 있습니다.");
        }

        Map<Long, Warehouse> warehouseMap = warehouseList.stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse));

        if (warehouseMap.size() != warehouseIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "조재하지 않는 창고가 포함되어 있습니다.");
        }

        for (PurchaseOrderRequest.PurchaseLine purchaseLine : request.purchaseLines()) {
            Item item = itemMap.get(purchaseLine.itemId());
            Warehouse warehouse = warehouseMap.get(purchaseLine.warehouseId());

            item.ensureAvailable();
            warehouse.ensureAvailable();

            purchaseOrder.addLine(item, warehouse, purchaseLine.qty(), purchaseLine.unitCost());
        }

        purchaseOrderRepository.save(purchaseOrder);
    }

    // 항목 추가
    @Transactional
    public void addOrderLine(Long purchaseOrderId, AddPurchaseOrderLineRequest request) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdWithLines(purchaseOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 구매건이 존재하지 않습니다."));

        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 상품이 존재하지 않습니다."));

        item.ensureAvailable();

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 창고가 존재하지 않습니다."));

        warehouse.ensureAvailable();

        purchaseOrder.addLine(
                item, warehouse, request.qty(), request.unitCost()
        );
    }

    // 항목 삭제
    @Transactional
    public void removePurchaseOrder(Long purchaseOrderId, Long purchaseOrderLineId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdWithLines(purchaseOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 구매건이 존재하지 않습니다."));

        purchaseOrder.removeLine(purchaseOrderLineId);
    }

    // 주문(상태 변경)
    @Transactional
    public void orderPurchase(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 구매건이 존재하지 않습니다."));

        purchaseOrder.markAsOrdered();
    }

    // 주문 취소(상태 변경)
    @Transactional
    public void cancelPurchase(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 구매건이 존재하지 않습니다."));

        purchaseOrder.cancel();
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public PurchaseDetailResponse getPurchaseOrderDetail(Long purchaseOrderId) {
        // 헤더 조회
        PurchaseHeaderDto header = purchaseOrderRepository.getPurchaseDetailHeader(purchaseOrderId);

        if (header == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "해당 구매건이 존재하지 않습니다.");
        }

        List<PurchaseLineDto> lines = purchaseOrderRepository.getPurchaseDetailLines(purchaseOrderId);

        return new PurchaseDetailResponse(header, lines);
    }

    // 입고 완료
    @Transactional
    public void receive(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdWithLines(purchaseOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 구매건이 존재하지 않습니다."));

        // (itemId, warehouseId), qty 조합으로 Map 생성
        Map<StockKey, Long> lineMap = purchaseOrder.getPurchaseOrderLines().stream()
                .collect(Collectors.toMap(
                        l -> new StockKey(l.getItem().getId(), l.getWarehouse().getId()),
                        PurchaseOrderLine::getQty,
                        Long::sum
                ));

        stockService.increase(lineMap, purchaseOrderId);

        purchaseOrder.markAsReceived();
    }
}