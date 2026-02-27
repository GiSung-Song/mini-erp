package com.erp.mini.seed;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.mini.common.security.CustomUserDetails;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemStatus;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.user.domain.User;
import com.erp.mini.user.repo.UserRepository;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.repo.WarehouseRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final PartnerRepository partnerRepository;
    private final EntityManager em;

    private static final String ADMIN_EMP_NO = "admin";

    @Transactional
    public void seed() {
        User admin = userRepository.findByEmployeeNumber(ADMIN_EMP_NO)
                .orElseGet(() -> userRepository.save(
                        User.createUser(
                                "관리자",
                                ADMIN_EMP_NO,
                                passwordEncoder.encode("admin1234"))));

        CustomUserDetails adminPrincipal = new CustomUserDetails(
                admin.getId(),
                admin.getEmployeeNumber(),
                admin.getPassword(),
                true);

        SecurityContextUtil.runAs(adminPrincipal, this::seedDomainData);
    }

    protected void seedDomainData() {
        seedBulkItems(10000);
        seedBulkWarehouses(100);
        seedBulkPartners(500, PartnerType.SUPPLIER, "SUP-");
        seedBulkPartners(500, PartnerType.CUSTOMER, "CUS-");
    }

    private void seedBulkItems(int count) {
        if (itemRepository.count() > 0)
            return;

        for (int i = 0; i < count; i++) {
            itemRepository.save(
                    Item.createItem(
                            "상품-" + i,
                            "ITEM-" + String.format("%06d", i),
                            BigDecimal.valueOf(1000 + i),
                            ItemStatus.ACTIVE));

            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
    }

    private void seedBulkWarehouses(int count) {
        if (warehouseRepository.count() > 0)
            return;

        for (int i = 0; i < count; i++) {
            Warehouse wh = warehouseRepository.save(Warehouse.createWarehouse(
                    "창고-" + i, "테스트 주소 " + i, WarehouseStatus.ACTIVE));
            wh.generateCode();
        }
    }

    private void seedBulkPartners(int count, PartnerType type, String prefix) {
        for (int i = 0; i < count; i++) {
            Partner partner = partnerRepository.save(Partner.createPartner(
                    type.name() + "-" + i,
                    type,
                    "010-0000-" + String.format("%04d", i),
                    "test" + i + "@example.com"));
            partner.generateCode();
        }
    }
}