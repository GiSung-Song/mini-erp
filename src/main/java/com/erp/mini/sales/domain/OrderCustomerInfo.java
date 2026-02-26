package com.erp.mini.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCustomerInfo {

    @Column(length = 50, name = "customer_name", nullable = false)
    private String customerName;

    @Column(length = 30, name = "customer_phone", nullable = false)
    private String customerPhone;

    public OrderCustomerInfo(String customerName, String customerPhone) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }
}
