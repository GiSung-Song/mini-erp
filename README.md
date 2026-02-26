# Inventory & Order Management System
경량 재고 중심 주문 관리 프로젝트입니다. 현재 백엔드(Spring Boot) 기반으로 도메인, 저장소, 서비스, 컨트롤러 계층과 통합/단위 테스트가 구현되어 있습니다.

---

## 개요
- 목적: 상품/창고/거래처 관리를 중심으로 재고 흐름(입고/출고)과 재고 원장(InventoryTransaction)을 기록하고 조회하는 시스템입니다.

---

## 기술 스택
- Java 17
- Spring Boot 3.x
- Spring Data JPA, QueryDSL
- MySQL (테스트에서는 Testcontainers 사용)
- Flyway DB 마이그레이션
- 테스트: JUnit 5, Testcontainers

---

## 주요 기능
- 상품/창고/거래처 CRUD
- 입/출고 주문 생성 및 상태 변경
- 재고 및 재고 트랜잭션 기록
- 복잡 검색(QueryDSL 기반) 및 페이징
- 트랜잭션 동시성 제어(비관적 락 + 재시도)

---

## Scope
- Items(상품)
- Warehouses(창고)
- Partners(거래처: supplier / customer)
- Stocks(재고: item + warehouse 단위)
- SalesOrders(출고) / PurchaseOrders(입고)
- InventoryTransactions(재고 원장)

---

## REST API
모든 엔드포인트는 `/api/**` 아래에 있습니다.  
주요 리소스별 예시는 다음과 같습니다.

### Purchase orders
* `POST /api/purchase-orders` – 입고 발주 생성
* `POST /api/purchase-orders/{id}/receive` – 입고 완료

### Sales orders
* `POST /api/sales-orders` - 출고 발주 생성
* `POST /api/sales-orders/{id}/order`  – 출고 확정
* `POST /api/sales-orders/{id}/cancel` – 출고 취소

### Stocks
* `POST /api/stocks/adjust` – 재고 조정
* `GET  /api/stocks/items/{itemId}` - 특정 상품의 재고 현황
* `GET  /api/stocks/warehouses/{warehouseId}` - 특정 창고의 재고 현황

### Inventory transactions
* `GET /api/transactions` – 재고 이력 검색(필터/페이징)
* `GET /api/transactions/{id}` - 특정 재고 이력 조회

(전체 목록은 Swagger/OpenAPI 문서에서 확인 가능)

## 아키텍처 & 설계
- 표준적인 레이어드(Spring MVC → Service → Repository) 구조
- DTO 매핑, 글로벌 예외 핸들러
- 비관적 락(`@Lock(PESSIMISTIC_WRITE)`/`FOR UPDATE`)을 이용한 재고 동시성 제어와 `@Retryable` 재시도
- 복잡한 조회는 QueryDSL 커스텀 리포지토리로 구현
- 통합/단위 테스트 클래스별로 작성

## 향후 계획
- **캐싱**: 상품·창고·거래처 목록에 Redis `@Cacheable` 적용
- **배치/집계**: 일별·상품별·월별 집계를 Spring Batch 혹은 `@Scheduled`로 구현
- **비동기/스레드**: 무거운 집계를 백그라운드 처리
- **프론트**: `src/main/resources/static`에 간단한 HTML/JS로 API 테스트용 페이지 추가 예정