package com.erp.mini.sales.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.common.response.ErrorCode;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemFixture;
import com.erp.mini.partner.domain.Partner;
import com.erp.mini.partner.domain.PartnerFixture;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseFixture;

class SalesOrderTest {

    private final Partner customer = PartnerFixture.create();
    private final Item item1 = ItemFixture.create();
    private final Item item2 = ItemFixture.create();
    private final Warehouse warehouse1 = WarehouseFixture.create();
    private final Warehouse warehouse2 = WarehouseFixture.create();

    @Nested
    class createSalesOrder_test {

        @Test
        void createSalesOrder_success() {
            OrderCustomerInfo info = new OrderCustomerInfo("김철수", "010-9876-5432");
            ShippingAddress addr = new ShippingAddress("12345", "서울", "강남구");

            SalesOrder so = SalesOrder.createSalesOrder(customer, info, addr);

            assertThat(so.getPartner()).isEqualTo(customer);
            assertThat(so.getStatus()).isEqualTo(SalesStatus.CREATED);
            assertThat(so.getOrderCustomerInfo()).isEqualTo(info);
            assertThat(so.getShippingAddress()).isEqualTo(addr);
            assertThat(so.getSalesOrderLines()).isEmpty();
        }
    }

    @Nested
    class addLine_test {

        @Test
        void addLine_success_in_created_status() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("홍길동", "010-1111-1111"),
                    new ShippingAddress("00001", "서울", "강남")
            );

            so.addLine(item1, warehouse1, 5L, BigDecimal.valueOf(1500));

