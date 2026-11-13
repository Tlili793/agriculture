# logistics microservice

Spring Boot 3.2 / Java 17 service for the **Logistics domain** of the agriculture platform:
**Delivery** and **Warehouse**, with **H2**, **Eureka**, and **Swagger**.

The two tables are **independent**: no foreign key between `delivery` and `warehouse`, and
`delivery.order_id` is a plain number (the order belongs to the Ordering microservice).

## 1. Runtime

- Port: **8087**
- Eureka name: **`logistics-service`**
- Database: H2 file at `./data/logisticsdb` by default (Docker: `/app/data/logisticsdb`)
- Gateway prefix: `/api/logistics/**`, `/api/deliveries/**`, `/api/warehouses/**`

The service registers with Eureka as `logistics-service`. Through the gateway it is
reachable with a Bearer JWT at the paths above.

## 2. Run

```bash
mvn spring-boot:run
```

- Swagger UI: http://localhost:8087/swagger-ui.html
- Unified Swagger UI: http://localhost:8081/swagger-ui.html (select **Logistics Service**)
- OpenAPI JSON: http://localhost:8087/v3/api-docs
- Hello endpoint: http://localhost:8087/api/logistics/hi -> `Hi, from Ines`

Environment variables:

| Variable | Default | Docker value |
|---|---|---|
| `PORT` | `8087` | `8087` |
| `DATABASE_URL` | `jdbc:h2:file:./data/logisticsdb` | `jdbc:h2:file:/app/data/logisticsdb` |
| `DATABASE_USERNAME` | `sa` | `sa` |
| `DATABASE_PASSWORD` | empty | empty |
| `H2_CONSOLE_ENABLED` | `false` | `false` |
| `EUREKA_SERVER_URL` | `http://localhost:8761/eureka` | `http://eureka:8761/eureka` |
| `EUREKA_INSTANCE_HOST` | `localhost` | `logistics-service` |
| `EUREKA_PREFER_IP_ADDRESS` | `false` | `false` |

## 3. Authorization

Authorization is enforced by the gateway.

| Endpoint | ADMIN | TRANSPORTER | Other authenticated users |
|---|---|---|---|
| `GET /api/deliveries`, `GET /api/deliveries/{id}` | yes | yes | no |
| `POST/PUT/PATCH/DELETE /api/deliveries/**` | yes | no | no |
| `POST /api/deliveries/{id}/assignment-request` | no | yes, own Keycloak `sub` | no |
| `POST /api/deliveries/{id}/assignment-accept` | yes | no | no |
| `/api/warehouses/**` | yes | no | no |
| `GET /api/logistics/hi` | yes | yes | yes |

## 4. Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/logistics/hi` | Greeting: "Hi, from Ines" |
| GET | `/api/deliveries?status=&orderId=` | List deliveries (filters optional) |
| GET | `/api/deliveries/{id}` | Get one delivery |
| POST | `/api/deliveries` | Create delivery (status defaults to PENDING) |
| PUT | `/api/deliveries/{id}` | Update delivery |
| PATCH | `/api/deliveries/{id}/status?status=DISPATCHED` | Change status (DISPATCHED sets dispatchedAt) |
| DELETE | `/api/deliveries/{id}` | Delete delivery |
| POST | `/api/deliveries/{id}/assignment-request` | Transporter asks to be assigned |
| POST | `/api/deliveries/{id}/assignment-accept` | Admin accepts the pending request |
| GET | `/api/warehouses` | List warehouses |
| GET | `/api/warehouses/{id}` | Get one warehouse |
| POST | `/api/warehouses` | Create warehouse |
| PUT | `/api/warehouses/{id}` | Update warehouse |
| DELETE | `/api/warehouses/{id}` | Delete warehouse |

Delivery statuses: `PENDING`, `DISPATCHED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`.

Errors: `400` validation / bad value, `404` not found, `409` assignment conflict
(RFC 7807 JSON).

## 5. Assignment workflow

A transporter requests the delivery with their own Keycloak subject:

```json
POST /api/deliveries/42/assignment-request
{ "transporterId": "b0cef70a-5190-400b-a4f7-590285be1065" }
```

The gateway rejects the request unless `transporterId` equals the JWT `sub`.
Only one pending request is kept per delivery. The admin then accepts it:

```
POST /api/deliveries/42/assignment-accept
```

The response contains `requestedTransporterId` and `assignmentRequestedAt` while a request
is pending, and `assignedTransporterId` and `assignedAt` after acceptance.

## 6. Examples

```json
POST /api/warehouses
{ "name": "Tunis Central Warehouse", "fridgeCapacity": 500.0, "normalCapacity": 2000.0,
  "addressLocale": "Zone Industrielle, Ben Arous" }
```

```json
POST /api/deliveries
{ "orderId": 42, "destination": "12 Avenue Habib Bourguiba, Tunis" }
```

## 7. Structure

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

`database/logistics_db.sql` is the legacy MySQL sample script and is not used by the
H2-based runtime.

## 8. Tests

`mvn test` runs integration tests (MockMvc) on in-memory H2 in MySQL mode. Eureka is
disabled in tests.
