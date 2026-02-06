package com.erp.mini.warehouse.domain;

import com.erp.mini.common.entity.BaseEntity;
import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "warehouses",
        uniqueConstraints = {@UniqueConstraint(name = "uq_warehouses_code", columnNames = {"code"})},
        indexes = {@Index(name = "idx_warehouses_name", columnList = "name"),
                @Index(name = "idx_warehouses_location", columnList = "location")}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private WarehouseStatus status;

    private Warehouse(String name, String location, WarehouseStatus status) {
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public static Warehouse createWarehouse(String name, String location, WarehouseStatus status) {
        return new Warehouse(name, location, status);
    }

    public void generateCode() {
        if (this.id == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "식별자 ID가 생성되지 않았습니다.");
        }

        this.code = "WH" + String.format("%06d", id);
    }

    public void activateWarehouse() {
        if (this.status == WarehouseStatus.INACTIVE) {
            this.status = WarehouseStatus.ACTIVE;
        }
    }

    public void deactivateWarehouse() {
        if (this.status == WarehouseStatus.ACTIVE) {
            this.status = WarehouseStatus.INACTIVE;
        }
    }
}
