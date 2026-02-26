package com.erp.mini.purchase.repo;

import com.erp.mini.purchase.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, PurchaseOrderRepositoryCustom {
    @Query("""
        select po from PurchaseOrder po
        join fetch po.purchaseOrderLines pol
        where po.id = :id
    """)
    Optional<PurchaseOrder> findByIdWithLines(Long id);
}