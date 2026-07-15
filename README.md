# Job Tracker

[![CI](https://github.com/miss-roy/JobTracker/actions/workflows/ci.yml/badge.svg)](https://github.com/miss-roy/JobTracker/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

A **Spring Boot microservices** project with a **React + Vite** frontend,
focused on backend engineering. Track job applications across statuses
(*Applied, Interviewing, Offered, Rejected, Ghosted*), view a pie-chart
breakdown, and add applications from a popup form.

---

## Architecture

```
                 ┌─────────────────────┐
  Browser  ──▶   │  frontend (nginx)    │  :3000
                 │  React + Vite        │
                 └──────────┬───────────┘
                            │  /api/**  (proxied)
                            ▼
                 ┌─────────────────────┐
                 │  api-gateway         │  :8080   Spring Cloud Gateway
                 └──────────┬───────────┘
                            │  lb://job-service (resolved via Eureka)
                            ▼
                 ┌─────────────────────┐
                 │  job-service         │  :8081   Spring Web + JPA + H2
                 └──────────┬───────────┘
                            │ registers / discovers
                            ▼
                 ┌─────────────────────┐
                 │  discovery-server    │  :8761   Netflix Eureka
                 └─────────────────────┘
```

- **discovery-server** — Eureka registry. Services register here; the gateway
  uses it to find `job-service` without hard-coded hosts/ports.
- **api-gateway** — single entry point. Routes `/api/**` to `job-service`.
- **job-service** — the only service that owns domain data. Classic layered
  Spring Boot: `controller → service → repository → H2`.
- **frontend** — React SPA; calls relative `/api/...`, proxied to the gateway.

---

## Tech stack & why

### Backend (100% Spring Boot)
| Tech | Why it's here |
|------|---------------|
| **Java 21** | LTS; uses `record` DTOs and text blocks for JPQL. |
| **Spring Web** | REST controllers. |
| **Spring Data JPA** | CRUD with almost no boilerplate via `JpaRepository`. |
| **H2 (in-memory)** | Zero-setup database; data reseeded on each boot. |
| **Spring Cloud Gateway** | API gateway / routing (the "micro" in microservices). |
| **Netflix Eureka** | Service discovery so services find each other by name. |
| **Maven (multi-module)** | One reactor build; each service independently deployable. |

**Supporting libraries (all Spring-native):**
| Library | What it provides | Optional? |
|---------|------------------|-----------|
| **Lombok** | Removes getter/setter/builder boilerplate on the entity. | Yes — getters/setters can be written by hand. |
| **Bean Validation** (`spring-boot-starter-validation`) | `@NotBlank`/`@NotNull` on request DTOs; returns 400 on invalid input. | Yes — validation could be done manually. |
| **springdoc-openapi** | Generates **Swagger UI** at `/swagger-ui.html` to exercise the API in a browser. | Yes — curl/Postman work instead. |
| **RFC-7807 ProblemDetail** | Standard JSON error bodies (built into Spring 6). | Built-in; no extra dependency. |

### Frontend
| Tech | Why |
|------|-----|
| **React + Vite + TypeScript** | Fast dev server, typed, runs in Safari on **macOS and iOS** (responsive phone-width layout). |
| **Recharts** | The status pie chart. |
| **nginx** (in Docker) | Serves the build and proxies `/api` to the gateway. |

> **macOS + iOS:** built as a responsive **web app** that runs in Safari on
> both. To ship it as a native installable iOS/macOS app, the same codebase
> wraps with **Capacitor** (`npm i @capacitor/core @capacitor/ios`) — no
> rewrite required.

---

## API (job-service, via the gateway at `:8080`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/jobs` | All applications |
| GET | `/api/jobs?status=APPLIED` | Filter by status (drives each tab) |
| GET | `/api/jobs/stats` | Count per status (pie chart) |
| GET | `/api/jobs/{id}` | One application |
| POST | `/api/jobs` | Create (`company`, `status`, `dateApplied`, `hrContact`) |
| PUT | `/api/jobs/{id}` | Update |
| DELETE | `/api/jobs/{id}` | Delete |

---

## Running it

### Prerequisites
- **JDK 21** + **Maven 3.9+** — to build/run the services locally.
  (Not required if using Docker — the images build Maven inside the container.)
- **Docker** — to run the full stack with `docker compose`.
- **Node 22+** — for the frontend dev server.

### Option A — Docker (recommended, no local JDK/Maven needed)
```bash
docker compose up --build
```
Then open:
- App → http://localhost:3000
- Eureka dashboard → http://localhost:8761
- Swagger UI → http://localhost:8081/swagger-ui.html

### Option B — run locally (needs JDK 21 + Maven)
In separate terminals, **in this order**:
```bash
mvn -pl discovery-server spring-boot:run   # wait until it's up (:8761)
mvn -pl api-gateway     spring-boot:run     # :8080
mvn -pl job-service     spring-boot:run     # :8081
```
Then the frontend:
```bash
cd frontend
npm install
npm run dev                                 # http://localhost:3000
```
The Vite dev server proxies `/api` to the gateway on `:8080`.

### Build / test everything
```bash
mvn clean package        # builds all three services, runs job-service tests
```

---

## Deployment
To host this publicly (with HTTPS), see **[DEPLOY.md](DEPLOY.md)**. It covers a
single-server Docker deployment (`docker-compose.prod.yml` + `Caddyfile`, full
architecture) and a free split-hosting option. Secrets are supplied via a `.env`
file (template in [`.env.example`](.env.example)).

## Project layout
```
JobTracker/
├── pom.xml                 # aggregator (reactor) POM
├── docker-compose.yml      # local/dev stack
├── docker-compose.prod.yml # production stack (secrets via .env, HTTPS via Caddy)
├── Caddyfile               # reverse proxy + automatic HTTPS
├── DEPLOY.md               # hosting guide
├── discovery-server/       # Eureka
├── api-gateway/            # Spring Cloud Gateway
├── job-service/            # Web + JPA + Postgres (the domain service + tests)
└── frontend/               # React + Vite + Recharts (+ Capacitor iOS)
```

---
---

# Phase 2 — Scalability, Load Balancing & Resilience

**What Phase 2 is about (in a nutshell):** Phase 1 delivered a working
microservices app. Phase 2 makes it behave like a *production* distributed
system — it can now run **multiple copies of each service**, **spread traffic
across them** (at two layers), and **survive a backend going down** without
hanging or cascading failures. Crucially, scaling out is now a runtime flag
(`--scale`), not a code change, so future growth needs no re-architecting.

The three themes:
- **Scalability** — run N replicas of `job-service` / `api-gateway` on demand.
- **Load balancing** — server-side (an nginx *edge* in front of the gateways)
  + client-side (gateways round-robin to job-service via Eureka).
- **Resilience** — a Resilience4j *circuit breaker* in the gateway with a fast
  fallback, so a dead/slow backend fails gracefully and recovers automatically.

> **Additive update.** Everything above still applies. This phase hardened the
> system into a genuinely scalable, fault-tolerant distributed setup — without
> changing the application's behavior or API. New capabilities are demonstrable
> at runtime; adding more instances is now a one-flag change, not a rebuild.

## What this phase added

| Capability | Before | After |
|---|---|---|
| Horizontal scaling | single instance per service (pinned) | `job-service` & `api-gateway` run **N replicas** |
| Client-side LB (gateway → job-service) | wired (`lb://`) but only 1 target | round-robins across all `job-service` replicas (Eureka) |
| Server-side LB (client → gateway) | none | **edge nginx** round-robins across `api-gateway` replicas |
| Resilience | a dead backend would hang/cascade | **Resilience4j circuit breaker** + fast fallback in the gateway |

## Updated topology

```
Browser / iOS / curl  ── :8080 ──▶  edge (nginx)         # server-side LB
                                       │  round-robin
                          ┌────────────┴────────────┐
                          ▼                          ▼
                    api-gateway-1              api-gateway-2     # N gateway replicas
                    (circuit breaker +         (circuit breaker)  each w/ client-side LB
                     client-side LB)
                          │   lb://job-service (Eureka round-robin)
              ┌───────────┼───────────┐
              ▼           ▼           ▼
        job-service-1   -2          -3        # N stateless replicas
              └───────────┴───────────┘
                          ▼
                     Postgres (volume)         # Eureka coordinates discovery
```

## Changes by file

**Scalability**
- `docker-compose.yml` — removed `container_name` and host-port mappings from
  `job-service` and `api-gateway` (those pin a service to one container; removing
  them allows replicas). Singletons (`postgres`, `discovery-server`) keep names.
- `job-service` & `api-gateway` `application.yml` — added
  `eureka.instance.instance-id: ${spring.application.name}:${random.value}` so
  replicas register distinctly instead of colliding on host:port.

**Load balancing**
- `edge/nginx.conf` (new) + `edge` service in compose — nginx on public `:8080`
  that re-resolves the `api-gateway` service name per request (Docker DNS
  `resolver` + variable `proxy_pass`) to spread traffic across gateway replicas.
- `frontend/nginx.conf` — `/api` now proxies to `edge` (so browser traffic also
  flows through the load-balanced gateways).
- `job-service` `InstanceHeaderFilter.java` (new) — adds `X-Served-By` header.
- `api-gateway` `GatewayInstanceFilter.java` (new) — adds `X-Gateway` header.
  (Both headers exist purely to *observe* which replica answered.)

**Resilience (Resilience4j)**
- `api-gateway/pom.xml` — added `spring-cloud-starter-circuitbreaker-reactor-resilience4j`.
- `api-gateway/application.yml` — route wrapped in a `CircuitBreaker` filter
  (`jobServiceCB`, `fallbackUri: forward:/fallback/jobs`); Resilience4j tuned to
  open at 50% failure rate over a 10-call window, 4s per-call timeout, 10s open
  state with automatic half-open recovery.
- `api-gateway` `FallbackController.java` (new) — returns a clean `503` when the
  breaker is open, so requests fail fast instead of hanging.

> **Why Resilience4j (not Log4j):** they solve different problems. Resilience4j
> is a *fault-tolerance* library (circuit breakers, retries, time limiters);
> Log4j is a *logging* library. A circuit breaker can't be built with a logging
> framework — logging only records that a failure happened. (Spring Boot's
> default logger is Logback, via SLF4J.)

## Running it scaled

```bash
# bring everything up with 3 job-service + 2 api-gateway replicas
docker compose up -d --build --scale job-service=3 --scale api-gateway=2

# scale further any time — no rebuild, no code change:
docker compose up -d --scale job-service=5 --scale api-gateway=3
```

## How to demonstrate each capability

```bash
# 1) Load balancing — watch which gateway + job-service answered each request
for i in $(seq 1 12); do
  curl -s -D - -o /dev/null http://localhost:8080/api/jobs/stats \
    | grep -iE "X-Gateway|X-Served-By"
  echo "---"
done

# 2) Which instances are registered
curl -s -H "Accept: application/json" http://localhost:8761/eureka/apps

# 3) Circuit breaker — stop the backend, observe fast 503 fallback, then recover
docker compose stop job-service
curl -i http://localhost:8080/api/jobs/stats     # -> 503 fallback (fails fast)
docker compose start job-service                  # auto-recovers within ~10s
```

## Notes / trade-offs
- `job-service` no longer publishes `:8081`, so **Swagger** isn't directly
  exposed while scaled. Use `docker compose run --service-ports job-service`
  (single instance) for Swagger UI access.
- The edge LB can look briefly skewed toward one gateway (nginx caches DNS ~1s);
  it evens out under sustained traffic. Traefik/Envoy give smoother per-request
  balancing in production.
- Still single-node where it's intentional: Eureka, Postgres. Production next
  steps would be HA Eureka, Postgres replicas/pooling, distributed tracing
  (Micrometer + Zipkin), and async messaging (Kafka) for inter-service events.

---
---

# Phase 3 — Production Deployment (Live on the Cloud)

**What Phase 3 is about (in a nutshell):** the application moves from running
locally to a **publicly accessible, secure, always-on deployment** on cloud
infrastructure. The full containerized microservices stack runs on a single
cloud virtual machine behind a reverse proxy that terminates HTTPS and is
reachable at a real domain.

**🔗 Live demo:** https://jobtracker09.duckdns.org

The themes:
- **Production configuration** — a hardened compose setup with secrets supplied
  from an environment file and a single public entry point.
- **Cloud hosting** — the entire stack deployed to a Google Cloud virtual machine.
- **Domain + HTTPS** — a public domain served over an automatically issued and
  renewed TLS certificate.

> **Additive update.** Everything from Phase 1 and Phase 2 still applies. This
> phase adds deployment artifacts and a live environment without changing the
> application code or the API.

## What this phase added

| Area | Before | After |
|---|---|---|
| Runtime | Local Docker only | **Live on a Google Cloud VM**, always-on |
| Access | `localhost` | Public URL: **https://jobtracker09.duckdns.org** |
| Transport | HTTP | **HTTPS** (auto-issued, auto-renewing certificate) |
| Secrets | inline in compose | **Environment file** (`.env`), never committed |
| Exposure | all ports published | **Only the reverse proxy is public**; database, registry, gateway and services stay on the internal network |
| Resilience | manual start | **Automatic restart** on crash or reboot |

## Deployment topology

```
Browser ──HTTPS──▶ Caddy (reverse proxy, automatic TLS)   :443
                      │
                      ▼
                  frontend (nginx, serves the SPA)
                      │  /api
                      ▼
                  edge (load balancer)
                      │
                      ▼
                  api-gateway ──▶ job-service ──▶ PostgreSQL
                      ▲                 │
                      └───── Eureka ◀────┘
       (all containers run on one cloud VM; only the reverse proxy is exposed)
```

## What was done in this phase

- **Production compose (`docker-compose.prod.yml`)** — environment-driven
  secrets, no publicly exposed database/registry/gateway ports, automatic
  container restart, and a reverse proxy as the sole public service.
- **Reverse proxy (`Caddyfile`)** — Caddy fronts the stack, serving the frontend
  and forwarding API traffic internally, with automatic HTTPS.
- **Cloud deployment** — the full stack was containerized and deployed to a
  Google Cloud Compute Engine virtual machine (Ubuntu + Docker), running every
  service: reverse proxy, frontend, edge load balancer, API gateway, Eureka
  registry, job-service, and PostgreSQL.
- **Public domain** — a free dynamic-DNS subdomain (DuckDNS) resolves to the
  server's public IP.
- **Automatic TLS** — an HTTPS certificate is issued and renewed automatically
  (Let's Encrypt via Caddy), with HTTP redirected to HTTPS.
- **Live demo data** — sample records are seeded so the public site is populated.
- **Deployment guide (`DEPLOY.md`)** — documents single-server and split-hosting
  options, with secrets supplied via `.env` (template in `.env.example`).
- **Repository polish** — MIT license, README status badges, and a continuous
  integration workflow that builds and tests the backend and frontend.
- **UI refinement** — the status tabs use a single-row horizontal scroller with
  an edge-fade cue indicating more tabs are scrollable, applied to both the web
  and iOS clients.

## Notes / trade-offs

- The live environment runs on a single virtual machine; production-grade next
  steps would include multiple VMs behind a managed load balancer, a managed
  PostgreSQL instance, and a custom domain.
- The cloud free-credit period is time-limited; the environment can be resized,
  migrated to a cheaper host, or taken down afterward.
- HTTPS depends on a domain; a dynamic-DNS subdomain is used here and can be
  swapped for a custom domain without code changes.

---
---

# Phase 4 — Authentication & Per-User Data

**What Phase 4 is about (in a nutshell):** the application becomes **multi-user**.
Each person signs up, logs in, and gets a **private board** — their own job
applications only. Every request is authenticated with a JSON Web Token (JWT),
and every database query is scoped to the signed-in user, so no one can see or
touch anyone else's data.

The themes:
- **Authentication** — register/login with JWT-based, stateless security.
- **Authorization / isolation** — jobs are owned by a user; queries are
  scoped so users only ever access their own records.
- **Same experience on web and iOS** — both clients share one authenticated
  backend.

> **Additive update.** Everything from Phases 1–3 still applies. The public API
> is now protected: unauthenticated requests receive `401`.

## What this phase added

| Area | Before | After |
|---|---|---|
| Access | open API — anyone could read/write all jobs | **login required**; unauthenticated → `401` |
| Data | one shared list for everyone | **private per user** — each account sees only its own jobs |
| Identity | none | `users` table; **BCrypt**-hashed passwords |
| Sessions | none | **stateless JWT** (Bearer token), 24h expiry |
| Clients | — | login/register screen on **web and iOS** |

## How a request is authenticated

```
Login ─▶ POST /api/auth/login ─▶ returns a JWT
Then every call carries:  Authorization: Bearer <token>
        │
        ▼
  JWT filter validates the token ─▶ sets the current user
        │
        ▼
  queries run as  WHERE user_id = <current user>   (data isolation)
```

## What was done in this phase

- **Spring Security + JWT** (`jjwt`) — stateless authentication; a filter reads
  the `Authorization: Bearer` header, validates the token, and sets the
  authenticated principal. Missing/invalid tokens return `401`.
- **User accounts** — a `users` table (Flyway `V2`) with **BCrypt**-hashed
  passwords; `register` and `login` endpoints return a signed JWT.
- **Ownership & isolation** — jobs gain a `user_id` owner column; every service
  and repository method is scoped by the current user, and ownership is checked
  on read/update/delete so accounts are fully separated.
- **Graceful errors** — `401` for missing/invalid tokens, `401` for bad
  credentials, `409` for a taken username.
- **Demo account** — the seeder provisions a `demo` account (with a sample
  board) and adopts any pre-auth jobs into it, so an existing database keeps its
  data through the upgrade.
- **Clients** — a login/register screen on both the **web** and **iOS** apps;
  the JWT is stored client-side and sent as a Bearer token, with automatic
  logout when the token expires.
- **Tests** — cover the auth flow and verify cross-user isolation (one user
  cannot see another's jobs).

## Notes / trade-offs

- The JWT signing secret is supplied via `JWT_SECRET` (never committed).
- Auth and user storage live in `job-service` alongside the domain; a dedicated
  auth service is a possible future refinement.
- Natural next steps: token refresh, OAuth2 social login, and roles/permissions.
