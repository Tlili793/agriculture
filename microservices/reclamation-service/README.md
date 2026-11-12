# MS6 · reclamation-service

Owner: **Hamza** · Port **8086** · Database **H2**

Consumers complain about an order. The admin answers and changes the status.

## Run

```bash
mvn spring-boot:run
```

Swagger: http://localhost:8086/swagger-ui.html
(Errors about Eureka 8761 / Config 8888 are normal while they are not running.)

## Endpoints

| Method | URI | What | Codes |
|---|---|---|---|
| POST | /api/reclamations | Create a complaint | 201, 400 |
| GET | /api/reclamations | List all | 200 |
| GET | /api/reclamations/{id} | Get one | 200, 404 |
| GET | /api/reclamations/consumer/{consumerId} | Complaints of one consumer | 200 |
| PUT | /api/reclamations/{id}/status | Admin changes status + answers | 200, 404, 409 |
| DELETE | /api/reclamations/{id} | Delete | 204, 404 |

## Communication (later sessions)

- **S6 sync (OpenFeign):** `GET /api/orders/{orderId}` on Ordering, to check the order exists.
- **S7 async (RabbitMQ):** publish `reclamation.resolved` on `platform.events`; Ordering refunds the order.
