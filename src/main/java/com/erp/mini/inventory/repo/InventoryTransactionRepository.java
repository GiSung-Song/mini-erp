package com.erp.mini.inventory.repo;

import com.erp.mini.inventory.domain.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long>, InventoryTransactionRepositoryCustom {
}