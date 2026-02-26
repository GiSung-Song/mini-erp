package com.erp.mini.sales.repo;

import com.erp.mini.sales.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, SalesOrderRepositoryCustom {
    @Query("""
        select so from SalesOrder so
        left join fetch so.salesOrderLines sol
        where so.id = :id
    """)
    Optional<SalesOrder> findByIdWithLines(Long id);
}