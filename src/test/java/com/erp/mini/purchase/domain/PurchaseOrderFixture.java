package com.erp.mini.purchase.domain;

import com.erp.mini.partner.domain.Partner;
import org.springframework.test.util.ReflectionTestUtils;

public class PurchaseOrderFixture {

    private static long sequence = 1L;

    public static PurchaseOrder create(Partner partner) {
        PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
        ReflectionTestUtils.setField(purchaseOrder, "id", sequence++);

        return purchaseOrder;
    }

    public static PurchaseOrder create(Partner partner, PurchaseStatus purchaseStatus) {
        PurchaseOrder purchaseOrder = create(partner);
        ReflectionTestUtils.setField(purchaseOrder, "status", purchaseStatus);

        return purchaseOrder;
    }
}