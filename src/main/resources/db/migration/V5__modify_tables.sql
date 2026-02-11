-- 기존 데이터 존재할 시 값 변경
UPDATE sales_orders
SET status = 'SHIPPED'
WHERE status = 'COMPLETED'
;

UPDATE purchase_orders
SET status = 'RECEIVED'
WHERE status = 'COMPLETED'