package com.erp.mini.warehouse.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.PageResponse;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseFixture;
import com.erp.mini.warehouse.domain.WarehouseStatus;
import com.erp.mini.warehouse.dto.SearchWarehouseCondition;
import com.erp.mini.warehouse.dto.SearchWarehouseResponse;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Nested
    class deactivate_warehouse_test {
        @Test
        void deactivate_warehouse_success() {
            Warehouse warehouse = WarehouseFixture.create();

            given(warehouseRepository.findById(warehouse.getId())).willReturn(Optional.of(warehouse));

            assertThat(warehouse.getStatus()).isEqualTo(WarehouseStatus.ACTIVE);

            warehouseService.deactivateWarehouse(warehouse.getId());

            assertThat(warehouse.getStatus()).isEqualTo(WarehouseStatus.INACTIVE);
        }

        @Test
        void deactivate_warehouse_fail_with_not_found() {
            Warehouse warehouse = WarehouseFixture.create();
            given(warehouseRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.deactivateWarehouse(warehouse.getId()))
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class activate_warehouse_test {
        @Test
        void activate_warehouse_success() {
            Warehouse warehouse = WarehouseFixture.create(
                    "테스트 창고1", "WH010101", "서울시 울산구 부산동", WarehouseStatus.INACTIVE
            );

            given(warehouseRepository.findById(warehouse.getId())).willReturn(Optional.of(warehouse));

            assertThat(warehouse.getStatus()).isEqualTo(WarehouseStatus.INACTIVE);

            warehouseService.activateWarehouse(warehouse.getId());

            assertThat(warehouse.getStatus()).isEqualTo(WarehouseStatus.ACTIVE);
        }

        @Test
        void activate_warehouse_fail_with_not_found() {
            Warehouse warehouse = WarehouseFixture.create(
                    "테스트 창고1", "WH010101", "서울시 울산구 부산동", WarehouseStatus.INACTIVE
            );
            given(warehouseRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> warehouseService.activateWarehouse(warehouse.getId()))
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class search_warehouse_test {
        @Test
        void search_warehouse_success() {
            Pageable pageable = PageRequest.of(0, 10);

            List<SearchWarehouseResponse> contents = List.of(
                    new SearchWarehouseResponse(1L, "창고1", "WH000001", "어딘가1", WarehouseStatus.ACTIVE),
                    new SearchWarehouseResponse(2L, "창고2", "WH000002", "어딘가2", WarehouseStatus.ACTIVE),
                    new SearchWarehouseResponse(3L, "창고3", "WH000003", "어딘가3", WarehouseStatus.ACTIVE),
                    new SearchWarehouseResponse(4L, "창고4", "WH000004", "어딘가4", WarehouseStatus.INACTIVE),
                    new SearchWarehouseResponse(5L, "창고5", "WH000005", "어딘가5", WarehouseStatus.ACTIVE)
            );

            Page<SearchWarehouseResponse> page = new PageImpl<>(contents, pageable, contents.size());

            SearchWarehouseCondition condition = new SearchWarehouseCondition("창고", null);

            given(warehouseRepository.search(condition, pageable)).willReturn(page);

            PageResponse<SearchWarehouseResponse> response = warehouseService.searchWarehouse(condition, pageable);

            assertThat(response.pageInfo().page()).isEqualTo(1);
            assertThat(response.pageInfo().totalElements()).isEqualTo(contents.size());
            assertThat(response.pageInfo().size()).isEqualTo(10);
        }
    }
}