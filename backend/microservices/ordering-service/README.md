# Ordering Service (MS3)

Spring Boot microservice for creating and querying orders. Orders belong to the
Keycloak user whose `sub` claim is sent as `userId`. The service stores order and
order-item data in its local database and publishes an event when an order is placed.

The previous MS1/MS2 lookups are no longer used. Until the product service is
available, the frontend supplies `productId` and `unitPrice` with each order item.

## Request flow

```text
React frontend
  -> POST /api/orders with Authorization: Bearer <JWT>
     payload includes userId = keycloak.tokenParsed.sub
  -> Gateway validates userId against the JWT sub
  -> ORDERING-SERVICE (Eureka load balancing)
     -> H2 database
     -> RabbitMQ platform.events / order.placed
```

The gateway rejects a create-order request when:

- the body is missing or is not valid JSON (`400`);
- `userId` is missing or is not a UUID (`400`);
- `userId` does not equal the JWT `sub` (`403`).

## Endpoints

### `POST /api/orders`

Creates an order for the authenticated user.

Request:

```json
{
  "userId": "6f1c2b7e-9a2d-4f5c-8b1e-3d4a5c6b7e8f",
  "items": [
    {
      "productId": 10,
      "quantity": 2,
      "unitPrice": 12.50
    }
  ]
}
```

The service computes `totalAmount = sum(unitPrice * quantity)`, saves the order
with status `PENDING`, and publishes `OrderPlacedEvent`. It returns `201 Created`
with a `Location` header and the created order.

Response:

```json
{
  "id": 42,
  "userId": "6f1c2b7e-9a2d-4f5c-8b1e-3d4a5c6b7e8f",
  "orderDate": "2026-11-12T10:15:30",
  "totalAmount": 25.00,
  "status": "PENDING",
  "consumer": {
    "id": "6f1c2b7e-9a2d-4f5c-8b1e-3d4a5c6b7e8f",
    "email": null,
    "fullName": null
  },
  "items": [
    {
      "id": 7,
      "productId": 10,
      "quantity": 2,
      "unitPrice": 12.50,
      "product": {
        "id": 10,
        "name": "Product 10",
        "price": 12.50
      }
    }
  ]
}
```

`consumer` and `product` are temporary response shapes. The consumer fields other
than `id` are `null`, and the product name is derived from `productId`.

### `GET /api/orders/{id}`

Returns an order by its local numeric ID, or `404` when it does not exist.

### `GET /api/orders/user/{userId}`

Returns the user's orders, newest first. `userId` is the Keycloak UUID.

## Data model

| Entity | Fields | Notes |
| --- | --- | --- |
| `Order` | `id`, `userId` (UUID), `orderDate`, `totalAmount`, `status`, `items` | Status is `PENDING`, `COMPLETED`, or `CANCELLED`. |
| `OrderItem` | `id`, `productId`, `quantity`, `unitPrice`, `order` | `unitPrice` is currently supplied by the frontend. |
| `Cart` | `id`, `userId` (UUID), `createdAt` | Present in the domain but not exposed by any endpoint. |

## Events

After a successful save, the service publishes to RabbitMQ:

- Exchange: `platform.events`
- Routing key: `order.placed`
- Payload: `OrderPlacedEvent`

```json
{
  "eventId": "b7d8e9f0-1a2b-4c3d-8e4f-5a6b7c8d9e0f",
  "orderId": 42,
  "userId": "6f1c2b7e-9a2d-4f5c-8b1e-3d4a5c6b7e8f",
  "totalAmount": 25.00
}
```

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8083` | HTTP port. Docker Compose uses `8084`. |
| `DATABASE_URL` | `jdbc:h2:mem:orderingdb` | JDBC URL. |
| `DATABASE_USERNAME` | `sa` | Database user. |
| `DATABASE_PASSWORD` | empty | Database password. |
| `H2_CONSOLE_ENABLED` | `false` | Enables the H2 console. |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host. |
| `RABBITMQ_PORT` | `5672` | RabbitMQ port. |
| `RABBITMQ_USERNAME` | `guest` | RabbitMQ user. |
| `RABBITMQ_PASSWORD` | `guest` | RabbitMQ password. |
| `EUREKA_SERVER_URL` | `http://localhost:8761/eureka` | Eureka registry. |
| `EUREKA_INSTANCE_HOST` | `localhost` | Hostname registered with Eureka. |
| `EUREKA_PREFER_IP_ADDRESS` | `true` | Whether Eureka should prefer the IP address. |

The root Docker Compose stack sets the port to `8084` and uses the dedicated
`agriculture` RabbitMQ user because the default `guest` account is restricted to
local connections.

## Run locally

Prerequisites: Java 17, Maven, a running Eureka server, and RabbitMQ.

From the repository root:

```bash
mvn -f backend/microservices/ordering-service/pom.xml spring-boot:run
```

From the service directory:

```bash
mvn spring-boot:run
```

The service listens on `8083` by default and uses an in-memory H2 database. Data is
lost when the process stops. MS1/MS2 are not required.

## Run with Docker Compose

The root `docker-compose.yml` starts this service as `ordering-service` with:

- host port `8084:8084`;
- Eureka at `http://eureka:8761/eureka`;
- RabbitMQ at `rabbitmq:5672` with user/password `agriculture`;
- the gateway route `/api/orders/**` targeting `lb://ORDERING-SERVICE`.

```bash
docker compose up --build
```

## Testing

```bash
mvn -f backend/microservices/ordering-service/pom.xml test
```

## Error responses

| Status | Cause |
| --- | --- |
| `400` | Request validation failed, or the gateway rejected a missing/invalid `userId`. |
| `401` | The gateway did not receive a valid JWT. |
| `403` | The payload `userId` does not match the JWT `sub`. |
| `404` | The requested order does not exist. |

## Notes and limitations

- Client-supplied `unitPrice` is temporary. Product pricing should come from the
  product service when it is restored.
- The service itself does not validate JWTs. Authentication and the `userId`
  consistency check happen at the gateway.
- The gateway validates `userId` only for `POST /api/orders`. Read endpoints use the
  `userId` path variable.
- H2 is in-memory; there is no persistent order storage or migration tooling.
- The `Cart` entity is unused by the current API.
