package com.erp.mini.partner.domain;

import com.erp.mini.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "partners",
        uniqueConstraints = {@UniqueConstraint(name = "uq_partners_code", columnNames = {"code"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PartnerType type;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String email;

    private Partner(String name, String code, PartnerType type, String phone, String email) {
        this.name = name;
        this.code = code;
        this.type = type;
        this.phone = phone;
        this.email = email;
    }

    public static Partner createPartner(String name, String code, PartnerType type, String phone, String email) {
        return new Partner(name, code, type, phone, email);
    }
}
