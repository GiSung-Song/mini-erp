package com.erp.mini.warehouse.repo;

import com.erp.mini.warehouse.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long>, WarehouseRepositoryCustom {
    boolean existsByCode(String code);
    Optional<Warehouse> findByCode(String code);
}
