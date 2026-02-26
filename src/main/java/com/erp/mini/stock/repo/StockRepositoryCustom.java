package com.erp.mini.stock.repo;

import com.erp.mini.stock.domain.Stock;
import com.erp.mini.stock.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockRepositoryCustom {
    ItemInfoDto getItemInfo(Long itemId);
    Page<ItemStockInfoDto> getItemStockInfo(Long itemId, Pageable pageable);

    WarehouseInfoDto getWarehouseInfo(Long warehouseId);
    Page<WarehouseStockInfoDto> getWarehouseStockInfo(Long warehouseId, Pageable pageable);

    List<Stock> findAllByKeysForUpdate(List<StockKey> keys);
}
