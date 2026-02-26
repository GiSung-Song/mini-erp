package com.erp.mini.sales.repo;

import com.erp.mini.sales.dto.SalesHeaderDto;
import com.erp.mini.sales.dto.SalesLineDto;

import java.util.List;

public interface SalesOrderRepositoryCustom {
    SalesHeaderDto getSalesDetailHeader(Long salesOrderId);
    List<SalesLineDto> getSalesDetailLines(Long salesOrderId);
}
