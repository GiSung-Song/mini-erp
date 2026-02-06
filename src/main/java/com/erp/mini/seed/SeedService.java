package com.erp.mini.seed;

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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final PartnerRepository partnerRepository;

    private static final String ADMIN_EMP_NO = "admin";
    private static final String ITEM_CODE = "item-code";
    private static final String WH_CODE = "warehouse-code";
    private static final String SUPPLIER_CODE = "supplier-code";
    private static final String CUSTOMER_CODE = "customer-code";

    @Transactional
    public void seed() {
        User admin = userRepository.findByEmployeeNumber(ADMIN_EMP_NO)
                .orElseGet(() -> userRepository.save(
                        User.createUser(
                                "관리자",
                                ADMIN_EMP_NO,
                                passwordEncoder.encode("admin1234")
                        )
                ));

        CustomUserDetails adminPrincipal = new CustomUserDetails(
                admin.getId(),
                admin.getEmployeeNumber(),
                admin.getPassword(),
                true
        );

        SecurityContextUtil.runAs(adminPrincipal, this::seedDomainData);
    }

    protected void seedDomainData() {
        seedTestItem();
        seedTestWarehouse();
        seedTestSupplier();
        seedTestCustomer();
    }

    private void seedTestItem() {
        if (itemRepository.existsByCode(ITEM_CODE)) return;

        itemRepository.save(
                Item.createItem(
                        "테스트 아이템", ITEM_CODE,
                        BigDecimal.valueOf(1000), ItemStatus.ACTIVE
                )
        );
    }

    private void seedTestWarehouse() {
        if (warehouseRepository.existsByCode(WH_CODE)) return;

        Warehouse warehouse = warehouseRepository.save(
                Warehouse.createWarehouse(
                        "테스트 1창고",
                        "테스트시 테스트구 테스트동 테스트지역 12-34",
                        WarehouseStatus.ACTIVE
                )
        );

        warehouse.generateCode();
    }

    private void seedTestSupplier() {
        if (partnerRepository.existsByCode(SUPPLIER_CODE)) return;
        Partner supplier = partnerRepository.save(
                Partner.createPartner(
                        "테스트 공급사", PartnerType.SUPPLIER,
                        "01012344321", "test@supplier.com"
                )
        );

        supplier.generateCode();
    }

    private void seedTestCustomer() {
        if (partnerRepository.existsByCode(CUSTOMER_CODE)) return;
        Partner customer = partnerRepository.save(
                Partner.createPartner(
                        "테스트 고객사", PartnerType.CUSTOMER,
                        "01043211234", "test@customer.com"
                )
        );

        customer.generateCode();
    }
}