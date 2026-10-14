# FaceBookRegression

Application Spring Boot qui collecte les publications d'une page Facebook via
l'API Graph, calcule leurs caractéristiques, les encode en variables
numériques prêtes pour la régression et les enregistre dans PostgreSQL. Une API
REST permet ensuite de consulter, rechercher et exporter les données brutes et
encodées.

## Pré-requis

- JDK 21 ;
- PostgreSQL, accessible par défaut sur `localhost:5433`, avec une base `facebook` ;
- un token Facebook Graph API ayant accès aux publications et aux insights de la page.

Les identifiants ne sont pas dans le code. `application.yml` les lit dans des
variables d'environnement, listées dans `.env.example` :

| Variable | Rôle | Défaut |
|---|---|---|
| `DB_URL` | URL JDBC | `jdbc:postgresql://localhost:5433/facebook` |
| `DB_USERNAME` | Utilisateur PostgreSQL | `postgres` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | aucun (obligatoire) |
| `FACEBOOK_ACCESS_TOKEN` | Token Graph API | aucun (obligatoire) |

L'identifiant de la page est la constante `PAGE_ID` de `FacebookGraphApiService`.

## Installation de la base

```sh
createdb -p 5433 -U postgres facebook
psql -h localhost -p 5433 -U postgres -d facebook -f sql/schema.sql
```

Le schéma n'est pas généré par Hibernate (`ddl-auto: none`) : `sql/schema.sql`
crée les tables `donnees_facebook_brutes` et `donnees_facebook_encoded`.

## Lancement — Linux/macOS

```sh
export DB_PASSWORD='...'
export FACEBOOK_ACCESS_TOKEN='...'
./mvnw spring-boot:run
```

## Lancement — Windows PowerShell

```powershell
$env:DB_PASSWORD = "..."
$env:FACEBOOK_ACCESS_TOKEN = "..."
.\mvnw.cmd spring-boot:run
```

L'application écoute sur `http://localhost:9000/face-book-regression`. La racine
redirige vers Swagger UI (`/swagger-ui/index.html`).

## Utilisation

Vérifier d'abord que le token est valide :

```sh
curl http://localhost:9000/face-book-regression/collecte/test-connexion
```

Lancer la collecte sur une période :

```sh
curl -X POST http://localhost:9000/face-book-regression/collecte/lancer \
  -H "Content-Type: application/json" \
  -d '{"since": "2024-10-01", "until": "2026-07-14"}'
```

Réponse :

```json
{
  "nbPublicationsCollectees": 101,
  "nbLignesBrutes": 101,
  "nbLignesEncoded": 101,
  "cheminCsvTrace": ".../output/trace_collecte_2024-10-01_2026-07-14_20261014_182542.csv",
  "since": "2024-10-01",
  "until": "2026-07-14",
  "message": "Collecte terminée avec succès.",
  "erreurApi": null
}
```

Si l'API Facebook renvoie une erreur (token expiré, par exemple), la base n'est
pas modifiée et le détail est renvoyé dans `erreurApi`. Si la période ne
contient aucune publication, la réponse l'indique et la base reste intacte.

### Consultation des données

| Ressource | Table |
|---|---|
| `/donneesfacebookbrutes` | `donnees_facebook_brutes` |
| `/donneesfacebookencodeds` | `donnees_facebook_encoded` |

| Méthode | Sous-route | Description |
|---|---|---|
| GET | `/` | Liste paginée |
| POST | `/search` | Recherche multicritères (filtres, intervalles) |
| GET | `/{id}` | Détail |
| POST | `/export/csv` | Export CSV |
| POST / PUT / DELETE | `/`, `/{id}` | Création, modification, suppression |

La route `/api/config/columns/{entity}/{componentType}` (GET/PUT) gère la
visibilité des colonnes côté interface (`config/column-visibility.json`).

## Données produites

**Données brutes** (`donnees_facebook_brutes`) :

- contenu : longueur du message, nombre de hashtags et de tags, lien externe, emoji, question ;
- média : type de post (`texte`, `photo`, `album`, `video`, `lien`, `evenement`), ratio d'image, nombre de photos d'un album, durée et longueur du titre d'une vidéo ;
- temps : jour de la semaine, heure, jours depuis le dernier post, nombre de posts sur 7 et 30 jours ;
- métriques : vues, réactions, commentaires.

**Données encodées** (`donnees_facebook_encoded`) :

- booléens convertis en 0/1 ;
- variables catégorielles en one-hot, avec une modalité de référence omise pour éviter la colinéarité (`texte` pour le type de post, `dimanche` pour le jour) ;
- ratio d'image converti en `log(largeur / hauteur)`, valeur 0 si absent.

Chaque collecte génère aussi un CSV horodaté dans `output/`
(`trace_collecte_<since>_<until>_<horodatage>.csv`) qui contient les deux jeux
de données, pour audit.

## Règles de collecte et limites

- **Remplacement** : chaque collecte réussie vide les deux tables avant de les réécrire, dans une seule transaction. Une collecte en échec ne laisse pas de données partielles.
- **Vues** : conservées uniquement pour les vidéos et les albums ; elles valent `NULL` pour les autres types.
- **Indicateurs temporels** : « jours depuis le dernier post » et les nombres de posts sur 7 et 30 jours sont calculés sur la période collectée uniquement. Les premiers posts d'une période n'ont donc pas d'historique avant `since`, et le premier post n'a pas de valeur pour « jours depuis le dernier post ».
- **Métriques indisponibles** : si l'API ne renvoie pas les insights d'un post, ses vues et réactions valent 0. Un 0 peut donc signifier « non disponible » autant que « aucune réaction ».
- **Temps de collecte** : chaque post déclenche plusieurs appels à l'API (insights, commentaires, vidéo). Une longue période prend du temps (environ une seconde par publication).
- **Sécurité** : l'API REST n'est pas authentifiée et le CORS est ouvert. Elle est prévue pour un usage local.

## Structure du projet

```
src/main/java/org/example/facebookregression/
├── config/          Configuration web (CORS, redirection Swagger)
├── controllers/     Endpoints REST (collecte, consultation, colonnes)
├── dto/             Requête et résultat de la collecte
├── exception/       Gestion centralisée des erreurs
├── models/          Entités JPA et DTO de recherche
├── repositories/    Accès aux données (Spring Data JPA)
├── services/        Graph API, encodage, orchestration de la collecte
├── specification/   Critères de recherche dynamiques
└── utils/           Export CSV, conversions, utilitaires
sql/schema.sql       Schéma PostgreSQL
config/              Visibilité des colonnes
output/              CSV de trace générés (non versionné)
```
