# Deployment Guide

This guide takes Job Tracker from running on your laptop to a public URL a
recruiter can open. Two routes are described — pick one.

---

## Route A — One small server + Docker (recommended)

Hosts the **full architecture** (Eureka, load balancing, circuit breaker) exactly
as in the repo. Costs roughly $4–6/month for a small VM.

### 1. Get a server
Create a small Linux VM (Ubuntu 22.04+) on any provider — e.g. Hetzner,
DigitalOcean, or Oracle Cloud's free tier. **2 GB RAM minimum** (the Java images
build inside the server). Note its public IP.

### 2. Point a domain at it
Buy a cheap domain (or use a free subdomain). Add a DNS **A record** for e.g.
`jobtracker.yourdomain.com` → your server's IP. HTTPS needs a real domain.

### 3. Install Docker on the server
```bash
ssh root@YOUR_SERVER_IP
curl -fsSL https://get.docker.com | sh
```

### 4. Get the code and configure secrets
```bash
git clone https://github.com/miss-roy/JobTracker.git
cd JobTracker
cp .env.example .env
nano .env        # set a strong POSTGRES_PASSWORD and your DOMAIN
```

### 5. Launch
```bash
docker compose -f docker-compose.prod.yml up -d --build
# optional: run it scaled
docker compose -f docker-compose.prod.yml up -d --scale job-service=2 --scale api-gateway=2
```
Caddy automatically fetches an HTTPS certificate for your domain. Open
`https://jobtracker.yourdomain.com` — that's your live link.

### Updating later
```bash
git pull && docker compose -f docker-compose.prod.yml up -d --build
```

---

## Route B — Free split hosting ($0)

Frontend, backend, and database hosted separately on free tiers. No server to
manage, but free backends "sleep" when idle (≈30s cold start), and you host a
slimmed backend (the full multi-replica/Eureka setup stays in the repo).

1. **Database** — create a free PostgreSQL on **Neon** or **Supabase**. Copy its
   connection URL / user / password.
2. **Backend** — on **Render** or **Railway**, deploy `job-service` (and optionally
   `api-gateway`) from this repo. Set the `SPRING_DATASOURCE_*` env vars to the
   Neon/Supabase values, and `APP_SEED_SAMPLE_DATA=false`.
3. **Frontend** — deploy `frontend/` to **Vercel** or **Netlify**. Set
   `VITE_API_BASE` to your backend's public URL and rebuild.

---

## After going live
- Put the live URL at the top of `README.md` and on your resume.
- Set `APP_SEED_SAMPLE_DATA=false` (or seed once, then turn it off).
- For the **iOS app** in production: set `VITE_API_BASE=https://jobtracker.yourdomain.com`
  in `frontend/.env.ios`, rebuild with `npm run build:ios`, and remove the
  localhost ATS exception in `ios/App/App/Info.plist`.
- Never commit the real `.env` (it's gitignored).
