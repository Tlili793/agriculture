# Agriculture Microservices Platform

A development-oriented agriculture platform built around a Spring Cloud gateway,
Eureka service discovery, Keycloak authentication, React frontend, and five
domain services.

The platform currently provides:

- Refrigerator inventory management
- Ordering with RabbitMQ messaging
- Reclamation handling
- Logistics, deliveries, and warehouses
- Marketplace listings
- Keycloak-backed browser login
- Eureka-based service discovery
- A single gateway entry point and consolidated Swagger UI

This README describes the stack as it currently runs. In particular, gateway
authentication is configured but **not enforced** yet; see
[Gateway security](#gateway-security).

## Contents

- [Architecture](#architecture)
- [Services and ports](#services-and-ports)
- [Project layout](#project-layout)
- [Keycloak](#keycloak)
- [Eureka](#eureka)
- [Gateway](#gateway)
- [Frontend](#frontend)
- [Running the stack](#running-the-stack)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Current limitations](#current-limitations)

## Architecture

```text
Browser
   |
   |-- http://keycloak.localhost:8080  (login, tokens)
   |
   `-- http://localhost:5173  (React frontend)
             |
             |-- /api/*, /refrigerator/*  (Nginx or Vite proxy)
             v
       http://localhost:8081  (Spring Cloud Gateway)
             |
             |-- Eureka service discovery (lb://...)
             |
             |-- REFRIGERATOR-SERVICE  :8083
             |-- ORDERING-SERVICE      :8084
             |-- RECLAMATION-SERVICE   :8086
             |-- LOGISTICS-SERVICE     :8087
             `-- MARKETPLACE           :8030

Eureka dashboard and registry: http://localhost:8761
Keycloak admin console:        http://keycloak.localhost:8080/admin
Swagger UI:                    http://localhost:8081/swagger-ui.html
```

The gateway resolves service IDs through Eureka using `lb://` URIs. Keycloak is
a separate identity service. Service-to-service calls and inter-container URLs
use Docker Compose service names such as `eureka`, `gateway`, and `keycloak`.

## Services and ports

| Component | Eureka name | Host port | Data store | Health/check |
| --- | --- | --- | --- | --- |
| Frontend | n/a | `5173` (container port `80`) | n/a | Container starts after gateway is healthy |
| Gateway | `GATEWAY` | `8081` | n/a | `GET /public/hello` |
| Eureka Server | `eureka-server` (not self-registered) | `8761` | n/a | `GET /actuator/health` |
| Keycloak | n/a | `8080` | PostgreSQL 16 | Internal health port `9000`; OIDC discovery on `8080` |
| PostgreSQL | n/a | internal `5432` only | Keycloak database | `pg_isready` |
| RabbitMQ | n/a | `5672`, management `15672` | RabbitMQ volume | `rabbitmq-diagnostics ping` |
| Refrigerator | `REFRIGERATOR-SERVICE` | `8083` | H2 file | `GET /actuator/health` |
| Ordering | `ORDERING-SERVICE` | `8084` | H2 in-memory | No actuator health endpoint |
| Reclamation | `RECLAMATION-SERVICE` | `8086` | H2 file | `GET /actuator/health` |
| Logistics | `LOGISTICS-SERVICE` | `8087` | H2 file | `GET /actuator/health` |
| Marketplace | `MARKETPLACE` | `8030` | H2 file | `GET /actuator/health` |

Service names are case-insensitive in Eureka, but the gateway route URIs use the
uppercase IDs shown above.

Development credentials:

- Keycloak bootstrap admin: `admin` / `admin`
- Demo Keycloak user from the realm export: `demo` / `demo`
- RabbitMQ: `agriculture` / `agriculture`

These are development defaults only. Change them before using the stack outside
a local environment.

## Project layout

```text
.
├── backend/
│   ├── src/main/java/com/workshop/gateway/   # Gateway application
│   ├── src/main/resources/application.yaml   # Routes and OAuth2 configuration
│   ├── eureka-server/                        # Eureka registry
│   ├── keycloak/realm-export.json            # Keycloak import
│   ├── microservices/
│   │   ├── refrigerator-service/
│   │   ├── ordering-service/
│   │   ├── reclamation-service/
│   │   ├── ms_logistics/                     # Logistics service module
│   │   └── marketplace/
│   └── docker-compose.infra.yml              # Keycloak/Postgres/Eureka only
├── frontend/                                 # React + Vite + keycloak-js
├── docker-compose.yml                        # Full platform
└── README.md
```

## Keycloak

Keycloak runs in development mode with the `agriculture` realm imported from
`backend/keycloak/realm-export.json`.

### URLs

The browser-facing issuer and OIDC endpoints use the `keycloak.localhost`
hostname:

| Purpose | URL |
| --- | --- |
| Admin console | `http://keycloak.localhost:8080/admin` |
| Realm issuer | `http://keycloak.localhost:8080/realms/agriculture` |
| Authorization | `http://keycloak.localhost:8080/realms/agriculture/protocol/openid-connect/auth` |
| Token | `http://keycloak.localhost:8080/realms/agriculture/protocol/openid-connect/token` |
| User info | `http://keycloak.localhost:8080/realms/agriculture/protocol/openid-connect/userinfo` |
| JWKS | `http://keycloak.localhost:8080/realms/agriculture/protocol/openid-connect/certs` |
| Logout | `http://keycloak.localhost:8080/realms/agriculture/protocol/openid-connect/logout` |

Inside Docker, the gateway uses the internal JWKS URL
`http://keycloak:8080/realms/agriculture/protocol/openid-connect/certs` through
`KEYCLOAK_JWK_SET_URI`. The public issuer remains `keycloak.localhost`.

`keycloak.localhost` normally resolves through Docker network aliases. If the
browser cannot resolve it, add an `/etc/hosts` entry pointing it to `127.0.0.1`
or use the hostname that matches `KC_HOSTNAME`.

### Clients

| Client | Type | Flow | Used by |
| --- | --- | --- | --- |
| `react-client` | Public SPA client | Authorization Code + PKCE | React frontend |
| `gateway-client` | Confidential client | Authorization Code | Gateway OAuth2 client configuration |

Direct access grants are disabled for these clients. The frontend initiates login
with `keycloak-js` using `login-required` and PKCE (`S256`), and refreshes the
token while the session is active.

Redirect URIs are configured for local frontend and gateway development URLs,
including `http://localhost:5173` and `http://localhost:8081`. If the frontend is
served from another origin, add its callback URL to `react-client` in the admin
console.

The gateway client secret is supplied through `KEYCLOAK_CLIENT_SECRET`. Replace
the development default and do not commit a production secret.

### Roles and users

The current live development realm contains these custom realm roles:

| Role | Intended use |
| --- | --- |
| `ADMIN` | Administrative UI and domain operations |
| `USER` | General authenticated user |
| `FARMER` | Farmer-specific features |
| `TRANSPORTER` | Logistics/transporter features |

The frontend reads roles from `realm_access.roles` and currently uses `ADMIN` and
`TRANSPORTER` for UI decisions.

The checked-in `realm-export.json` defines the realm, clients, and demo user, but
it does not define custom role entries or role assignments beyond the export
contents. After wiping the Keycloak database, recreate the custom roles and
assignments in the admin console if they are required.

## Eureka

Eureka is the service registry used by the gateway and all domain services.

| Setting | Value |
| --- | --- |
| Dashboard | `http://localhost:8761/` |
| Health | `http://localhost:8761/actuator/health` |
| Apps API | `http://localhost:8761/eureka/apps` |
| Self-registration | Disabled |
| Registry fetch (server) | Disabled |
| Self-preservation | Disabled |
| Response cache update | `1000 ms` |
| Eviction interval | `5000 ms` |

The Eureka server itself does not register as a client. Domain services and the
gateway register with `EUREKA_SERVER_URL`, which is
`http://eureka:8761/eureka` inside Compose.

Expected registered applications:

```text
GATEWAY
REFRIGERATOR-SERVICE
ORDERING-SERVICE
RECLAMATION-SERVICE
LOGISTICS-SERVICE
MARKETPLACE
```

Clients fetch the registry every 5 seconds and renew leases every 10 seconds.
The gateway prefers DNS/hostnames rather than IP addresses for registered
instances. Self-preservation is disabled to make local development fail fast;
this is not a production recommendation.

To inspect registrations:

```bash
curl -H 'Accept: application/json' http://localhost:8761/eureka/apps
```

## Gateway

The gateway is the single HTTP entry point for application traffic on port
`8081`. It uses Spring Cloud Gateway Server WebFlux and Eureka load balancing.

### Routes

| Path | Target | Notes |
| --- | --- | --- |
| `/refrigerator/**` | `lb://REFRIGERATOR-SERVICE` | No rewrite |
| `/api/orders/**` | `lb://ORDERING-SERVICE` | No rewrite |
| `/api/reclamations/**` | `lb://RECLAMATION-SERVICE` | No rewrite |
| `/api/logistics/**`, `/api/deliveries/**`, `/api/warehouses/**` | `lb://LOGISTICS-SERVICE` | No rewrite |
| `/api/marketplace/**` | `lb://MARKETPLACE` | No rewrite; marketplace controllers already use `/api/marketplace` |
| `/services/refrigerator/**` | `lb://REFRIGERATOR-SERVICE` | Swagger/doc proxy with prefix rewrite |
| `/services/ordering/**` | `lb://ORDERING-SERVICE` | Swagger/doc proxy with prefix rewrite |
| `/services/reclamation/**` | `lb://RECLAMATION-SERVICE` | Swagger/doc proxy with prefix rewrite |
| `/services/logistics/**` | `lb://LOGISTICS-SERVICE` | Swagger/doc proxy with prefix rewrite |
| `/services/marketplace/**` | `lb://MARKETPLACE` | Swagger/doc proxy with prefix rewrite |
| `/api/**` | `${AGRICULTURE_SERVICE_URI}` | Legacy fallback route |

The service routes use a higher precedence than the `/api/**` fallback. The
fallback defaults to `http://localhost:8082` locally and
`http://host.docker.internal:8082` in Compose. No service in the standard
Compose stack listens there, so the fallback is only relevant when connecting a
legacy external service.

### Swagger and OpenAPI

Swagger UI is available through the gateway:

- UI: `http://localhost:8081/swagger-ui.html`
- Alternate UI entry: `http://localhost:8081/docs`

The UI loads these gateway-relative specs:

- `/services/refrigerator/v3/api-docs`
- `/services/ordering/v3/api-docs`
- `/services/reclamation/v3/api-docs`
- `/services/logistics/v3/api-docs`
- `/services/marketplace/v3/api-docs`

The service OpenAPI documents use a gateway-relative server URL (`/`), so
"Try it out" requests from the consolidated UI are sent through the gateway. For
marketplace, use the public gateway path such as
`/api/marketplace/listings`, not a `/mic3` path.

### Gateway security

The current gateway security configuration is intentionally permissive:

- CSRF is disabled.
- Every exchange is permitted: `anyExchange().permitAll()`.
- No route currently attaches a JWT validation or user-validation filter.
- Swagger and all routed APIs are publicly accessible.

The gateway does contain OAuth2 client registration, resource-server JWKS
settings, `KeycloakJwtAuthenticationConverter`, and validation filter factory
classes. They are not wired into the active security chain, so requests are not
currently authenticated or rejected because of a missing or invalid token.

Treat this deployment as an open development gateway. Before exposing it, enable
a resource-server security chain, attach the intended route filters, and remove
the blanket `permitAll()` rule.

### Gateway environment variables

| Variable | Purpose | Default |
| --- | --- | --- |
| `KEYCLOAK_REALM_URL` | Public Keycloak realm URL | `http://localhost:8080/realms/agriculture` |
| `KEYCLOAK_JWK_SET_URI` | JWKS URL used by the gateway | Derived from the realm URL |
| `KEYCLOAK_CLIENT_SECRET` | Confidential `gateway-client` secret | Development default in Compose/config |
| `EUREKA_SERVER_URL` | Eureka registry URL | `http://localhost:8761/eureka` |
| `AGRICULTURE_SERVICE_URI` | Legacy fallback target for `/api/**` | `http://localhost:8082` |

Gateway routes and OAuth2 settings live in
`backend/src/main/resources/application.yaml`. Gateway security lives in
`backend/src/main/java/com/workshop/gateway/config/SecurityConfig.java`.

## Frontend

The frontend is a React 19 application built with Vite and `keycloak-js`.

- Development server: `http://localhost:5173`
- Container: Nginx serves the production build; host port `5173` maps to port `80`
- Nginx proxies `/api/` and `/refrigerator/` to `http://gateway:8081`
- Vite dev proxy forwards `/api` to `http://localhost:8081`

Key environment variables (see `frontend/.env.example`):

| Variable | Default |
| --- | --- |
| `VITE_KEYCLOAK_URL` | `http://keycloak.localhost:8080` |
| `VITE_KEYCLOAK_REALM` | `agriculture` |
| `VITE_KEYCLOAK_CLIENT_ID` | `react-client` |
| `VITE_REFRIGERATOR_ENDPOINT` | `/refrigerator/fruits` |
| `VITE_ORDERS_ENDPOINT` | `/api/orders` |
| `VITE_RECLAMATIONS_ENDPOINT` | `/api/reclamations` |
| `VITE_LOGISTICS_DELIVERIES_ENDPOINT` | `/api/deliveries` |
| `VITE_LOGISTICS_WAREHOUSES_ENDPOINT` | `/api/warehouses` |
| `VITE_MARKETPLACE_ENDPOINT` | `/api/marketplace/listings` |

The frontend requires a successful Keycloak login before rendering the
authenticated application shell. After login, the main page shows a service
selector and renders only the selected service panel: Refrigerator, Ordering,
Reclamations, Marketplace, and Logistics for admins/transporters.

Resource panels use tables for every exposed backend resource:

- Refrigerator: fruits, vegetables, meals, and monthly budgets.
- Ordering: the authenticated user's orders with expandable order items.
- Reclamations: consumer/admin list, status updates, and delete actions.
- Marketplace: all listings with owner-only edit/delete actions.
- Logistics: deliveries with expandable controls and separate warehouses.

Gateway authorization is still the current security limitation described above.

## Running the stack

### Prerequisites

- Docker with Docker Compose
- Java 17 and Node.js only if running components outside Docker

### Start everything

```bash
docker compose up -d --build
docker compose ps
```

Startup order is handled with health checks:

1. PostgreSQL becomes healthy.
2. Keycloak imports the realm and becomes healthy.
3. Eureka becomes healthy.
4. RabbitMQ becomes healthy.
5. Domain services register with Eureka.
6. Gateway waits for Keycloak, Eureka, and the refrigerator service.
7. Frontend waits for the gateway.

Open:

- Frontend: `http://localhost:5173`
- Keycloak admin: `http://keycloak.localhost:8080/admin`
- Eureka: `http://localhost:8761`
- Gateway Swagger: `http://localhost:8081/swagger-ui.html`
- RabbitMQ management: `http://localhost:15672`

The realm import runs only when the Keycloak database is empty. On later starts,
existing Keycloak data is preserved in the `postgres-data` volume.

### Logs and targeted rebuilds

```bash
docker compose logs -f gateway
docker compose logs -f keycloak
docker compose up -d --build gateway marketplace-service
```

### Stop or reset

```bash
docker compose down
docker compose down -v
```

`down -v` deletes database and H2 volumes. The realm export will be imported
again on the next start, but custom roles/users not present in the export must be
recreated.

### Infra-only Compose

`backend/docker-compose.infra.yml` starts only PostgreSQL and Keycloak. Use the
root `docker-compose.yml` for the full platform, including Eureka and all domain
services.

### Local frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to the gateway. The containerized Nginx
configuration also proxies `/refrigerator/`.

## Verification

Use these checks after startup:

```bash
# Eureka
curl -fsS http://localhost:8761/actuator/health
curl -fsS -H 'Accept: application/json' http://localhost:8761/eureka/apps

# Keycloak OIDC discovery
curl -fsS http://localhost:8080/realms/agriculture/.well-known/openid-configuration

# Gateway
curl -fsS http://localhost:8081/public/hello
curl -fsS -o /dev/null -w '%{http_code}\n' http://localhost:8081/swagger-ui.html
curl -fsS -o /dev/null -w '%{http_code}\n' http://localhost:8081/services/marketplace/v3/api-docs

# Service health through host ports
for port in 8030 8083 8086 8087; do
  printf '%s: ' "$port"
  curl -fsS -o /dev/null -w '%{http_code}\n' "http://localhost:$port/actuator/health"
done
```

Because gateway security currently permits all exchanges, application calls do
not require an `Authorization` header. A valid route should still return a
service response even without a token. Verify users and realm roles in the
Keycloak admin console when testing role-aware UI behavior.

## Troubleshooting

| Symptom | Likely cause | Check |
| --- | --- | --- |
| Keycloak login redirect fails | Redirect URI missing from `react-client` | Add the frontend/gateway URL in Keycloak |
| `keycloak.localhost` does not resolve | Hostname/alias missing | Add `/etc/hosts` entry or use matching `KC_HOSTNAME` |
| User has no expected roles | Realm was reset or role not assigned | Recreate/assign realm roles in the admin console |
| Gateway returns `503` | No healthy instance in Eureka | Check service health, registration, and gateway logs |
| Gateway returns `404` | Path does not match a route | Use `/api/marketplace/...`, `/services/...`, or the documented route prefixes |
| OpenAPI/Swagger request fails | Wrong service path or spec not served | Check `/services/{service}/v3/api-docs` and the service logs |
| Marketplace request uses `/mic3/...` | Old path removed | Use `/api/marketplace/...` |
| Eureka list is empty | Service cannot reach Eureka | Check `EUREKA_SERVER_URL` and service startup logs |
| Ordering service restarts | RabbitMQ unavailable or startup dependency failed | Check `docker compose logs ordering-service rabbitmq` |
| Keycloak role changes disappear | Database volume was reset | Recreate roles after `docker compose down -v` |

## Current limitations

- **Gateway authentication is not enforced.** OAuth2/Keycloak configuration
  exists, but the active security chain permits all requests and does not attach
  JWT or user-ownership filters.
- **Custom Keycloak roles are not in the realm export.** The live development
  realm has `ADMIN`, `USER`, `FARMER`, and `TRANSPORTER`, but a fresh import may
  need manual role recreation and assignment.
- **The legacy `/api/**` fallback has no service in the Compose stack.** It is
  retained for optional external/legacy integration.
- **Ordering uses in-memory H2 in Compose.** Its data is lost when the ordering
  container is recreated.
- **Marketplace POST responses may use an internal `Location` header.** Through
  the gateway, a created listing can return a location pointing at the internal
  `marketplace-service:8030` host. The response body and CRUD flow still work,
  but the header is not host-reachable.
- **Development defaults are embedded in Compose.** Change the Keycloak admin,
  demo, RabbitMQ, and `gateway-client` secrets before using this outside local
  development.

## Version notes

- Java 17
- Spring Boot 4.1.2
- Spring Cloud 2025.1.4
- Spring Cloud Gateway Server WebFlux 5.0.4
- Spring Security 7.1.2
- Keycloak 26.4.0
- PostgreSQL 16
- RabbitMQ 4 (management image)
- React 19 / Vite 7 / `keycloak-js` 26

Gateway 5 configuration uses the
`spring.cloud.gateway.server.webflux` namespace. Spring Boot 4 uses the explicit
starters `spring-cloud-starter-gateway-server-webflux`,
`spring-boot-starter-security-oauth2-client`, and
`spring-boot-starter-security-oauth2-resource-server`.
