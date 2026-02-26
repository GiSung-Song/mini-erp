package com.erp.mini.stock.domain;

import com.erp.mini.common.response.BusinessException;
import com.erp.mini.item.domain.Item;
import com.erp.mini.item.domain.ItemFixture;
import com.erp.mini.warehouse.domain.Warehouse;
import com.erp.mini.warehouse.domain.WarehouseFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    private final Item item = ItemFixture.create();
    private final Warehouse warehouse = WarehouseFixture.create();

    @Nested
    class increase_test {

        @Test
        void increase_success() {
            Stock stock = Stock.createStock(item, warehouse);

            stock.increase(10);

            assertThat(stock.getQty()).isEqualTo(10);
        }

        @Test
        void increase_fail_with_minus_qty() {
            Stock stock = Stock.createStock(item, warehouse);

            assertThatThrownBy(() -> stock.increase(-1))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Nested
    class decrease_test {

        @Test
        void decrease_success() {
            Stock stock = Stock.createStock(item, warehouse);
            stock.increase(100);

            stock.decrease(17);

            assertThat(stock.getQty()).isEqualTo(83);
        }

        @Test
        void decrease_fail_with_minus_qty() {
            Stock stock = Stock.createStock(item, warehouse);
            stock.increase(100);

            assertThatThrownBy(() -> stock.decrease(-1))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }

        @Test
        void decrease_fail_with_shortage_qty() {
            Stock stock = Stock.createStock(item, warehouse);
            stock.increase(100);

            assertThatThrownBy(() -> stock.decrease(101))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException exception = (BusinessException) ex;

                        assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }
    }

}