# 📘 Documentation complète des APIs — FaceBookRegression

> **URL de base :** `http://localhost:8080`  
> **Swagger UI :** `http://localhost:8080/swagger-ui.html`  
> **Format des réponses :** JSON

---

## Table des matières

1. [Collecte de données Facebook](#1--collecte-de-données-facebook)
2. [Données brutes Facebook (CRUD)](#2--données-brutes-facebook)
3. [Données encodées Facebook (CRUD)](#3--données-encodées-facebook)
4. [Entraînement des modèles Weka](#4--entraînement-des-modèles-weka)
5. [Modèles de régression Facebook (CRUD)](#5--modèles-de-régression-facebook)
6. [Simulation & Prédiction](#6--simulation--prédiction)
7. [Configuration des colonnes visibles](#7--configuration-des-colonnes-visibles)

---

## 1. 📥 Collecte de données Facebook

**Préfixe URL :** `/collecte`

Ces endpoints permettent de collecter les publications Facebook via l'API Graph.

---

### `GET /collecte/test-connexion`

**Description :** Vérifie que le token Facebook configuré est valide.

**Pas de corps de requête.**

**Réponse 200 :**
```json
{
  "id": "123456789",
  "name": "Ma Page Facebook"
}
```

**Exemple Postman :**
- Méthode : `GET`
- URL : `http://localhost:8080/collecte/test-connexion`

---

### `POST /collecte/lancer`

**Description :** Lance la collecte complète des publications Facebook pour une période donnée, les encode, et les sauvegarde en base de données.

**Corps de la requête (JSON) :**
```json
{
  "since": "2024-01-01",
  "until": "2024-03-31"
}
```

| Champ   | Type   | Obligatoire | Description                   |
|---------|--------|-------------|-------------------------------|
| `since` | String | ✅           | Date de début (format `yyyy-MM-dd`) |
| `until` | String | ✅           | Date de fin (format `yyyy-MM-dd`)   |

**Réponse 200 :**
```json
{
  "nbPublications": 42,
  "nbLignesBase": 42,
  "cheminCsv": "output/features_facebook_encoded.csv"
}
```

**Exemple Postman :**
- Méthode : `POST`
- URL : `http://localhost:8080/collecte/lancer`
- Headers : `Content-Type: application/json`
- Body (raw JSON) :
```json
{
  "since": "2024-01-01",
  "until": "2024-12-31"
}
```

---

## 2. 📋 Données brutes Facebook

**Préfixe URL :** `/donneesfacebookbrutes`

Gestion CRUD des données brutes collectées depuis Facebook (avant encodage).

---

### `GET /donneesfacebookbrutes`

**Description :** Récupère la liste paginée et triée de toutes les données brutes.

**Paramètres de requête (query params) :**

| Paramètre   | Type   | Défaut   | Description                        |
|-------------|--------|----------|------------------------------------|
| `page`      | int    | `0`      | Numéro de page (0-indexé)         |
| `size`      | int    | `10`     | Nombre d'éléments par page        |
| `sortParam` | String | `id,asc` | Tri : `champ,asc` ou `champ,desc` |

**Exemple Postman :**
- Méthode : `GET`
- URL : `http://localhost:8080/donneesfacebookbrutes?page=0&size=10&sortParam=id,asc`

---

### `POST /donneesfacebookbrutes/search`

**Description :** Recherche paginée avec des critères de filtrage.

**Paramètres de requête :** Identiques à `GET /donneesfacebookbrutes`.

**Corps de la requête (JSON) :**
```json
{
  "typePost": "photo"
}
```

---

### `GET /donneesfacebookbrutes/{id}`

**Description :** Récupère une donnée brute par son identifiant.

**Paramètre de chemin :**

| Paramètre | Type    | Description       |
|-----------|---------|-------------------|
| `id`      | Integer | ID de l'enregistrement |

**Réponse 200 :** Un objet `DonneesFacebookBrute`.  
**Réponse 404 :** Enregistrement introuvable.

---

### `POST /donneesfacebookbrutes`

**Description :** Crée un nouvel enregistrement de données brutes.

**Corps de la requête (JSON) :** Un objet `DonneesFacebookBrute` complet.

**Réponse 201 :** Enregistrement créé.  
**Réponse 400 :** Données invalides.

---

### `PUT /donneesfacebookbrutes/{id}`

**Description :** Met à jour un enregistrement existant.

**Paramètre de chemin :** `id` (Integer)  
**Corps de la requête :** Objet `DonneesFacebookBrute` mis à jour.

**Réponse 200 :** Enregistrement mis à jour.  
**Réponse 404 :** Non trouvé.

---

### `DELETE /donneesfacebookbrutes/{id}`

**Description :** Supprime un enregistrement (retourne 200 même s'il était déjà supprimé).

**Réponse 200 :**
```json
{
  "status": "OK",
  "message": "donnees facebook brute deleted successfully"
}
```

---

### `POST /donneesfacebookbrutes/export/csv`

**Description :** Exporte une liste de données brutes vers un fichier CSV téléchargeable.

**Corps de la requête :** Liste d'objets `DonneesFacebookBrute` (tableau JSON).  
**Réponse :** Fichier `data.csv` (Content-Type: `text/csv`).

---

## 3. 🔢 Données encodées Facebook

**Préfixe URL :** `/donneesfacebookencodeds`

Gestion CRUD des données encodées (après transformation des variables par `FacebookEncoder`).  
Les endpoints sont **identiques en structure** à ceux des données brutes.

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/donneesfacebookencodeds` | Liste paginée |
| `POST` | `/donneesfacebookencodeds/search` | Recherche paginée avec filtre |
| `GET` | `/donneesfacebookencodeds/{id}` | Récupère par ID |
| `POST` | `/donneesfacebookencodeds` | Créer un enregistrement |
| `PUT` | `/donneesfacebookencodeds/{id}` | Mettre à jour |
| `DELETE` | `/donneesfacebookencodeds/{id}` | Supprimer |
| `POST` | `/donneesfacebookencodeds/export/csv` | Exporter en CSV |

> Voir la [section 2](#2--données-brutes-facebook) pour les détails de chaque endpoint — la structure est identique.

---

## 4. 🤖 Entraînement des modèles Weka

**Préfixe URL :** `/entrainement`

---

### `POST /entrainement/lancer`

**Description :** Lance le pipeline complet d'entraînement des modèles de classification.

**Opérations effectuées :**
1. Lit le fichier `output/features_facebook_encoded.csv`
2. Évalue les modèles candidats (J48, Random Forest) par validation croisée stratifiée (5 plis × 10 répétitions)
3. Sélectionne le meilleur modèle pour chaque cible
4. Entraîne sur toutes les données
5. Stocke le modèle sérialisé en base PostgreSQL (`modeles_classification_facebook`)

**Pas de corps de requête.**

**Réponse 200 :**
```json
{
  "message": "Pipeline d'entraînement terminé.",
  "erreur": null,
  "cibles": [
    {
      "cible": "reactions_classe",
      "verdict": "FIABLE",
      "accuracy": 0.82,
      "baseline": 0.60,
      "nbInstances": 120,
      "stockeEnBase": true,
      "resume": "Modèle J48 sélectionné, accuracy 82% > baseline 60%"
    },
    {
      "cible": "commentaires_classe",
      "verdict": "NON_FIABLE",
      "accuracy": 0.55,
      "baseline": 0.70,
      "nbInstances": 120,
      "stockeEnBase": false,
      "resume": "Accuracy inférieure à la baseline"
    },
    {
      "cible": "vues_classe",
      "verdict": "INSUFFISANT",
      "accuracy": null,
      "baseline": null,
      "nbInstances": 15,
      "stockeEnBase": false,
      "resume": "Données insuffisantes (< 20 instances)"
    }
  ]
}
```

**Verdicts possibles :**

| Verdict | Signification |
|---------|---------------|
| `FIABLE` | Le modèle dépasse la baseline → stocké en base |
| `NON_FIABLE` | Accuracy inférieure à la baseline → non stocké |
| `INSUFFISANT` | Pas assez de données pour entraîner |
| `ERREUR` | Erreur technique sur cette cible |

**Exemple Postman :**
- Méthode : `POST`
- URL : `http://localhost:8080/entrainement/lancer`
- Pas de body

> ⚠️ L'opération peut prendre **plusieurs secondes** (validation croisée 5×10).

---

## 5. 🗃️ Modèles de régression Facebook

**Préfixe URL :** `/modelesregressionfacebooks`

Gestion CRUD de la table `modeles_classification_facebook` (lecture, modification manuelle des entrées).

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/modelesregressionfacebooks` | Liste paginée |
| `POST` | `/modelesregressionfacebooks/search` | Recherche paginée avec filtre |
| `GET` | `/modelesregressionfacebooks/{id}` | Récupère par ID |
| `POST` | `/modelesregressionfacebooks` | Créer un enregistrement |
| `PUT` | `/modelesregressionfacebooks/{id}` | Mettre à jour |
| `DELETE` | `/modelesregressionfacebooks/{id}` | Supprimer |
| `POST` | `/modelesregressionfacebooks/export/csv` | Exporter en CSV |

> La structure des endpoints est identique à celle de la [section 2](#2--données-brutes-facebook).

---

## 6. 🔮 Simulation & Prédiction

**Préfixe URL :** `/simulation`

Ces endpoints permettent de simuler une publication Facebook et d'obtenir une prédiction du nombre de réactions, commentaires et vues à partir des modèles Weka entraînés.

---

### `POST /simulation/predire`  
### `POST /simulation/simuler` *(alias identique)*

**Description :** Prend en entrée les caractéristiques d'une publication simulée, encode les données, charge les modèles Weka depuis la base PostgreSQL, et retourne la classe prédite pour chaque cible.

**Corps de la requête (JSON) :**
```json
{
  "longueurMessage": 150,
  "nbHashtags": 3,
  "nbTags": 2,
  "contientLienExterne": true,
  "contientEmoji": false,
  "contientQuestion": true,
  "typePost": "photo",
  "ratioImage": 0.75,
  "nbPhotosAlbum": 0,
  "dureeVideo": 0,
  "longueurTitreVideo": 0,
  "jourSemaine": 3,
  "heurePublication": 18,
  "jourDepuisDernierPost": 2,
  "nbPosts7j": 4,
  "nbPostsMois": 12
}
```

**Description des champs :**

| Champ | Type | Description |
|-------|------|-------------|
| `longueurMessage` | int | Nombre de caractères du texte |
| `nbHashtags` | int | Nombre de `#` dans le post |
| `nbTags` | int | Nombre de `@mentions` |
| `contientLienExterne` | boolean | Présence d'un lien URL externe |
| `contientEmoji` | boolean | Présence d'emojis |
| `contientQuestion` | boolean | Présence d'un `?` dans le texte |
| `typePost` | String | Type : `"photo"`, `"video"`, `"lien"`, `"statut"` |
| `ratioImage` | double | Ratio image (0.0 à 1.0) |
| `nbPhotosAlbum` | int | Nombre de photos si album |
| `dureeVideo` | int | Durée en secondes si vidéo |
| `longueurTitreVideo` | int | Longueur du titre si vidéo |
| `jourSemaine` | int | Jour de la semaine : 1=Lundi … 7=Dimanche |
| `heurePublication` | int | Heure de publication (0–23) |
| `jourDepuisDernierPost` | int | Jours depuis la dernière publication |
| `nbPosts7j` | int | Nombre de posts sur les 7 derniers jours |
| `nbPostsMois` | int | Nombre de posts sur les 30 derniers jours |

**Réponse 200 :**
```json
{
  "predictions": {
    "reactions_classe": {
      "classePredit": "7-18",
      "probabilites": {
        "0-6": 0.20,
        "7-18": 0.65,
        "19+": 0.15
      },
      "verdict": "FIABLE"
    },
    "commentaires_classe": {
      "classePredit": "1+",
      "probabilites": {
        "0": 0.35,
        "1+": 0.65
      },
      "verdict": "FIABLE"
    },
    "vues_classe": {
      "classePredit": null,
      "probabilites": null,
      "verdict": "INSUFFISANT"
    }
  }
}
```

**Classes prédites par cible :**

| Cible | Classes possibles |
|-------|-------------------|
| `reactions_classe` | `"0-6"`, `"7-18"`, `"19+"` |
| `commentaires_classe` | `"0"`, `"1+"` |
| `vues_classe` | Valeurs numériques (si vidéo/album avec modèle disponible) |

**Exemple Postman :**
- Méthode : `POST`
- URL : `http://localhost:8080/simulation/predire`
- Headers : `Content-Type: application/json`
- Body (raw JSON) :
```json
{
  "longueurMessage": 200,
  "nbHashtags": 5,
  "nbTags": 1,
  "contientLienExterne": false,
  "contientEmoji": true,
  "contientQuestion": false,
  "typePost": "photo",
  "ratioImage": 0.8,
  "nbPhotosAlbum": 0,
  "dureeVideo": 0,
  "longueurTitreVideo": 0,
  "jourSemaine": 5,
  "heurePublication": 12,
  "jourDepuisDernierPost": 1,
  "nbPosts7j": 3,
  "nbPostsMois": 10
}
```

> 💡 `/simulation/predire` et `/simulation/simuler` sont **exactement identiques** — les deux utilisent les modèles Weka entraînés.

---

## 7. ⚙️ Configuration des colonnes visibles

**Préfixe URL :** `/api/config/columns`

Permet de gérer quelles colonnes sont affichées dans le frontend pour chaque entité.

---

### `GET /api/config/columns/{entity}/{componentType}`

**Description :** Récupère la liste des champs visibles pour une entité et un type de vue donnés.

**Paramètres de chemin :**

| Paramètre | Type | Exemple | Description |
|-----------|------|---------|-------------|
| `entity` | String | `donneesfacebookbrute` | Nom de l'entité |
| `componentType` | String | `list` ou `detail` | Type de vue |

**Réponse 200 :**
```json
{
  "status": "OK",
  "message": "Configuration retrieved successfully",
  "data": ["id", "typePost", "nbReactions", "datePublication"]
}
```

**Exemple Postman :**
- Méthode : `GET`
- URL : `http://localhost:8080/api/config/columns/donneesfacebookbrute/list`

---

### `PUT /api/config/columns/{entity}/{componentType}`

**Description :** Sauvegarde une nouvelle configuration de colonnes visibles.

**Corps de la requête (JSON) :** Liste de noms de champs.
```json
["id", "typePost", "nbReactions", "datePublication", "nbHashtags"]
```

**Réponse 200 :**
```json
{
  "status": "OK",
  "message": "Configuration des colonnes mise à jour avec succès"
}
```

**Réponse 400 :** Si la liste est vide ou nulle.

---

## 🔄 Flux complet d'utilisation

```
1. Vérifier la connexion Facebook
   GET /collecte/test-connexion

2. Collecter les données Facebook
   POST /collecte/lancer  { "since": "...", "until": "..." }

3. Vérifier les données brutes collectées
   GET /donneesfacebookbrutes?page=0&size=20

4. Lancer l'entraînement des modèles
   POST /entrainement/lancer

5. Vérifier les modèles entraînés
   GET /modelesregressionfacebooks?page=0&size=10

6. Prédire l'impact d'une publication simulée
   POST /simulation/predire  { ... caractéristiques ... }
```

---

## 📌 Notes importantes

- **Pagination :** Tous les endpoints de liste retournent un objet `Page` avec les champs `content`, `totalElements`, `totalPages`, `number`, `size`.
- **Tri :** Le paramètre `sortParam` accepte des tris multiples séparés par `;`. Exemple : `typePost,asc;datePublication,desc`.
- **Entraînement préalable :** Les endpoints `/simulation/predire` et `/simulation/simuler` **nécessitent** qu'au moins un modèle ait été entraîné via `POST /entrainement/lancer`.
- **Swagger UI :** Pour une exploration interactive, accéder à `http://localhost:8080/swagger-ui.html` quand l'application est lancée.
