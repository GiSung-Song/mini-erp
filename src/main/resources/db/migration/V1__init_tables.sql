-- 사용자 테이블
CREATE TABLE users
(
	id              BIGINT AUTO_INCREMENT PRIMARY KEY,       -- 식별자 ID
	name            VARCHAR(20)  NOT NULL,                   -- 이름
	employee_number VARCHAR(50)  NOT NULL,                   -- 사번
	password        VARCHAR(255) NOT NULL,                   -- 비밀번호
	status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',  -- 상태
	deleted_at      DATETIME NULL,                           -- 탈퇴 시각
	created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각
	last_login_at   DATETIME NULL,

	UNIQUE KEY uq_users_employee (employee_number),

	CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 상품 테이블
CREATE TABLE items
(
	id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
	name       VARCHAR(100)   NOT NULL,           -- 상품명
	code       VARCHAR(50)    NOT NULL,           -- 상품 코드
	base_price DECIMAL(15, 2) NOT NULL,           -- 상품 가격
	status     VARCHAR(10)    NOT NULL DEFAULT 'ACTIVE', -- 상태
    created_by BIGINT NOT NULL, -- 생성자 식별자 ID
    updated_by BIGINT NOT NULL, -- 수정자 식별자 ID
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각

	UNIQUE KEY uq_items_code (code),

	CONSTRAINT chk_items_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 창고 테이블
CREATE TABLE warehouses
(
	id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
	name       VARCHAR(50)  NOT NULL,             -- 창고명
	code       VARCHAR(50)  NOT NULL,             -- 창고 코드
	location   VARCHAR(255) NOT NULL,             -- 창고 위치
	status     VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE', -- 상태
    created_by BIGINT NOT NULL, -- 생성자 식별자 ID
    updated_by BIGINT NOT NULL, -- 수정자 식별자 ID
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각

	UNIQUE KEY uq_warehouses_code (code),

	CONSTRAINT chk_warehouses_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 파트너 테이블 (거래처, 구매처)
CREATE TABLE partners
(
	id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
	name       VARCHAR(100) NOT NULL,             -- 파트너사 이름
	code       VARCHAR(50)  NOT NULL,             -- 파트너사 코드
	type       VARCHAR(20)  NOT NULL,             -- 파트너사 종류 (거래처, 구매처)
	phone      VARCHAR(30)  NULL, -- 연락처
	email      VARCHAR(100) NULL, -- 이메일
    created_by BIGINT NOT NULL,   -- 생성자 식별자 ID
    updated_by BIGINT NOT NULL,   -- 수정자 식별자 ID
	created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각

	UNIQUE KEY uq_partners_code (code),

	CONSTRAINT chk_partners_type   CHECK (type IN ('CUSTOMER', 'SUPPLIER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 재고 테이블
CREATE TABLE stocks
(
	id           BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
	item_id      BIGINT NOT NULL,                   -- 상품 식별자 ID
	warehouse_id BIGINT NOT NULL,                   -- 창고 식별자 ID
	qty          BIGINT NOT NULL,                   -- 수량
    created_by   BIGINT NOT NULL,                   -- 생성자 식별자 ID
    updated_by   BIGINT NOT NULL,                   -- 수정자 식별자 ID
	created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각

	UNIQUE KEY uq_stocks_item_warehouse (item_id, warehouse_id),

	KEY idx_stocks_warehouse (warehouse_id),

	CONSTRAINT fk_stocks_item      FOREIGN KEY (item_id)      REFERENCES items(id),
    CONSTRAINT fk_stocks_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT chk_stocks_qty CHECK (qty >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 판매 테이블
CREATE TABLE sales_orders
(
	id           BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 식별자 ID
	customer_id  BIGINT      NOT NULL,                   -- 고객사 식별자 ID
	status       VARCHAR(10) NOT NULL DEFAULT 'CREATED', -- 상태
    created_by   BIGINT      NOT NULL,                   -- 생성자 식별자 ID
    updated_by   BIGINT      NOT NULL,                   -- 수정자 식별자 ID
	created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각

	KEY idx_sales_orders_customer_created (customer_id, created_at),

	CONSTRAINT fk_sales_orders_customer  FOREIGN KEY (customer_id)  REFERENCES partners(id),
	CONSTRAINT chk_sales_orders_status CHECK (status IN ('CREATED', 'CANCELLED', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 판매 상품 테이블
CREATE TABLE sales_order_lines
(
	id             BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
	sales_order_id BIGINT NOT NULL,                   -- 판매 식별자 ID
	item_id        BIGINT NOT NULL,                   -- 상품 식별자 ID
    warehouse_id   BIGINT NOT NULL,                   -- 창고 식별자 ID
	qty            BIGINT NOT NULL,                   -- 수량
	unit_price     DECIMAL(15, 2) NOT NULL,           -- 가격

	UNIQUE KEY uq_sales_ordr_lines_sales_order_item_warehouse (sales_order_id, item_id, warehouse_id),

	KEY idx_sales_order_lines_item (item_id),

	CONSTRAINT fk_sales_order_lines_sales_order FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id),
	CONSTRAINT fk_sales_order_lines_item        FOREIGN KEY (item_id)        REFERENCES items(id),
	CONSTRAINT fk_sales_order_lines_warehouse   FOREIGN KEY (warehouse_id)   REFERENCES warehouses(id),
	CONSTRAINT chk_sales_order_lines_qty        CHECK (qty > 0),
	CONSTRAINT chk_sales_order_lines_unit_price CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 입고 테이블
CREATE TABLE purchase_orders
(
	id           BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 식별자 ID
	supplier_id  BIGINT      NOT NULL,                   -- 공급처 식별자 ID
	status       VARCHAR(10) NOT NULL DEFAULT 'CREATED', -- 상태
    created_by   BIGINT      NOT NULL,                   -- 생성자 식별자 ID
    updated_by   BIGINT      NOT NULL,                   -- 수정자 식별자 ID
	created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,                             -- 생성 시각
	updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 변경 시각

	KEY idx_purchase_orders_supplier_created (supplier_id, created_at),

	CONSTRAINT fk_purchase_orders_supplier  FOREIGN KEY (supplier_id)  REFERENCES partners(id),
	CONSTRAINT chk_purchase_orders_status CHECK (status IN ('CREATED', 'CANCELLED', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 입고 상품 테이블
CREATE TABLE purchase_order_lines
(
	id                BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
	purchase_order_id BIGINT NOT NULL,                   -- 입고 식별자 ID
	item_id           BIGINT NOT NULL,                   -- 상품 식별자 ID
    warehouse_id      BIGINT NOT NULL,                   -- 창고 식별자 ID
	qty               BIGINT NOT NULL,                   -- 수량
	unit_cost         DECIMAL(15, 2) NOT NULL,           -- 원가

	UNIQUE KEY uq_purchase_order_lines_purchase_order_item_warehouse (purchase_order_id, item_id, warehouse_id),

	KEY idx_purchase_order_lines_item (item_id),

	CONSTRAINT fk_purchase_order_lines_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
	CONSTRAINT fk_purchase_order_lines_item           FOREIGN KEY (item_id)           REFERENCES items(id),
	CONSTRAINT fk_purchase_order_lines_warehouse      FOREIGN KEY (warehouse_id)      REFERENCES warehouses(id),
	CONSTRAINT chk_purchase_order_lines_qty        CHECK (qty > 0),
	CONSTRAINT chk_purchase_order_lines_unit_cost CHECK (unit_cost >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 재고 이력
CREATE TABLE inventory_transactions
(
	id           BIGINT AUTO_INCREMENT PRIMARY KEY, -- 식별자 ID
    item_id      BIGINT NOT NULL,                   -- 상품 식별자 ID
    warehouse_id BIGINT NOT NULL,                   -- 창고 식별자 ID

    type         VARCHAR(10) NOT NULL,  -- INBOUND 입고 / OUTBOUND 출고 / ADJUST 조정 등
    qty_delta    BIGINT      NOT NULL,  -- +입고, -출고 (부호 포함)

    ref_type     VARCHAR(20) NULL,      -- SALES_ORDER / PURCHASE_ORDER
    ref_id       BIGINT      NULL,      -- SALES_ORDER 식별자 ID / PURCHASE_ORDER 식별자 ID

    reason       VARCHAR(255) NULL,     -- ADJUST 사유(또는 공용 메모)
    created_by   BIGINT       NOT NULL, -- 생성자 식별자 ID
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 시각

    KEY idx_inventory_transactions_item_warehouse_created (item_id, warehouse_id, created_at),
    KEY idx_inventory_transactions_item_created (item_id, created_at),
    KEY idx_inventory_transactions_ref (ref_type, ref_id),

    CONSTRAINT fk_inventory_transactions_item       FOREIGN KEY (item_id)      REFERENCES items(id),
    CONSTRAINT fk_inventory_transactions_warehouse  FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),

    CONSTRAINT chk_inventory_transactions_type      CHECK (type IN ('INBOUND', 'OUTBOUND', 'ADJUST')),
    CONSTRAINT chk_inventory_transactions_qty_delta CHECK (qty_delta <> 0),

    CONSTRAINT chk_inventory_transactions_sign_rule CHECK (
        (type='INBOUND'  AND qty_delta >  0) OR
        (type='OUTBOUND' AND qty_delta <  0) OR
        (type='ADJUST'   AND qty_delta <> 0)
    ),

    CONSTRAINT chk_inventory_transactions_ref_rule CHECK (
    	(type='ADJUST' AND ref_type IS NULL AND ref_id IS NULL)
    	OR
    	(type IN ('INBOUND', 'OUTBOUND') AND ref_type IS NOT NULL AND ref_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;