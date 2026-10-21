# Infra — Djiguiya Transport

```bash
# MySQL seul (dev local backend/front sur la machine)
docker compose up -d db

# Stack complète (quand Dockerfiles backend/front sont prêts)
docker compose up -d --build
```

- MySQL : `localhost:3307` (user `djiguiya` / `djiguiya`, base `djiguiya_transport`)
- API : `http://localhost:8080`
- Front : `http://localhost:4200`
