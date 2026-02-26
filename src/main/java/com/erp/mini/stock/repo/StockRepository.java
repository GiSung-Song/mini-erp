package com.erp.mini.stock.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.erp.mini.stock.domain.Stock;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;

public interface StockRepository extends JpaRepository<Stock, Long>, StockRepositoryCustom {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s
        from Stock s
        where s.item.id = :itemId
        and s.warehouse.id = :warehouseId
    """)
    Optional<Stock> findByItemAndWarehouseForUpdate(
            @Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId
    );
}