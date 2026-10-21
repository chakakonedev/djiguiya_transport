# Djiguiya Transport — Code (`codes/`)

Application complète (Phase 1) : **Angular** + **Spring Boot** + **MySQL** (H2 pour tests / profil local).

```
codes/
├── backend-core/      # API REST /api/v1
├── frontend-web/     # SPA Angular (i18n FR)
└── infra-platform/   # Docker Compose (si Docker dispo)
```

## Backend

```bash
cd backend-core

# Tests unitaires + intégration (H2, sans Docker)
./mvnw.cmd test

# Lancer sans MySQL (fichier H2 local)
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local-h2"

# Lancer avec MySQL (port 3307 — voir infra-platform)
./mvnw.cmd spring-boot:run
```

- Swagger : http://localhost:8080/swagger-ui.html  
- Seed MySQL (Flyway V3) : `chakakone93@yahoo.fr` / `Djiguiya!Owner2026` (rôle `owner` = directeur/admin ; à changer hors local)  
- Si Flyway refuse un checksum (V2/V3 déjà altérés) : recréer la BD / volume Docker `djiguiya_mysql_data`  
- Tests : créent un user `owner@test.local` / `password`

### API principale

| Préfixe | Rôle |
|---------|------|
| `/api/v1/auth/login` | Connexion JWT |
| `/api/v1/staff` | Personnel |
| `/api/v1/vehicles` | Bus, chauffeur principal, documents |
| `/api/v1/trips` | Voyages (bus + départ + retour) |
| `/api/v1/transactions` | Recettes / dépenses |
| `/api/v1/stats/*` | Summary, compare-month, YoY |
| `/api/v1/profit-share/preview` | Répartition bénéfice |
| `/api/v1/payroll/*` | Salaires / primes |

## Frontend

```bash
cd frontend-web
npm install
npm start          # http://localhost:4200
npm test -- --watch=false
npm run build
```

Écrans : Login, Accueil, Flotte, Personnel, Voyages, Caisse, Stats, Répartition.  
Tous les textes UI passent par `src/assets/i18n/fr.json`.

## Infra

```bash
cd infra-platform
docker compose up -d db   # MySQL : localhost:3307
```

Sans Docker : utiliser le profil `local-h2` du backend.

## Hors scope actuel

- Orange Money / réservations clients  
- APK Capacitor (emballage à faire plus tard sur le même front)
