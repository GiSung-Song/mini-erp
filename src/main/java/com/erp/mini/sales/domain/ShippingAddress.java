package com.erp.mini.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress {

    @Column(length = 20, name = "zipcode", nullable = false)
    private String zipcode;

    @Column(name = "address1", nullable = false)
    private String address1;

    @Column(name = "address2", nullable = false)
    private String address2;

    public ShippingAddress(String zipcode, String address1, String address2) {
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
    }
}
