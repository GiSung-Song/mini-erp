package com.erp.mini.purchase.domain;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.repo.PurchaseOrderRepository;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;

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
                Partner partner = Partner.createPartner("SUP", PartnerType.SUPPLIER, "010-0000-0000",
                                "sup@company.com");
                partner = partnerRepository.save(partner);

                Item item = Item.createItem("item", "CODE", BigDecimal.valueOf(1000), ItemStatus.ACTIVE);
                item = itemRepository.save(item);

                Warehouse warehouse = Warehouse.createWarehouse("warehouse", "loc", WarehouseStatus.ACTIVE);
                warehouse = warehouseRepository.save(warehouse);

                PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
                purchaseOrder.addLine(item, warehouse, 10, BigDecimal.valueOf(1500));

                return purchaseOrderRepository.save(purchaseOrder);
        }
}
