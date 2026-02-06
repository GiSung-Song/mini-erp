package com.erp.mini.warehouse.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class WarehouseFixture {

    private static long sequence = 1L;

    public static Warehouse create() {
        return create("서울 1창고", "WH000001", "서울시 부산구 울산동", WarehouseStatus.ACTIVE);
    }

    public static Warehouse create(String name, String code, String location, WarehouseStatus status) {
        Warehouse warehouse = Warehouse.createWarehouse(name, location, status);
        ReflectionTestUtils.setField(warehouse, "id", sequence++);
        ReflectionTestUtils.setField(warehouse, "code", code);
        return warehouse;
    }
}