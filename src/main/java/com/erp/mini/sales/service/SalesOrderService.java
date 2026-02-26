package com.erp.mini.sales.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.sales.domain.OrderCustomerInfo;
import com.erp.mini.sales.domain.SalesOrder;
import com.erp.mini.sales.domain.SalesOrderLine;
import com.erp.mini.sales.domain.ShippingAddress;
import com.erp.mini.sales.dto.*;
import com.erp.mini.sales.repo.SalesOrderRepository;
import com.erp.mini.stock.dto.StockKey;
import com.erp.mini.stock.service.StockService;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PartnerRepository partnerRepository;
    private final StockService stockService;

    // 출고 생성
    @Transactional
    public void createSale(SalesOrderRequest request) {
        Partner partner = partnerRepository.findById(request.partnerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 거래처가 존재하지 않습니다."));

        partner.validateCustomer();

        SalesOrder salesOrder = SalesOrder.createSalesOrder(
                partner,
                new OrderCustomerInfo(request.customerName(), request.customerPhone()),
                new ShippingAddress(request.zipcode(), request.address1(), request.address2())
        );

        Set<Long> itemIds = request.saleLines().stream()
                .map(SalesOrderRequest.SaleLine::itemId)
                .collect(Collectors.toSet());

        Set<Long> warehouseIds = request.saleLines().stream()
                .map(SalesOrderRequest.SaleLine::warehouseId)
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
            throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 창고가 포함되어 있습니다.");
        }

        for (SalesOrderRequest.SaleLine saleLine : request.saleLines()) {
            Item item = itemMap.get(saleLine.itemId());
            Warehouse warehouse = warehouseMap.get(saleLine.warehouseId());

            item.ensureAvailable();
            warehouse.ensureAvailable();

            salesOrder.addLine(item, warehouse, saleLine.qty(), saleLine.unitPrice());
        }

        salesOrderRepository.save(salesOrder);
    }

    // 출고 항목 추가
    @Transactional
    public void addOrderLine(Long salesOrderId, AddSalesOrderLineRequest request) {
        SalesOrder salesOrder = salesOrderRepository.findByIdWithLines(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 판매건이 존재하지 않습니다."));

        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 상품이 존재하지 않습니다."));

        item.ensureAvailable();

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 창고가 존재하지 않습니다."));

        warehouse.ensureAvailable();

        salesOrder.addLine(
                item, warehouse, request.qty(), request.unitPrice()
        );
    }

    // 출고 항목 삭제
    @Transactional
    public void removeSaleOrder(Long salesOrderId, Long salesOrderLineId) {
        SalesOrder salesOrder = salesOrderRepository.findByIdWithLines(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 판매건이 존재하지 않습니다."));

        salesOrder.removeLine(salesOrderLineId);
    }

    // 출고 주문(상태 변경)
    @Transactional
    public void orderSales(Long salesOrderId) {
        SalesOrder salesOrder = salesOrderRepository.findByIdWithLines(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 판매건이 존재하지 않습니다."));

        // 재고 확인 등 DB I/O 이전 상태 검증
        salesOrder.availableOrder();

        Map<StockKey, Long> lineMap = salesOrder.getSalesOrderLines().stream()
                .collect(Collectors.toMap(
                        l -> new StockKey(l.getItem().getId(), l.getWarehouse().getId()),
                        SalesOrderLine::getQty,
                        Long::sum
                ));

        stockService.decrease(lineMap, salesOrderId);

        // 상태 검증 및 상태 변경
        salesOrder.markAsOrdered();
    }

    // 출고 취소
    @Transactional
    public void cancelSales(Long salesOrderId) {
        SalesOrder salesOrder = salesOrderRepository.findByIdWithLines(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 판매건이 존재하지 않습니다."));

        if (salesOrder.isOrdered()) {
            Map<StockKey, Long> lineMap = salesOrder.getSalesOrderLines().stream()
                    .collect(Collectors.toMap(
                            l -> new StockKey(l.getItem().getId(), l.getWarehouse().getId()),
                            SalesOrderLine::getQty,
                            Long::sum
                    ));

            stockService.restore(lineMap, salesOrderId);
        }

        salesOrder.cancel();
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public SalesDetailResponse getSalesOrderDetail(Long salesOrderId) {
        // 헤더 조회
        SalesHeaderDto header = salesOrderRepository.getSalesDetailHeader(salesOrderId);

        if (header == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "해당 판매건이 존재하지 않습니다.");
        }

        List<SalesLineDto> lines = salesOrderRepository.getSalesDetailLines(salesOrderId);

        return new SalesDetailResponse(header, lines);
    }

    // 출고 완료
    @Transactional
    public void shipped(Long salesOrderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 판매건이 존재하지 않습니다."));

        salesOrder.markAsShipped();
    }
}
