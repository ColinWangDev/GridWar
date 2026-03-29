# GridWar

Browser-based strategy MVP: players capture or attack cells on a shared grid, earn score, and climb live leaderboards. This repo contains a **React (Vite)** frontend and a **Spring Boot** API backed by **PostgreSQL**.

## Prerequisites

- **Java 17** and **Maven 3.9+** (for the API)
- **Node.js 20.19+** (recommended) or **18+** with **Vite 5** (see `frontend/package.json`; Vite 8 requires a newer Node)
- **Docker** and **Docker Compose** (optional but recommended for PostgreSQL and full stack)

## Quick start (Docker Compose)

Runs PostgreSQL and the API. The API applies database migrations (Flyway) on startup.

```bash
docker compose up --build
```

- API: [http://localhost:8080](http://localhost:8080)
- Postgres: `localhost:5432`, database `gridwar`, user/password `gridwar` (as defined in `docker-compose.yml`)

The frontend is not started by Compose. Run it separately (below) or serve the built static files behind a reverse proxy.

## Local development

### 1. Start PostgreSQL

Either use Docker for the database only:

```bash
docker compose up postgres
```

Or point the API at any PostgreSQL instance and create a database named `gridwar` with matching credentials (see environment variables below).

### 2. Run the API

```bash
cd backend
mvn spring-boot:run
```

The API listens on **port 8080** by default. Flyway creates and seeds the schema (including the 30×30 grid) on first run.

### 3. Run the frontend (dev)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies **`/api`** to `http://localhost:8080` (see `frontend/vite.config.js`). Open the URL Vite prints (usually [http://localhost:5173](http://localhost:5173)).

### 4. Play

Choose a nickname on the join screen. The client stores your guest `playerId` in `localStorage`.

## Production build (frontend)

```bash
cd frontend
npm install
npm run build
```

Static assets are written to `frontend/dist/`. Serve that folder with a web server and configure it to **proxy `/api`** to the Spring Boot application so the browser can call the API on the same origin, or set an appropriate base URL in your deployment.

## Environment variables

| Variable | Description | Default |
|---------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `gridwar` |
| `DB_USER` | Database user | `gridwar` |
| `DB_PASSWORD` | Database password | `gridwar` |
| `SERVER_PORT` | HTTP port for the API | `8080` |

Gameplay tuning (Spring `application.yml`, prefix `gridwar.*`): grid size, max energy, regen interval, action cooldown, optional CAPTCHA threshold.

**Season schedule:** Each season runs from **Monday 00:00 to the next Monday 00:00** in **Australia/Sydney** (AEST/AEDT). A scheduled job and startup logic roll the map when that boundary passes. Historical rows stay in `season_leaderboard_results`.

## Database reference

A standalone SQL reference (same structure as Flyway migrations) lives in [`database/gridwar-schema.sql`](database/gridwar-schema.sql) for restoring an empty database or documenting tables.

## API overview

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/player/start` | Create guest session `{ "nickname": "..." }` |
| `GET` | `/api/player/status` | Player stats (header `X-Player-Id`) |
| `POST` | `/api/player/captcha-verify` | After many actions, verify with `{ "answer": "gridwar" }` |
| `GET` | `/api/grid` | Full grid state |
| `POST` | `/api/cell/action` | `{ "x", "y" }` (header `X-Player-Id`) |
| `GET` | `/api/season` | Current season window + live top 20 |
| `GET` | `/api/leaderboard` | Latest overall snapshot (empty during the first season) |

## Project structure

```
GridWar/
├── backend/          # Spring Boot + Flyway
├── frontend/         # Vite + React
├── database/         # Schema reference SQL
├── docker-compose.yml
└── README.md
```
