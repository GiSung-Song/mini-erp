package com.erp.mini.partner.domain;

import org.springframework.test.util.ReflectionTestUtils;

public class PartnerFixture {

    private static long sequence = 1L;

    public static Partner create() {
        return create("기성식품", "CUS-0000001", PartnerType.CUSTOMER, null, null);
    }

    public static Partner create(String name, String code, PartnerType type, String phone, String email) {
        Partner partner = Partner.createPartner(name, type, phone, email);
        ReflectionTestUtils.setField(partner, "id", sequence++);
        ReflectionTestUtils.setField(partner, "code", code);
        return partner;
    }
}