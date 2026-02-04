package com.erp.mini.item.domain;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class ItemFixture {

    private static long sequence = 1L;

    public static Item create() {
        return create("테스트 상품", "IC000001", BigDecimal.valueOf(15000.00), ItemStatus.ACTIVE);
    }

    public static Item create(String name, String code, BigDecimal basePrice, ItemStatus status) {
        Item item = Item.createItem(name, code, basePrice, status);
        ReflectionTestUtils.setField(item, "id", sequence++);
        return item;
    }
}