-- 파트너 type 인덱스 추가 (검색 조회용)
CREATE INDEX idx_partners_type ON partners(type);

-- 창고 status 인덱스 추가 (검색 조회용)
CREATE INDEX idx_warehouses_status ON warehouses(status);