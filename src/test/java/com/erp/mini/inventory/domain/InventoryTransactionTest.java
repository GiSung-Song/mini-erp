package com.erp.mini.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemFixture;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseFixture;

class InventoryTransactionTest {

    private final Item item = ItemFixture.create();
    private final Warehouse warehouse = WarehouseFixture.create();

    @Nested
    class purchase_inbound_test {
        @Test
        void purchase_inbound_success() {
            InventoryTransaction inbound = InventoryTransaction.purchaseInbound(item, warehouse, 10, 1L);

            assertThat(inbound.getType()).isEqualTo(TransactionType.INBOUND);
            assertThat(inbound.getRefType()).isEqualTo(RefType.PURCHASE_ORDER);
            assertThat(inbound.getRefId()).isEqualTo(1L);
            assertThat(inbound.getReason()).isNull();
        }

        @Test
        void purchase_inbound_fail_zero_qty() {
            assertThatThrownBy(() -> InventoryTransaction.purchaseInbound(item, warehouse, 0, 1L))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void purchase_inbound_fail_negative_qty() {
            assertThatThrownBy(() -> InventoryTransaction.purchaseInbound(item, warehouse, -5, 1L))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void purchase_inbound_fail_null_refId() {
            assertThatThrownBy(() -> InventoryTransaction.purchaseInbound(item, warehouse, 10, null))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class cancel_sales_inboud_test {
        @Test
        void cancel_sales_inbound_success() {
            InventoryTransaction inbound = InventoryTransaction.cancelSalesInbound(item, warehouse, 5, 2L);

            assertThat(inbound.getType()).isEqualTo(TransactionType.INBOUND);
            assertThat(inbound.getRefType()).isEqualTo(RefType.SALES_ORDER);
            assertThat(inbound.getRefId()).isEqualTo(2L);
            assertThat(inbound.getReason()).isNull();
        }

        @Test
        void cancel_sales_inbound_fail_zero_qty() {
            assertThatThrownBy(() -> InventoryTransaction.cancelSalesInbound(item, warehouse, 0, 2L))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void cancel_sales_inbound_fail_negative_qty() {
            assertThatThrownBy(() -> InventoryTransaction.cancelSalesInbound(item, warehouse, -3, 2L))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void cancel_sales_inbound_fail_null_refId() {
            assertThatThrownBy(() -> InventoryTransaction.cancelSalesInbound(item, warehouse, 5, null))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class sales_outbound_test {
        @Test
        void sales_outbound_success() {
            InventoryTransaction outbound = InventoryTransaction.salesOutbound(item, warehouse, 8, 3L);

            assertThat(outbound.getType()).isEqualTo(TransactionType.OUTBOUND);
            assertThat(outbound.getRefType()).isEqualTo(RefType.SALES_ORDER);
            assertThat(outbound.getQtyDelta()).isEqualTo(-8);
            assertThat(outbound.getRefId()).isEqualTo(3L);
            assertThat(outbound.getReason()).isNull();
        }

        @Test
        void sales_outbound_fail_zero_qty() {
            assertThatThrownBy(() -> InventoryTransaction.salesOutbound(item, warehouse, 0, 3L))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void sales_outbound_fail_negative_qty() {
            assertThatThrownBy(() -> InventoryTransaction.salesOutbound(item, warehouse, -4, 3L))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void sales_outbound_fail_null_refId() {
            assertThatThrownBy(() -> InventoryTransaction.salesOutbound(item, warehouse, 8, null))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class adjust_test {
        @Test
        void adjust_success() {
            InventoryTransaction adjust = InventoryTransaction.adjust(item, warehouse, -2, "손상");

            assertThat(adjust.getType()).isEqualTo(TransactionType.ADJUST);
            assertThat(adjust.getRefType()).isNull();
            assertThat(adjust.getRefId()).isNull();
            assertThat(adjust.getReason()).isEqualTo("손상");
        }

        @Test
        void adjust_fail_zero_delta() {
            assertThatThrownBy(() -> InventoryTransaction.adjust(item, warehouse, 0, "조정"))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void adjust_fail_blank_reason() {
            assertThatThrownBy(() -> InventoryTransaction.adjust(item, warehouse, 5, ""))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void adjust_fail_null_reason() {
            assertThatThrownBy(() -> InventoryTransaction.adjust(item, warehouse, -3, null))
                    .isInstanceOf(BusinessException.class)
                    .matches(e -> ((BusinessException) e).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }
}