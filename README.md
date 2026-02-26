# Inventory & Order Management System 
재고 중심 주문 관리 시스템입니다.

---
## 1. Tech Stack
- Java 17
- Spring Boot 3.5
- Spring Data JPA
- MySQL
- Docker Compose (DB)
- Redis
- Test: JUnit5 / Testcontainers

---
## 2. Scope
- Items(상품)
- Warehouses(창고)
- Partners(거래처: supplier / customer)
- Stocks(재고: item + warehouse 단위)
- SalesOrders(출고) / PurchaseOrders(입고)
- InventoryTransactions(재고 원장)