# API Gestion des Villes et Départements

## 📋 Description

API REST Spring Boot permettant la gestion des villes et départements français avec leurs données de recensement. L'application propose un CRUD complet avec validation des données, recherche avancée et statistiques.

## 🛠️ Technologies utilisées

### Backend
- **Spring Boot 3.5.4** - Framework principal
- **Java 21** - Langage de développement
- **Spring Data JPA** - ORM et accès aux données
- **Hibernate** - Implémentation JPA
- **MySQL** - Base de données
- **Maven** - Gestionnaire de dépendances
- **Bean Validation** - Validation des données

### Frontend
- **HTML5/CSS3** - Interface utilisateur
- **JavaScript (ES6+)** - Logique côté client
- **Fetch API** - Communication avec l'API REST

## 🏗️ Architecture

### Choix architecturaux

#### **Pattern MVC en couches**
```
Frontend (JS) ↔ Controller ↔ Service ↔ DAO ↔ Base de données
                     ↕         ↕
                   DTO ↔ Mapper ↔ Entity
```

#### **Justification des choix**

1. **Séparation en couches** : 
   - **Maintenabilité** : Chaque couche a une responsabilité unique
   - **Testabilité** : Tests unitaires isolés par couche
   - **Évolutivité** : Modification d'une couche sans impact sur les autres

2. **Pattern DTO + Mapper** :
   - **Problème résolu** : Évite les références circulaires JSON (`Ville ↔ Departement`)
   - **Sécurité** : Contrôle des données exposées par l'API
   - **Versioning** : Évolution de l'API sans casser les entités

3. **Services transactionnels** :
   - **Cohérence** : Gestion automatique des transactions
   - **Logique métier** : Centralisation des règles business
   - **Validation** : Contrôles avant persistance

4. **Validation multicouche** :
   - **Frontend** : Validation UX immédiate
   - **Controller** : Validation des données reçues (`@Valid`)
   - **Service** : Validation métier (unicité, cohérence)
   - **Entity** : Contraintes de base de données

## 🚀 Installation et lancement

### Prérequis
- Java 21+
- MySQL 8.0+
- Maven 3.6+

### Configuration base de données
1. Créer une base de données MySQL nommée `recensement`
2. Configurer les accès dans `src/main/resources/application.properties` :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/recensement
spring.datasource.username=root
spring.datasource.password=
```

### Lancement de l'application
```bash
# Cloner le projet
git clone [URL_DU_PROJET]
cd hello

