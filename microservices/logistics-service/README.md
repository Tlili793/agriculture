# logistics microservice

Spring Boot 3.2 / Java 17 service for the **Logistics domain** of JobBoard-style microservices:
**Delivery** and **Warehouse**, with **MySQL** and **Swagger**.

The two tables are **independent**: no foreign key between `delivery` and `warehouse`, and
`delivery.order_id` is a plain number (the order belongs to the Ordering microservice).

## 1. Database
MySQL must be running on `localhost:3306` (user `root`, empty password by default; edit
`spring.datasource.username/password` in `application.properties` if yours differ).

With `spring.jpa.hibernate.ddl-auto=update`, the database `logistics_db` and its tables are
created automatically on the first start. To also get sample data, run the optional script:

```bash
mysql -u root -p < database/logistics_db.sql
```

## 2. Run
```bash
mvn spring-boot:run
```
- Swagger UI: http://localhost:8086/swagger-ui.html
- OpenAPI JSON: http://localhost:8086/v3/api-docs
- Hello endpoint: http://localhost:8086/api/logistics/hi  ->  `Hi, from Ines`

## Endpoints
| Method | Path | Description |
|---|---|---|
| GET | /api/logistics/hi | Greeting: "Hi, from Ines" |
| GET | /api/deliveries?status=&orderId= | List deliveries (filters optional) |
| GET | /api/deliveries/{id} | Get one delivery |
| POST | /api/deliveries | Create delivery (status defaults to PENDING) |
| PUT | /api/deliveries/{id} | Update delivery |
| PATCH | /api/deliveries/{id}/status?status=DISPATCHED | Change status (DISPATCHED sets dispatchedAt) |
| DELETE | /api/deliveries/{id} | Delete delivery |
| GET | /api/warehouses | List warehouses |
| GET | /api/warehouses/{id} | Get one warehouse |
| POST | /api/warehouses | Create warehouse |
| PUT | /api/warehouses/{id} | Update warehouse |
| DELETE | /api/warehouses/{id} | Delete warehouse |

Delivery statuses: `PENDING`, `DISPATCHED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`.
Errors: 400 validation / bad value, 404 not found (RFC 7807 JSON).

## Examples
```json
POST /api/warehouses
{ "name": "Tunis Central Warehouse", "fridgeCapacity": 500.0, "normalCapacity": 2000.0,
  "addressLocale": "Zone Industrielle, Ben Arous" }
```
```json
POST /api/deliveries
{ "orderId": 42, "destination": "12 Avenue Habib Bourguiba, Tunis" }
```

## Structure
```
com.awd.logistics
├── config       OpenApiConfig (Swagger info)
├── controller   HelloController, DeliveryController, WarehouseController
├── dto          request/response records with validation + @Schema
├── entity       Delivery, Warehouse, DeliveryStatus
├── exception    GlobalExceptionHandler, ResourceNotFoundException
├── mapper       LogisticsMapper (entity <-> DTO)
├── repository   Spring Data JPA repositories
└── service      DeliveryService, WarehouseService
```

## Tests
`mvn test` runs integration tests (MockMvc) on in-memory H2 in MySQL mode, so MySQL is not needed.
