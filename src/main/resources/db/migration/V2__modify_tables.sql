-- items 테이블에 인덱스 추가
CREATE INDEX idx_items_name on items(name);

-- 상품 코드 sequence 테이블 추가
CREATE TABLE item_code_sequence
(
    id       TINYINT PRIMARY KEY,
    next_val BIGINT  NOT NULL,

    CONSTRAINT chk_item_code_sequence_single_row CHECK (id = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 상품 코드 초기 데이터 추가
INSERT INTO item_code_sequence (id, next_val) VALUES (1, 1);