# Compiler et lancer
mvn spring-boot:run
```

L'application sera accessible sur : **http://localhost:8081**

### Vérification du démarrage
- API : http://localhost:8081/hello
- Interface : http://localhost:8081 (page d'accueil avec l'interface de test)

## 📚 Documentation API

### Base URL
```
http://localhost:8081
```

### Endpoints Villes

#### GET /villes
Récupère toutes les villes avec leurs départements.
```json
[
  {
    "id": 1,
    "nom": "Paris",
    "nbHabitants": 2165423,
    "departement": {
      "id": 1,
      "code": "75",
      "nom": "Paris"
    }
  }
]
```

#### GET /villes/{id}
Récupère une ville par son ID.

#### GET /villes/nom/{nom}
Récupère une ville par son nom (insensible à la casse).

#### POST /villes
Crée une nouvelle ville.
```json
{
  "nom": "Nouvelle Ville",
  "nbHabitants": 50000,
  "departement": {
    "code": "75",
    "nom": "Paris"
  }
}
```

**Validation** :
- `nom` : 2-100 caractères, obligatoire
- `nbHabitants` : 1-50 000 000, obligatoire
- `departement` : obligatoire avec code existant

#### PUT /villes/{id}
Met à jour une ville existante (même format que POST).

#### DELETE /villes/{id}
Supprime une ville.

### Endpoints Départements

#### GET /departements
Récupère tous les départements.

#### GET /departements/{id}
Récupère un département par son ID avec ses villes.

#### GET /departements/code/{code}
Récupère un département par son code.

#### GET /departements/ordered-by-code
Départements triés par code croissant.

#### GET /departements/ordered-by-nom
Départements triés par nom alphabétique.

#### POST /departements
Crée un nouveau département.
```json
{
  "code": "99",
  "nom": "Nouveau Département"
}
```

#### PUT /departements/{id}
Met à jour un département existant.

#### DELETE /departements/{id}
Supprime un département (si aucune ville associée).

### Endpoints Statistiques

#### GET /departements/{id}/top-villes/{limit}
Top N villes du département par population.

#### GET /departements/{id}/villes-population?min=1000&max=50000
Villes du département dans une fourchette de population.

#### GET /departements/{id}/population-totale
Population totale du département.

#### GET /departements/{id}/nombre-villes
Nombre de villes dans le département.

## 🖥️ Guide d'utilisation du frontend

### Accès à l'interface
Ouvrir **http://localhost:8081** dans un navigateur web.

### Fonctionnalités disponibles

#### 🏙️ Gestion des Villes
1. **Onglet "Villes"** : Liste de toutes les villes
2. **Ajouter une ville** :
   - Cliquer sur "Nouvelle Ville"
   - Remplir le formulaire (nom, population, département)
   - Valider
3. **Modifier une ville** :
   - Cliquer sur l'icône ✏️ dans la liste
   - Modifier les champs souhaités
   - Sauvegarder
4. **Supprimer une ville** :
   - Cliquer sur l'icône 🗑️
   - Confirmer la suppression

#### 🗺️ Gestion des Départements
1. **Onglet "Départements"** : Liste complète
2. **CRUD départements** : Même principe que les villes
3. **Statistiques avancées** :
   - Population totale par département
   - Nombre de villes
   - Top villes les plus peuplées

#### 🔍 Fonctionnalités de recherche
- **Barre de recherche** : Filtrage en temps réel
- **Tri dynamique** : Clic sur les en-têtes de colonnes
- **Filtres avancés** : Population, département, etc.

#### 📊 Tableau de bord
- **Statistiques globales** : Total villes, départements, population
- **Graphiques** : Répartition par département
- **Export** : Données au format JSON

### Messages d'erreur courants

1. **"Département non trouvé"** : Le code département saisi n'existe pas
2. **"Ville déjà existante"** : Une ville avec ce nom existe déjà
3. **"Erreur de validation"** : Données invalides (population négative, nom trop court, etc.)

### Tests recommandés

1. **Test création** :
   - Créer une ville avec département valide
   - Vérifier l'affichage dans la liste

2. **Test validation** :
   - Essayer de créer une ville sans département
   - Vérifier l'affichage de l'erreur

3. **Test modification** :
   - Modifier une ville existante
   - Vérifier la persistance des changements

4. **Test suppression** :
   - Supprimer une ville
   - Vérifier sa disparition de la liste

## 🧪 Tests et validation

### Données de test
L'application se lance avec des données de recensement français pré-chargées via `data.sql`.

### Validation des endpoints
Utiliser un client REST (Postman, curl) ou l'interface web fournie pour tester les endpoints.

### Exemple de test avec curl
```bash
# Récupérer toutes les villes
curl http://localhost:8081/villes

# Créer une nouvelle ville
curl -X POST http://localhost:8081/villes \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test","nbHabitants":1000,"departement":{"code":"75","nom":"Paris"}}'
```

## 📁 Structure du projet

```
src/
├── main/
│   ├── java/fr/diginamic/hello/
│   │   ├── controlers/     # Controllers REST
│   │   ├── services/       # Logique métier
│   │   ├── dao/           # Repositories JPA
│   │   ├── models/        # Entités JPA
│   │   ├── dto/           # Data Transfer Objects
│   │   ├── mappers/       # Convertisseurs Entity ↔ DTO
│   │   └── config/        # Configuration Spring
│   └── resources/
│       ├── static/        # Frontend (HTML, CSS, JS)
│       ├── data.sql       # Données de test
│       └── application.properties
└── test/                  # Tests unitaires
```

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Commit les changements (`git commit -am 'Ajout nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Créer une Pull Request

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.