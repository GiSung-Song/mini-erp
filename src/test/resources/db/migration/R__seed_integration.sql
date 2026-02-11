-- 테스트 사용자
INSERT INTO users (id, name, employee_number, password, status)
VALUES (
    1,
    'SYSTEM',
    'SYSTEM',
    'DUMMY_ENCODED_PASSWORD',
    'ACTIVE'
)
ON DUPLICATE KEY UPDATE id = id;

-- 테스트 아이템
INSERT INTO items (id, name, code, base_price, status, created_by, updated_by)
VALUES (
    1,
    '테스트 아이템1',
    'ITEM_TEST1',
    1000,
    'ACTIVE',
    1,
    1
)
ON DUPLICATE KEY UPDATE id = id;

-- 테스트 아이템
INSERT INTO items (id, name, code, base_price, status, created_by, updated_by)
VALUES (
    2,
    '테스트 아이템2',
    'ITEM_TEST2',
    2000,
    'ACTIVE',
    1,
    1
)
ON DUPLICATE KEY UPDATE id = id;

-- 테스트 창고
INSERT INTO warehouses (id, name, code, location, status, created_by, updated_by)
VALUES (
    1,
    '테스트 1창고',
    'WH_TEST',
    '테스트시 테스트구 테스트동 테스트지역 12-34',
    'ACTIVE',
    1,
    1
)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO warehouses (id, name, code, location, status, created_by, updated_by)
VALUES (
    2,
    '테스트 2창고',
    'WH_TEST2',
    '테스트시 테스트구 테스트동 테스트지역 56-78',
    'INACTIVE',
    1,
    1
)
ON DUPLICATE KEY UPDATE id = id;

-- 공급사
INSERT INTO partners (id, name, code, type, phone, email, created_by, updated_by)
VALUES (
    1,
    '테스트 공급사',
    'SUPPLIER_TEST',
    'SUPPLIER',
    '01012344321',
    'test@supplier.com',
    1,
    1
)
ON DUPLICATE KEY UPDATE id = id;

-- 고객사
INSERT INTO partners (id, name, code, type, phone, email, created_by, updated_by)
VALUES (
    2,
    '테스트 고객사',
    'CUSTOMER_TEST',
    'CUSTOMER',
    '01043211234',
    'test@customer.com',
    1,
    1
)
ON DUPLICATE KEY UPDATE id = id;