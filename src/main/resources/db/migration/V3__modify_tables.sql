ALTER TABLE partners MODIFY code VARCHAR(50) NULL;

-- partners 테이블에 인덱스 추가
CREATE INDEX idx_partners_name on partners(name);