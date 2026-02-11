package com.erp.mini.purchase.domain;

import com.erp.mini.item.domain.Item;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.repo.PurchaseOrderRepository;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("integration")
public class PurchaseOrderTestDataFactory {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    public PurchaseOrder createPurchaseOrder() {
        Item item = itemRepository.findById(1L)
                .orElseThrow();

        Warehouse warehouse = warehouseRepository.findById(1L)
                .orElseThrow();

        Partner partner = partnerRepository.findById(1L)
                .orElseThrow();

        PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
        purchaseOrder.addLine(item, warehouse, 10, BigDecimal.valueOf(1500));

        return purchaseOrderRepository.save(purchaseOrder);
    }
}
