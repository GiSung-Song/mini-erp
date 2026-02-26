ALTER TABLE sales_orders
    ADD COLUMN customer_name  VARCHAR(50) NOT NULL,
    ADD COLUMN customer_phone VARCHAR(30) NOT NULL,
    ADD COLUMN zipcode        VARCHAR(20) NOT NULL,
    ADD COLUMN address1       VARCHAR(255) NOT NULL,
    ADD COLUMN address2       VARCHAR(255) NOT NULL
;

-- 인덱스 삭제
DROP INDEX idx_inventory_transactions_item_warehouse_created
ON inventory_transactions;

DROP INDEX idx_inventory_transactions_ref
ON inventory_transactions;

-- 인덱스 추가
CREATE INDEX idx_inventory_transactions_warehouse_created
ON inventory_transactions (warehouse_id, created_at);

-- Check 제약조건 수정
ALTER TABLE purchase_orders
    DROP CHECK chk_purchase_orders_status;

ALTER TABLE purchase_orders
    ADD CONSTRAINT chk_purchase_orders_status CHECK (status IN ('CREATED', 'CANCELLED', 'ORDERED', 'RECEIVED'));

ALTER TABLE sales_orders
    DROP CHECK chk_sales_orders_status;

ALTER TABLE sales_orders
    ADD CONSTRAINT chk_sales_orders_status CHECK (status IN ('CREATED', 'CANCELLED', 'ORDERED', 'SHIPPED'));