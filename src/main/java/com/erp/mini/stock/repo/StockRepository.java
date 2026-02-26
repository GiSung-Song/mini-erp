package com.erp.mini.stock.repo;

import com.erp.mini.stock.domain.Stock;
import com.erp.mini.stock.dto.StockKey;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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