package com.erp.mini.item.domain;

import com.erp.mini.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "items",
        uniqueConstraints = {@UniqueConstraint(name = "uq_items_code", columnNames = {"code"})},
        indexes = @Index(name = "idx_items_name", columnList = "name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, name = "base_price")
    private BigDecimal basePrice;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private Item(String name, String code, BigDecimal basePrice, ItemStatus status) {
        this.name = name;
        this.code = code;
        this.basePrice = basePrice;
        this.status = status;
    }

    public static Item createItem(String name, String code, BigDecimal basePrice, ItemStatus status) {
        return new Item(name, code, basePrice, status);
    }

    public void deactivate() {
        this.status = ItemStatus.INACTIVE;
    }

    public void changePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}