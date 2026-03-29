# Deploying GridWar (Vercel + Railway)

This guide walks through hosting the **React (Vite)** frontend on **Vercel** and the **Spring Boot** API plus **PostgreSQL** on **Railway**. Do the Railway steps first so you have a public API URL to plug into Vercel.

---

## Overview

| Piece        | Platform | Notes |
|-------------|----------|--------|
| PostgreSQL  | Railway  | Managed database; credentials injected into the API service |
| Spring API  | Railway  | Built with the existing `backend/Dockerfile`; listens on `$PORT` |
| React app   | Vercel   | Static build of `frontend/`; calls the API via `VITE_API_BASE_URL` |

The browser talks to the **Railway API** directly (cross-origin). CORS is already enabled for `/api/**` in the backend.

---

## Part A — Railway (database + API)

### 1. Create a project

1. Sign in at [railway.app](https://railway.app).
2. **New Project** → **Deploy from GitHub repo** (or **Empty Project** and connect the repo later).
3. If you use a monorepo, you will set the **root directory** for the API service to `backend` in a later step.

### 2. Add PostgreSQL

1. In the project, click **+ New** → **Database** → **PostgreSQL**.
2. Wait until the database is provisioned.
3. Open the **PostgreSQL** service → **Variables**. Railway exposes variables such as:
   - **`DATABASE_URL`** — full `postgresql://user:pass@host:port/dbname?...` (recommended for the API)
   - `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` — individual fields  

   The GridWar API accepts **`DATABASE_URL`** (parsed automatically into JDBC) **or** the **`PG*`** / **`DB_*`** variables. If **none** of these exist on the **API** service, Spring falls back to `localhost:5432`, which fails on Railway with *Connection refused*.

### 3. Deploy the API (Spring Boot)

1. In the same project, **+ New** → **GitHub Repo** → select **GridWar** (or **Dockerfile** if you paste the repo from GitHub once).
2. Open the **API** service → **Settings**:
   - **Root Directory:** `backend`  
     (so Railway runs the `Dockerfile` inside `backend/`.)
   - **Builder:** Dockerfile (should auto-detect `backend/Dockerfile`).
3. **Networking** → **Generate Domain** (or attach your custom domain).  
   Note the public URL, e.g. `https://gridwar-api-production-xxxx.up.railway.app`.  
   **No trailing slash** when you use it in Vercel.

4. **Variables** (API service) — connect the API to Postgres (**required**):

   **Easiest (recommended):** add **one** variable on the **API** service:

   | Name on API service | Value |
   |---------------------|--------|
   | `DATABASE_URL` | **Reference** → your PostgreSQL service → `DATABASE_URL` |

   That single reference is enough: the app parses `postgresql://...` and sets the JDBC URL, user, and password (and adds `sslmode=require` for non-local hosts when the URL has no query string).

   **Alternative:** reference each of `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` from the database service onto the API service with the **same names** (see table in earlier versions of this doc).

   Common mistake: variables exist only on the **Postgres** service. They must also appear on the **API** service (via reference or copy), or the API will keep using `localhost` and crash with *Connection refused*.

   **Do not set `PORT` yourself** — Railway sets `PORT` for the HTTP listener. The app uses `server.port=${PORT:8080}`.

5. **Deploy** (or redeploy). Watch **Build** and **Deploy** logs:

   - Docker builds the JAR and runs `java -jar app.jar`.
   - **Flyway** runs migrations on startup (`V1`, `V2`, …).
   - If the app exits immediately, check DB variables and that Postgres is **running** in the same project.

6. Smoke test (replace with your domain):

   ```bash
   curl -sS "https://YOUR-RAILWAY-API.up.railway.app/api/grid" | head -c 200
   ```

   You should see JSON with `size` and `cells`.

### 4. Railway tips

- **Free tier / sleep:** Free or hobby plans may sleep idle services; first request after sleep can be slow.
- **One-off DB reset:** Use Railway’s Postgres tab (query / connect) only if you know what you are doing; prefer Flyway migrations in the repo for schema changes.
- **Logs:** API service → **Deployments** → latest → **View logs** for Flyway and stack traces.

---

## Part B — Vercel (frontend)

### 1. Import the project

1. Sign in at [vercel.com](https://vercel.com).
2. **Add New** → **Project** → import the **GridWar** GitHub repo.
3. **Configure Project:**
   - **Root Directory:** `frontend`  
     (Critical: the Vite app lives here, not the repo root.)
   - **Framework Preset:** Vite (usually auto-detected).
   - **Build Command:** `npm run build` (default).
   - **Output Directory:** `dist` (default for Vite).

### 2. Environment variable (required for production)

In **Settings** → **Environment Variables**, add:

| Name | Value | Environments |
|------|--------|--------------|
| `VITE_API_BASE_URL` | `https://YOUR-RAILWAY-API.up.railway.app` | Production (and Preview if you want previews to hit a staging API) |

Rules:

- **No trailing slash** on the URL.
- **Do not** include `/api` in the value — the client appends `/api/...` itself.
- **Redeploy** after adding or changing this variable (Vite inlines env at **build** time).

### 3. Deploy

1. Click **Deploy**.
2. Open the production URL Vercel assigns (e.g. `https://gridwar.vercel.app`).
3. Join with a nickname and confirm the grid loads and actions work (browser dev tools → Network: requests should go to your Railway host).

### 4. Preview deployments

Each Git branch/PR can get a `*.vercel.app` URL. If `VITE_API_BASE_URL` is set only for **Production**, Preview builds may still call `/api` on the Vercel host (wrong). Either:

- Add the same `VITE_API_BASE_URL` for **Preview** (pointing at staging or production API), or  
- Accept that previews without the variable only work with local/API proxy patterns (not configured on Vercel by default).

---

## Part C — Checklist before calling it “v1 production”

- [ ] Railway Postgres running; API variables reference DB correctly.
- [ ] Railway API has a **public** domain; `curl /api/grid` succeeds over HTTPS.
- [ ] Vercel **Root Directory** = `frontend`.
- [ ] `VITE_API_BASE_URL` matches Railway API origin exactly (HTTPS, no trailing slash).
- [ ] Production deployment **rebuilt** after setting `VITE_API_BASE_URL`.
- [ ] Optional: set a **custom domain** on Vercel; CORS already allows any origin pattern for `/api/**`.

---

## Troubleshooting

| Symptom | What to check |
|--------|----------------|
| Frontend loads but API errors / Network CORS | Railway URL wrong or API down; confirm `curl` to `/api/grid`. |
| Frontend calls `vercel.app/api/...` (404) | `VITE_API_BASE_URL` missing or Preview env not set — rebuild. |
| API crashes on boot | Logs: Flyway error, missing `PG*`, or DB not reachable. |
| `Connection to localhost:5432 refused` | The API service has no `DATABASE_URL` / `PG*` / `DB_*`. Add a **reference** to Postgres `DATABASE_URL` on the API service (see Part A §3 step 4), then redeploy. |
| 502 from Railway | App not listening on `$PORT`; confirm current `application.yml` uses `${PORT:...}`. |

---

## Local development (unchanged)

- Backend: `DB_*` or `PG*` as in `README.md`; default port `8080`.
- Frontend: leave `VITE_API_BASE_URL` unset so requests use the Vite dev proxy to `localhost:8080`.

---

## File reference

| File | Role |
|------|------|
| `backend/Dockerfile` | Railway build/run image for the API |
| `backend/.../DatabaseUrlEnvironmentPostProcessor.java` | Parses `DATABASE_URL` into JDBC settings |
| `backend/src/main/resources/application.yml` | `PORT`, `PG*` / `DB_*` datasource |
| `frontend/src/api.js` | `VITE_API_BASE_URL` for production API origin |
| `frontend/vite.config.js` | Dev-only proxy to `localhost:8080` |
