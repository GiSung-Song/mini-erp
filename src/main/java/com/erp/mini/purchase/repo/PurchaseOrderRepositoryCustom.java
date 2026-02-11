package com.erp.mini.purchase.repo;

import com.erp.mini.purchase.dto.PurchaseHeaderDto;
import com.erp.mini.purchase.dto.PurchaseLineDto;

import java.util.List;

public interface PurchaseOrderRepositoryCustom {
    PurchaseHeaderDto getPurchaseDetailHeader(Long purchaseOrderId);
    List<PurchaseLineDto> getPurchaseDetailLines(Long purchaseOrderId);
}
