package com.erp.mini.partner.domain;

import com.erp.mini.common.entity.BaseEntity;
import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "partners",
        uniqueConstraints = {@UniqueConstraint(name = "uq_partners_code", columnNames = {"code"})},
        indexes = @Index(name = "idx_partners_name", columnList = "name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PartnerType type;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String email;

    private Partner(String name, PartnerType type, String phone, String email) {
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.email = email;
    }

    public static Partner createPartner(String name, PartnerType type, String phone, String email) {
        return new Partner(name, type, phone, email);
    }

    public void generateCode() {
        if (this.id == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "식별자 ID가 생성되지 않았습니다.");
        }

        String suffix = String.format("%06d", id);

        switch (this.type) {
            case CUSTOMER -> this.code = "CUS" + suffix;
            case SUPPLIER -> this.code = "SUP" + suffix;
            default -> throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "거래처 타입이 올바르지 않습니다.");
        }
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void validateSupplier() {
        if (this.type != PartnerType.SUPPLIER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "해당 거래처는 고객사입니다.");
        }
    }

    public void validateCustomer() {
        if (this.type != PartnerType.CUSTOMER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "해당 거래처는 공급처입니다.");
        }
    }
}
