package com.erp.mini.warehouse.domain;

import com.erp.mini.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "warehouses",
        uniqueConstraints = {@UniqueConstraint(name = "uq_warehouses_code", columnNames = {"code"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private WarehouseStatus status;

    private Warehouse(String name, String code, String location) {
        this.name = name;
        this.code = code;
        this.location = location;
        this.status = WarehouseStatus.ACTIVE;
    }

    public static Warehouse createWarehouse(String name, String code, String location) {
        return new Warehouse(name, code, location);
    }
}