            assertThat(so.getSalesOrderLines()).hasSize(1);
            SalesOrderLine line = so.getSalesOrderLines().get(0);
            assertThat(line.getItem()).isEqualTo(item1);
            assertThat(line.getWarehouse()).isEqualTo(warehouse1);
            assertThat(line.getQty()).isEqualTo(5L);
            assertThat(line.getUnitPrice()).isEqualTo(BigDecimal.valueOf(1500));
        }

        @Test
        void addLine_fail_when_not_created() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("김철수", "010-2222-2222"),
                    new ShippingAddress("00002", "서울", "종로")
            );
            so.addLine(item1, warehouse1, 5L, BigDecimal.valueOf(1500));

            so.markAsOrdered();  // CREATED → ORDERED

            assertThatThrownBy(() -> so.addLine(item1, warehouse1, 3L, BigDecimal.valueOf(2000)))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void addLine_fail_when_duplicate_item_warehouse() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("이순신", "010-3333-3333"),
                    new ShippingAddress("00003", "서울", "용산")
            );

            so.addLine(item1, warehouse1, 10L, BigDecimal.valueOf(5000));

            assertThatThrownBy(() -> so.addLine(item1, warehouse1, 5L, BigDecimal.valueOf(5000)))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.INTERNAL_SERVER_ERROR);
        }

        @Test
        void addLine_success_different_warehouse_same_item() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("강감찬", "010-4444-4444"),
                    new ShippingAddress("00004", "서울", "동작")
            );

            so.addLine(item1, warehouse1, 8L, BigDecimal.valueOf(3000));
            so.addLine(item1, warehouse2, 12L, BigDecimal.valueOf(3000));

            assertThat(so.getSalesOrderLines()).hasSize(2);
        }

        @Test
        void addLine_success_same_warehouse_different_items() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("을지문덕", "010-5555-5555"),
                    new ShippingAddress("00005", "서울", "중구")
            );

            so.addLine(item1, warehouse1, 6L, BigDecimal.valueOf(2000));
            so.addLine(item2, warehouse1, 9L, BigDecimal.valueOf(2500));

            assertThat(so.getSalesOrderLines()).hasSize(2);
        }
    }

    @Nested
    class removeLine_test {

        @Test
        void removeLine_success() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("신사임당", "010-6666-6666"),
                    new ShippingAddress("00006", "서울", "종로")
            );

            so.addLine(item1, warehouse1, 5L, BigDecimal.valueOf(1500));
            so.addLine(item2, warehouse1, 7L, BigDecimal.valueOf(2000));

            assertThat(so.getSalesOrderLines()).hasSize(2);

            ReflectionTestUtils.setField(so.getSalesOrderLines().get(0), "id", 1L);
            Long lineIdToRemove = so.getSalesOrderLines().get(0).getId();

            so.removeLine(lineIdToRemove);

            assertThat(so.getSalesOrderLines()).hasSize(1);
            assertThat(so.getSalesOrderLines().get(0).getItem()).isEqualTo(item2);
        }

        @Test
        void removeLine_fail_when_not_created() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("이황", "010-7777-7777"),
                    new ShippingAddress("00007", "서울", "서초")
            );

            so.addLine(item1, warehouse1, 4L, BigDecimal.valueOf(3500));
            so.markAsOrdered();

            Long lineId = so.getSalesOrderLines().get(0).getId();

            assertThatThrownBy(() -> so.removeLine(lineId))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void removeLine_fail_when_line_not_found() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("정약용", "010-8888-8888"),
                    new ShippingAddress("00008", "서울", "강서")
            );

            assertThatThrownBy(() -> so.removeLine(999L))
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.NOT_FOUND);
        }
    }

    @Nested
    class markAsOrdered_test {

        @Test
        void markAsOrdered_success() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("홍경래", "010-9999-9999"),
                    new ShippingAddress("00009", "서울", "성북")
            );
            so.addLine(item1, warehouse1, 3L, BigDecimal.valueOf(4000));

            so.markAsOrdered();

            assertThat(so.getStatus()).isEqualTo(SalesStatus.ORDERED);
        }

        @Test
        void markAsOrdered_fail_when_not_created() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("세종대왕", "010-1010-1010"),
                    new ShippingAddress("00010", "서울", "영등포")
            );
            so.addLine(item1, warehouse1, 2L, BigDecimal.valueOf(5000));
            so.markAsOrdered();

            assertThatThrownBy(() -> so.markAsOrdered())
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void markAsOrdered_fail_when_no_lines() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("단종", "010-1111-1112"),
                    new ShippingAddress("00011", "서울", "마포")
            );

            assertThatThrownBy(() -> so.markAsOrdered())
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class cancel_test {

        @Test
        void cancel_success_from_created() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("문정왕후", "010-2222-2221"),
                    new ShippingAddress("00012", "서울", "은평")
            );
            so.addLine(item1, warehouse1, 1L, BigDecimal.valueOf(6000));

            so.cancel();

            assertThat(so.getStatus()).isEqualTo(SalesStatus.CANCELLED);
        }

        @Test
        void cancel_success_from_ordered() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("중종", "010-3333-3331"),
                    new ShippingAddress("00013", "서울", "동대문")
            );
            so.addLine(item1, warehouse1, 15L, BigDecimal.valueOf(1200));
            so.markAsOrdered();

            so.cancel();

            assertThat(so.getStatus()).isEqualTo(SalesStatus.CANCELLED);
        }

        @Test
        void cancel_fail_when_shipped() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("현종", "010-4444-4441"),
                    new ShippingAddress("00014", "서울", "강동")
            );
            so.addLine(item1, warehouse1, 11L, BigDecimal.valueOf(800));
            so.markAsOrdered();
            so.markAsShipped();

            assertThatThrownBy(() -> so.cancel())
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void cancel_fail_when_already_cancelled() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("숙종", "010-5555-5551"),
                    new ShippingAddress("00015", "서울", "도봉")
            );
            so.addLine(item1, warehouse1, 20L, BigDecimal.valueOf(700));
            so.cancel();

            assertThatThrownBy(() -> so.cancel())
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class markAsShipped_test {

        @Test
        void markAsShipped_success_from_ordered() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("경종", "010-6666-6661"),
                    new ShippingAddress("00016", "서울", "노원")
            );
            so.addLine(item1, warehouse1, 9L, BigDecimal.valueOf(900));
            so.markAsOrdered();

            so.markAsShipped();

            assertThat(so.getStatus()).isEqualTo(SalesStatus.SHIPPED);
        }

        @Test
        void markAsShipped_fail_when_not_ordered() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("영조", "010-7777-7771"),
                    new ShippingAddress("00017", "서울", "구로")
            );
            so.addLine(item1, warehouse1, 7L, BigDecimal.valueOf(1100));

            assertThatThrownBy(() -> so.markAsShipped())
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }

        @Test
        void markAsShipped_fail_when_cancelled() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("정조", "010-8888-8881"),
                    new ShippingAddress("00018", "서울", "금천")
            );
            so.addLine(item1, warehouse1, 13L, BigDecimal.valueOf(950));
            so.cancel();

            assertThatThrownBy(() -> so.markAsShipped())
                    .isInstanceOf(BusinessException.class)
                    .matches(ex -> ((BusinessException) ex).getErrorCode() == ErrorCode.BAD_REQUEST);
        }
    }

    @Nested
    class isOrdered_test {

        @Test
        void isOrdered_true_when_ordered() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("순조", "010-9999-9991"),
                    new ShippingAddress("00019", "서울", "중랑")
            );
            so.addLine(item1, warehouse1, 6L, BigDecimal.valueOf(1300));
            so.markAsOrdered();

            assertThat(so.isOrdered()).isTrue();
        }

        @Test
        void isOrdered_false_when_created() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("헌종", "010-1212-1212"),
                    new ShippingAddress("00020", "서울", "송파")
            );

            assertThat(so.isOrdered()).isFalse();
        }

        @Test
        void isOrdered_false_when_shipped() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("철종", "010-1313-1313"),
                    new ShippingAddress("00021", "서울", "강북")
            );
            so.addLine(item1, warehouse1, 4L, BigDecimal.valueOf(1400));
            so.markAsOrdered();
            so.markAsShipped();

            assertThat(so.isOrdered()).isFalse();
        }

        @Test
        void isOrdered_false_when_cancelled() {
            SalesOrder so = SalesOrder.createSalesOrder(
                    customer,
                    new OrderCustomerInfo("고종", "010-1414-1414"),
                    new ShippingAddress("00022", "서울", "양천")
            );
            so.addLine(item1, warehouse1, 2L, BigDecimal.valueOf(1500));
            so.cancel();

            assertThat(so.isOrdered()).isFalse();
        }
    }
}
