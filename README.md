# Job Tracker

A small **Spring Boot microservices** project (backend refresher) with a
**React + Vite** frontend. Track job applications across statuses
(*Applied, Interviewing, Offered, Rejected, Ghosted*), see a pie-chart
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

### Backend (the focus — stays 100% Spring Boot)
| Tech | Why it's here |
|------|---------------|
| **Java 21** | LTS; uses `record` DTOs and text blocks for JPQL. |
| **Spring Web** | REST controllers. |
| **Spring Data JPA** | CRUD with almost no boilerplate via `JpaRepository`. |
| **H2 (in-memory)** | Zero-setup database; data reseeded on each boot. |
| **Spring Cloud Gateway** | API gateway / routing (the "micro" in microservices). |
| **Netflix Eureka** | Service discovery so services find each other by name. |
| **Maven (multi-module)** | One reactor build; each service independently deployable. |

**Extras I added (still Spring-native) — flagged because you asked me to:**
| Add-on | What it gives you | Could you drop it? |
|--------|-------------------|--------------------|
| **Lombok** | Removes getter/setter/builder boilerplate on the entity. | Yes — write the methods by hand. |
| **Bean Validation** (`spring-boot-starter-validation`) | `@NotBlank`/`@NotNull` on request DTOs, auto-400 on bad input. | Yes, but you'd validate manually. |
| **springdoc-openapi** | Auto **Swagger UI** at `/swagger-ui.html` to try the API in a browser. | Yes — use curl/Postman instead. |
| **RFC-7807 ProblemDetail** | Standard JSON error bodies (built into Spring 6). | It's built-in, no dependency. |

### Frontend
| Tech | Why |
|------|-----|
| **React + Vite + TypeScript** | Fast dev server, typed, runs in Safari on **macOS and iOS** (responsive phone-width layout). |
| **Recharts** | The status pie chart. |
| **nginx** (in Docker) | Serves the build and proxies `/api` to the gateway. |

> **About "macOS + iOS app":** this is built as a responsive **web app** (your
> selected option). It already runs in Safari on both. To ship it as a real
> installable iOS/macOS app later, wrap this same codebase with **Capacitor**
> (`npm i @capacitor/core @capacitor/ios`) — no rewrite needed.

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

### Prerequisites (all present on this machine)
- **JDK 21** — installed via SDKMAN (`~/.sdkman/candidates/java/21-tem`).
  If `java -version` shows 15, run `sdk use java 21-tem` (or `sdk default java 21-tem`).
- **Maven 3.9+** — installed via SDKMAN (`sdk install maven` already done).
- **Docker** — installed; start Docker Desktop before `docker compose`.
- **Node 22+** — for the frontend.

> Verified: `mvn clean package` → BUILD SUCCESS (all 3 services, 4 tests pass);
> `npm run build` in `frontend/` → succeeds.

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

## Project layout
```
JobTracker/
├── pom.xml                 # aggregator (reactor) POM
├── docker-compose.yml
├── discovery-server/       # Eureka
├── api-gateway/            # Spring Cloud Gateway
├── job-service/            # Web + JPA + H2 (the domain service + tests)
└── frontend/               # React + Vite + Recharts
```
