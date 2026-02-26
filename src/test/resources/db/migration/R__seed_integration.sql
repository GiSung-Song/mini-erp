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
