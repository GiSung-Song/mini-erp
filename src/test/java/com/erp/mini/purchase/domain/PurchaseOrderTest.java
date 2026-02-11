package com.erp.mini.purchase.domain;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemFixture;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerFixture;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderTest {

    private final Partner partner = PartnerFixture.create();
    private final Item item = ItemFixture.create();
    private final Warehouse warehouse = WarehouseFixture.create();

    @Nested
    class add_line_test {
        @Test
        void add_line_success() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);

            purchaseOrder.addLine(item, warehouse, 10, new BigDecimal("2000.00"));

            assertThat(purchaseOrder.getPurchaseOrderLines()).hasSize(1);
        }

        @Test
        void add_line_fail_with_status_not_created() {
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner, PurchaseStatus.ORDERED);

            assertThatThrownBy(() -> purchaseOrder.addLine(item, warehouse, 10, new BigDecimal("2000.00")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;

                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void add_line_fail_with_exists_line() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
            purchaseOrder.addLine(item, warehouse, 10, new BigDecimal("2000.00"));

            assertThatThrownBy(() -> purchaseOrder.addLine(item, warehouse, 10, new BigDecimal("2000.00")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;

                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

    @Nested
    class remove_line_test {
        @Test
        void remove_line_success() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
            purchaseOrder.addLine(item, warehouse, 10, new BigDecimal("2000.00"));

            ReflectionTestUtils.setField(purchaseOrder.getPurchaseOrderLines().get(0), "id", 1L);

            purchaseOrder.removeLine(1L);

            assertThat(purchaseOrder.getPurchaseOrderLines()).isEmpty();
        }

        @Test
        void remove_line_fail_with_line_not_found() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);

            assertThatThrownBy(() -> purchaseOrder.removeLine(999L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        void remove_line_fail_with_status_not_created() {
            PurchaseOrder order =
                    PurchaseOrderFixture.create(partner, PurchaseStatus.ORDERED);

            assertThatThrownBy(() -> order.removeLine(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Nested
    class cancel_test {
        @Test
        void cancel_order_success() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);

            purchaseOrder.cancel();

            assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.CANCELLED);
        }

        @Test
        void cancel_order_success_with_status_ordered() {
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner, PurchaseStatus.ORDERED);

            purchaseOrder.cancel();

            assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.CANCELLED);
        }

        @Test
        void cancel_order_fail_with_status_cancelled() {
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner, PurchaseStatus.CANCELLED);

            assertThatThrownBy(() -> purchaseOrder.cancel())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void cancel_order_fail_with_status_received() {
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner, PurchaseStatus.RECEIVED);

            assertThatThrownBy(() -> purchaseOrder.cancel())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Nested
    class mark_as_ordered_test {
        @Test
        void mark_as_ordered_success() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);
            purchaseOrder.addLine(item, warehouse, 10, new BigDecimal("2000.00"));

            purchaseOrder.markAsOrdered();

            assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.ORDERED);
        }

        @Test
        void mark_as_ordered_fail_with_empty_line() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);

            assertThatThrownBy(() -> purchaseOrder.markAsOrdered())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void mark_as_ordered_fail_with_status_not_created() {
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner, PurchaseStatus.ORDERED);

            assertThatThrownBy(() -> purchaseOrder.markAsOrdered())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Nested
    class mark_as_received_test {
        @Test
        void mark_as_received_success() {
            PurchaseOrder purchaseOrder = PurchaseOrderFixture.create(partner, PurchaseStatus.ORDERED);

            purchaseOrder.markAsReceived();

            assertThat(purchaseOrder.getStatus()).isEqualTo(PurchaseStatus.RECEIVED);
        }

        @Test
        void mark_as_received_fail_with_status_not_ordered() {
            PurchaseOrder purchaseOrder = PurchaseOrder.createPurchaseOrder(partner);

            assertThatThrownBy(() -> purchaseOrder.markAsReceived())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException ex = (BusinessException) e;
                        assertThat(ex.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }
}