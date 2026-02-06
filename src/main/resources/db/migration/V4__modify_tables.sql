ALTER TABLE warehouses MODIFY code VARCHAR(50) NULL;

-- warehouses 테이블에 인덱스 추가
CREATE INDEX idx_warehouses_name on warehouses(name);
CREATE INDEX idx_warehouses_location on warehouses(location);