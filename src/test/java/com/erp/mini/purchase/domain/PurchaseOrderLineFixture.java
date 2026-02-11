package com.erp.mini.purchase.domain;

import com.erp.mini.item.domain.Item;
import com.erp.mini.warehouse.domain.Warehouse;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class PurchaseOrderLineFixture {

    private static long sequence = 1L;

    public static PurchaseOrderLine create(
            PurchaseOrder order,
            Item item,
            Warehouse warehouse,
            long qty,
            BigDecimal unitCost
    ) {
        PurchaseOrderLine line =
                PurchaseOrderLine.createPurchaseOrderLine(order, item, warehouse, qty, unitCost);

        ReflectionTestUtils.setField(line, "id", sequence++);
        return line;
    }
}