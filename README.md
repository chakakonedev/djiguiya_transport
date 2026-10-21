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
- Seed au démarrage (4 personnels) : mot de passe défaut via `DJIGUIYA_DEFAULT_PASSWORD` (défaut `Djiguiya@2026`) — pas de hash en dur dans le SQL  
  - Chaka Koné (`owner`) — `chakakone93@yahoo.fr`  
  - Tièdiè Tièkoroba Koné (`driver_manager`) — 50 000 FCFA / jour 28 + 1 % recettes trimestre + 1 % bénéfice net annuel (01/08)  
  - Diakaridia Koné (`accountant`) — 1 % bénéfice net annuel  
  - Sekou Koné (`cashier_accountant`) — 1 % bénéfice net annuel  
- Changer son mot de passe : `POST /api/v1/auth/change-password` (JWT requis)  
- Flyway checksum mismatch (ex. V4 modifié) : `repair-on-migrate: true` dans `application.yaml` corrige au démarrage (dev). Sinon, en SQL :  
  `UPDATE flyway_schema_history SET checksum = <nouveau> WHERE version = '4';`  
  Vider la BD n’est utile que pour repartir de zéro.  
- Tests unitaires : user `owner@test.local` / `password` (seed core désactivé)

### API principale

| Préfixe | Rôle |
|---------|------|
| `/api/v1/auth/login` | Connexion JWT |
| `/api/v1/auth/change-password` | Modifier son mot de passe |
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
