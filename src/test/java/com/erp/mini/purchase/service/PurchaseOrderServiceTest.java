package com.erp.mini.purchase.service;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemFixture;
import com.erp.mini.item.repo.ItemRepository;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerFixture;
import com.erp.mini.partner.domain.PartnerType;
import com.erp.mini.partner.repo.PartnerRepository;
import com.erp.mini.purchase.domain.*;
import com.erp.mini.purchase.dto.*;
import com.erp.mini.purchase.repo.PurchaseOrderRepository;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseFixture;
import com.erp.mini.warehouse.repo.WarehouseRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    @Nested
    class create_purchase_test {
        @Test
        void add_purchase_success() {
            Partner partner = PartnerFixture.create(
                    "공급처", "SUP000001", PartnerType.SUPPLIER, null, null
            );
            given(partnerRepository.findById(1L)).willReturn(Optional.of(partner));

            Item item = ItemFixture.create();
            Warehouse warehouse = WarehouseFixture.create();

            given(itemRepository.findAllById(any())).willReturn(List.of(item));
            given(warehouseRepository.findAllById(any())).willReturn(List.of(warehouse));

            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    item.getId(), warehouse.getId(), BigDecimal.valueOf(1000), 5)
                    )
            );

            // when
            purchaseOrderService.createPurchase(request);

            // then
            verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        }

        @Test
        void add_purchase_fail_with_partner_not_found() {
            Partner partner = PartnerFixture.create();
            given(partnerRepository.findById(1L)).willReturn(Optional.empty());

            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(
                                    1L, 1L, BigDecimal.valueOf(1000), 5)
                    )
            );

            assertThatThrownBy(() -> purchaseOrderService.createPurchase(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

        }

        @Test
        void add_purchase_fail_with_item_not_found() {
            Partner partner = PartnerFixture.create(
                    "공급처", "SUP000001", PartnerType.SUPPLIER, null, null
            );
            given(partnerRepository.findById(1L)).willReturn(Optional.of(partner));

            Item item = ItemFixture.create();
            Warehouse warehouse = WarehouseFixture.create();

            given(itemRepository.findAllById(any())).willReturn(List.of(item));
            given(warehouseRepository.findAllById(any())).willReturn(List.of(warehouse));

            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(1L, 2L, BigDecimal.valueOf(1000), 5),
                            new PurchaseOrderRequest.PurchaseLine(2L, 2L, BigDecimal.valueOf(2000), 5)
                    )
            );

            assertThatThrownBy(() -> purchaseOrderService.createPurchase(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void add_purchase_fail_with_warehouse_not_found() {
            Partner partner = PartnerFixture.create(
                    "공급처", "SUP000001", PartnerType.SUPPLIER, null, null
            );
            given(partnerRepository.findById(1L)).willReturn(Optional.of(partner));

            Item item = ItemFixture.create();
            Item item2 = ItemFixture.create();
            Warehouse warehouse = WarehouseFixture.create();

            given(itemRepository.findAllById(any())).willReturn(List.of(item, item2));
            given(warehouseRepository.findAllById(any())).willReturn(List.of(warehouse));

            PurchaseOrderRequest request = new PurchaseOrderRequest(
                    1L,
                    List.of(
                            new PurchaseOrderRequest.PurchaseLine(1L, 1L, BigDecimal.valueOf(1000), 5),
                            new PurchaseOrderRequest.PurchaseLine(2L, 2L, BigDecimal.valueOf(2000), 5)
                    )
            );

            assertThatThrownBy(() -> purchaseOrderService.createPurchase(request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class add_line_test {
        @Test
        void add_line_success() {
            Partner partner = PartnerFixture.create();
            Item item = ItemFixture.create();
            Warehouse warehouse = WarehouseFixture.create();
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner);

            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.of(purchaseOrder));
            given(itemRepository.findById(any())).willReturn(Optional.of(item));
            given(warehouseRepository.findById(any())).willReturn(Optional.of(warehouse));

            AddPurchaseOrderLineRequest request =
                    new AddPurchaseOrderLineRequest(1L, 1L, BigDecimal.valueOf(1000), 10);

            purchaseOrderService.addOrderLine(1L, request);

            assertThat(purchaseOrder.getPurchaseOrderLines()).hasSize(1);
        }

        @Test
        void add_line_fail_with_purchase_order_not_found() {
            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.empty());

            AddPurchaseOrderLineRequest request =
                    new AddPurchaseOrderLineRequest(1L, 1L, BigDecimal.valueOf(1000), 10);

            assertThatThrownBy(() -> purchaseOrderService.addOrderLine(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void add_line_fail_with_item_not_found() {
            Partner partner = PartnerFixture.create();
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner);

            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.of(purchaseOrder));
            given(itemRepository.findById(any())).willReturn(Optional.empty());

            AddPurchaseOrderLineRequest request =
                    new AddPurchaseOrderLineRequest(1L, 1L, BigDecimal.valueOf(1000), 10);

            assertThatThrownBy(() -> purchaseOrderService.addOrderLine(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void add_line_fail_with_warehouse_not_found() {
            Partner partner = PartnerFixture.create();
            Item item = ItemFixture.create();
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner);

            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.of(purchaseOrder));
            given(itemRepository.findById(any())).willReturn(Optional.of(item));

            AddPurchaseOrderLineRequest request =
                    new AddPurchaseOrderLineRequest(1L, 1L, BigDecimal.valueOf(1000), 10);

            assertThatThrownBy(() -> purchaseOrderService.addOrderLine(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class remove_line_test {
        @Test
        void remove_line_success() {
            Partner partner = PartnerFixture.create();
            Item item = ItemFixture.create();
            Warehouse warehouse = WarehouseFixture.create();
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner);

            purchaseOrder.addLine(item, warehouse, 10, BigDecimal.valueOf(1000));

            PurchaseOrderLine purchaseOrderLine = purchaseOrder.getPurchaseOrderLines().get(0);
            ReflectionTestUtils.setField(purchaseOrderLine, "id", 1L);

            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.of(purchaseOrder));
            purchaseOrderService.removePurchaseOrder(1L, 1L);

            assertThat(purchaseOrder.getPurchaseOrderLines()).isEmpty();
        }

        @Test
        void remove_line_fail_with_purchase_order_not_found() {
            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> purchaseOrderService.removePurchaseOrder(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class order_purchase_test {
        @Test
        void order_purchase_success() {
            Partner partner = PartnerFixture.create();
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner);
            Item item = ItemFixture.create();
            Warehouse warehouse = WarehouseFixture.create();

            purchaseOrder.addLine(item, warehouse, 10, BigDecimal.valueOf(1000));

            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.of(purchaseOrder));

            purchaseOrderService.orderPurchase(1L);
            assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.ORDERED);
        }

        @Test
        void order_purchase_fail_with_purchase_order_not_found() {
            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> purchaseOrderService.orderPurchase(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class cancel_purchase_test {
        @Test
        void cancel_purchase_success() {
            Partner partner = PartnerFixture.create();
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner);

            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.of(purchaseOrder));

            purchaseOrderService.cancelPurchase(purchaseOrder.getId());
            assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.CANCELLED);
        }

        @Test
        void cancel_purchase_fail_with_purchase_order_not_found() {
            given(purchaseOrderRepository.findById(1L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> purchaseOrderService.cancelPurchase(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    @Nested
    class get_purchase_order_detail_test {
        @Test
        void get_purchase_order_detail_success() {
            PurchaseHeaderDto header = mock(PurchaseHeaderDto.class);
            List<PurchaseLineDto> lines = List.of(mock(PurchaseLineDto.class));

            given(purchaseOrderRepository.getPurchaseDetailHeader(1L)).willReturn(header);
            given(purchaseOrderRepository.getPurchaseDetailLines(1L)).willReturn(lines);

            PurchaseDetailResponse response = purchaseOrderService.getPurchaseOrderDetail(1L);

            assertThat(response.header()).isEqualTo(header);
            assertThat(response.lines()).isEqualTo(lines);
        }

        @Test
        void get_purchase_order_detail_fail_with_purchase_order_not_found() {
            given(purchaseOrderRepository.getPurchaseDetailHeader(1L)).willReturn(null);

            assertThatThrownBy(() -> purchaseOrderService.getPurchaseOrderDetail(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }
}