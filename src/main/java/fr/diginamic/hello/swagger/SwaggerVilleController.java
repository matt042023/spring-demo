package fr.diginamic.hello.swagger;

import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import fr.diginamic.hello.repositories.VilleRepositoryHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Interface de documentation Swagger pour les opérations sur les villes.
 * Cette interface définit toutes les annotations de documentation OpenAPI/Swagger
 * pour l'API de gestion des villes françaises.
 *
 * <p>Le contrôleur VilleController hérite de cette interface pour bénéficier
 * automatiquement de toute la documentation API sans polluer le code métier.</p>
 *
 * <p><strong>Fonctionnalités documentées :</strong></p>
 * <ul>
 *   <li>CRUD complet des villes</li>
 *   <li>Recherches avancées par critères multiples</li>
 *   <li>Statistiques et analyses par département</li>
 *   <li>Import/export en lot</li>
 *   <li>Gestion des erreurs avec codes HTTP appropriés</li>
 * </ul>
 *
 * @author Matthieu - Développeur Full Stack
 * @version 1.0.0
 * @since 2025-08-26
 */
@Tag(name = "🏙️ Villes", description = """
    **API de gestion des villes françaises**
    
    Cette API permet de gérer l'ensemble des villes françaises avec leurs caractéristiques :
    - **Données** : nom, population, code postal, département
    - **Recherches** : par nom, population, département, critères multiples
    - **Statistiques** : analyses démographiques par département
    - **Gestion** : CRUD complet, import/export en lot
    
    **Codes d'erreur principaux :**
    - `400` : Données invalides ou malformées
    - `404` : Ressource non trouvée
    - `422` : Erreur de validation métier
    - `500` : Erreur serveur interne
    """)
public interface SwaggerVilleController {

    // ==================== CRUD DE BASE ====================

    @Operation(
            summary = "📋 Récupérer toutes les villes avec pagination",
            description = """
            **Récupère la liste paginée de toutes les villes françaises.**
            
            Cette endpoint supporte la pagination et le tri pour gérer efficacement 
            les grandes quantités de données.
            
            **Paramètres de tri disponibles :**
            - `id` : Tri par identifiant (défaut)
            - `nom` : Tri alphabétique par nom
            - `nbHabitants` : Tri par nombre d'habitants
            - `departement.code` : Tri par code département
            
            **Exemples d'usage :**
            - `GET /villes` → Première page, 20 éléments, tri par ID
            - `GET /villes?page=2&size=50&sort=nom` → Page 3, 50 éléments, tri par nom
            - `GET /villes?page=0&size=100&sort=nbHabitants` → 100 plus petites villes
            """,
            tags = {"🏙️ Villes", "📋 CRUD"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Liste des villes récupérée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Exemple de réponse paginée",
                                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "nom": "Paris",
                              "nbHabitants": 2165423,
                              "departement": {
                                "code": "75",
                                "nom": "Paris"
                              }
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20,
                            "sort": {
                              "sorted": true
                            }
                          },
                          "totalElements": 34827,
                          "totalPages": 1742,
                          "first": true,
                          "last": false
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Paramètres de pagination invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionFonctionnelle.class)
                    )
            )
    })
    Page<VilleDTO> getAllVilles(
            @Parameter(
                    description = "Numéro de la page (commence à 0)",
                    example = "0",
                    schema = @Schema(minimum = "0", defaultValue = "0")
            ) @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Taille de la page (nombre d'éléments)",
                    example = "20",
                    schema = @Schema(minimum = "1", maximum = "1000", defaultValue = "20")
            ) @RequestParam(defaultValue = "20") int size,

            @Parameter(
                    description = "Critère de tri",
                    example = "nom",
                    schema = @Schema(
                            defaultValue = "id",
                            allowableValues = {"id", "nom", "nbHabitants", "departement.code"}
                    )
            ) @RequestParam(defaultValue = "id") String sort
    );

    @Operation(
            summary = "🔍 Récupérer une ville par son identifiant",
            description = """
            **Récupère les détails complets d'une ville spécifique.**
            
            Cette endpoint retourne toutes les informations d'une ville :
            nom, population, département associé, etc.
            
            **Cas d'usage typiques :**
            - Affichage des détails d'une ville
            - Vérification de l'existence d'une ville
            - Récupération avant mise à jour
            """,
            tags = {"🏙️ Villes", "📋 CRUD"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Ville trouvée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class),
                            examples = @ExampleObject(
                                    name = "Ville de Marseille",
                                    value = """
                        {
                          "id": 2,
                          "nom": "Marseille",
                          "nbHabitants": 868277,
                          "departement": {
                            "code": "13",
                            "nom": "Bouches-du-Rhône"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ville non trouvée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionFonctionnelle.class),
                            examples = @ExampleObject(
                                    name = "Ville inexistante",
                                    value = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "message": "Ville avec l'identifiant 99999 non trouvée",
                          "timestamp": "2025-08-26T10:30:00.000Z",
                          "path": "/villes/99999"
                        }
                        """
                            )
                    )
            )
    })
    VilleDTO getVilleById(
            @Parameter(
                    description = "Identifiant unique de la ville",
                    example = "1",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", minimum = "1")
            ) @PathVariable Long id
    );

    @Operation(
            summary = "➕ Créer une nouvelle ville",
            description = """
            **Crée une nouvelle ville dans le système.**
            
            La validation des données est automatique :
            - Nom obligatoire (2-100 caractères)
            - Population positive
            - Code département valide (existant dans le système)
            
            **Règles métier :**
            - Le nom doit être unique dans le département
            - Le département doit exister
            - La population ne peut pas être négative
            
            **Note :** L'identifiant est généré automatiquement.
            """,
            tags = {"🏙️ Villes", "📋 CRUD"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "✅ Ville créée avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class),
                            examples = @ExampleObject(
                                    name = "Nouvelle ville créée",
                                    value = """
                        {
                          "id": 12345,
                          "nom": "Nouvelle-Ville",
                          "nbHabitants": 25000,
                          "departement": {
                            "code": "34",
                            "nom": "Hérault"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Données invalides",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionFonctionnelle.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "❌ Erreur de validation métier (nom déjà existant, département inexistant)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionFonctionnelle.class)
                    )
            )
    })
    VilleDTO createVille(
            @Parameter(
                    description = "Données de la nouvelle ville à créer",
                    required = true,
                    schema = @Schema(implementation = VilleDTO.class)
            ) @Valid @RequestBody VilleDTO villeDTO
    );

    @Operation(
            summary = "✏️ Mettre à jour une ville existante",
            description = """
            **Met à jour les informations d'une ville existante.**
            
            Cette opération remplace complètement les données de la ville.
            Tous les champs doivent être fournis, même s'ils ne changent pas.
            
            **Validations appliquées :**
            - Même validation que pour la création
            - Vérification de l'existence de la ville
            - Vérification de l'existence du département
            
            **Important :** L'ID dans l'URL prime sur l'ID dans le body.
            """,
            tags = {"🏙️ Villes", "📋 CRUD"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Ville mise à jour avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Données invalides",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ville non trouvée",
                    content = @Content()
            )
    })
    VilleDTO updateVille(
            @Parameter(
                    description = "Identifiant de la ville à modifier",
                    example = "123",
                    required = true
            ) @PathVariable Long id,

            @Parameter(
                    description = "Nouvelles données de la ville",
                    required = true
            ) @Valid @RequestBody VilleDTO villeDTO
    );

    @Operation(
            summary = "🗑️ Supprimer une ville",
            description = """
            **Supprime définitivement une ville du système.**
            
            ⚠️ **Attention :** Cette opération est irréversible !
            
            **Vérifications préalables :**
            - La ville doit exister
            - Aucune référence externe ne doit pointer vers cette ville
            
            **Code de retour :** 204 No Content en cas de succès.
            """,
            tags = {"🏙️ Villes", "📋 CRUD"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "✅ Ville supprimée avec succès"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ville non trouvée",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "❌ Impossible de supprimer (contraintes d'intégrité)",
                    content = @Content()
            )
    })
    void deleteVille(
            @Parameter(
                    description = "Identifiant de la ville à supprimer",
                    example = "123",
                    required = true
            ) @PathVariable Long id
    );

    // ==================== RECHERCHES SPÉCIALISÉES ====================

    @Operation(
            summary = "🔍 Rechercher une ville par nom exact",
            description = """
            **Trouve une ville par son nom exact (insensible à la casse).**
            
            Cette recherche est exacte mais insensible à la casse et aux accents.
            
            **Exemples :**
            - `nom=Paris` → Trouve "Paris"
            - `nom=saint-étienne` → Trouve "Saint-Étienne"
            - `nom=MARSEILLE` → Trouve "Marseille"
            """,
            tags = {"🏙️ Villes", "🔍 Recherche"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Ville trouvée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Aucune ville trouvée avec ce nom",
                    content = @Content()
            )
    })
    VilleDTO findByNom(
            @Parameter(
                    description = "Nom exact de la ville recherchée",
                    example = "Paris",
                    required = true,
                    schema = @Schema(minLength = 1, maxLength = 100)
            ) @RequestParam String nom
    );

    @Operation(
            summary = "🔍 Rechercher des villes par nom partiel",
            description = """
            **Trouve toutes les villes dont le nom contient la chaîne recherchée.**
            
            Recherche insensible à la casse et aux accents.
            Les résultats sont triés par nom de ville.
            
            **Exemples d'usage :**
            - `nom=Saint` → Toutes les villes contenant "Saint"
            - `nom=sur-mer` → Toutes les villes "sur-mer"
            - `nom=ville` → Toutes les villes avec "ville" dans le nom
            """,
            tags = {"🏙️ Villes", "🔍 Recherche"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Liste des villes correspondantes",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Paramètre de recherche invalide",
                    content = @Content()
            )
    })
    List<VilleDTO> findByNomContaining(
            @Parameter(
                    description = "Fragment du nom à rechercher",
                    example = "Saint",
                    required = true,
                    schema = @Schema(minLength = 1, maxLength = 50)
            ) @RequestParam String nom
    );

    @Operation(
            summary = "🔍 Rechercher des villes par préfixe de nom",
            description = """
            **Trouve toutes les villes dont le nom commence par le préfixe donné.**
            
            Utile pour l'auto-complétion et les recherches par lettre.
            Résultats triés alphabétiquement.
            
            **Cas d'usage :**
            - Système d'auto-complétion
            - Navigation alphabétique
            - Statistiques par première lettre
            """,
            tags = {"🏙️ Villes", "🔍 Recherche"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Villes commençant par le préfixe",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            )
    })
    List<VilleDTO> findByNomStartingWith(
            @Parameter(
                    description = "Préfixe du nom de ville",
                    example = "Mont",
                    required = true,
                    schema = @Schema(minLength = 1, maxLength = 20)
            ) @RequestParam String prefix
    );

    // ==================== RECHERCHES PAR POPULATION ====================

    @Operation(
            summary = "📊 Villes avec population supérieure à un seuil",
            description = """
            **Récupère toutes les villes avec une population supérieure au minimum spécifié.**
            
            Les résultats sont triés par population décroissante.
            Utile pour analyser les grandes agglomérations.
            
            **Applications :**
            - Études démographiques
            - Seuils de services publics
            - Analyses urbaines
            """,
            tags = {"🏙️ Villes", "📊 Population"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Liste des villes avec population supérieure au seuil",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Valeur de population invalide",
                    content = @Content()
            )
    })
    List<VilleDTO> findByPopulationGreaterThan(
            @Parameter(
                    description = "Population minimum (exclusive)",
                    example = "100000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam Integer min
    );

    @Operation(
            summary = "📊 Villes avec population dans une plage donnée",
            description = """
            **Récupère les villes avec une population comprise entre deux valeurs.**
            
            Bornes inclusives : min ≤ population ≤ max
            Résultats triés par population décroissante.
            
            **Exemples d'usage :**
            - Villes moyennes : 10000-50000 habitants
            - Grandes villes : 100000-500000 habitants
            - Métropoles : > 500000 habitants
            """,
            tags = {"🏙️ Villes", "📊 Population"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Villes dans la plage de population",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Plage de population invalide (min > max)",
                    content = @Content()
            )
    })
    List<VilleDTO> findByPopulationBetween(
            @Parameter(
                    description = "Population minimum (inclusive)",
                    example = "50000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam Integer min,

            @Parameter(
                    description = "Population maximum (inclusive)",
                    example = "200000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam Integer max
    );

    // ==================== RECHERCHES PAR DÉPARTEMENT ====================

    @Operation(
            summary = "🏛️ Villes d'un département avec critères de population",
            description = """
            **Récupère les villes d'un département, optionnellement filtrées par population minimum.**
            
            Si aucun minimum n'est spécifié, retourne toutes les villes du département.
            Résultats triés par population décroissante.
            
            **Utilisation :**
            - Toutes les villes : `/villes/departement/34`
            - Avec minimum : `/villes/departement/34?min=5000`
            """,
            tags = {"🏙️ Villes", "🏛️ Département"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Villes du département",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Département non trouvé",
                    content = @Content()
            )
    })
    List<VilleDTO> findByDepartementAndMinPopulation(
            @Parameter(
                    description = "Code du département",
                    example = "34",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            pattern = "^[0-9]{1,3}[AB]?$",
                            examples = {"34", "75", "2A", "974"}
                    )
            ) @PathVariable String code,

            @Parameter(
                    description = "Population minimum (optionnel)",
                    example = "10000",
                    required = false,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam(required = false) Integer min
    );

    @Operation(
            summary = "🏛️ Villes d'un département dans une plage de population",
            description = """
            **Récupère les villes d'un département avec population dans une plage spécifique.**
            
            Combine filtrage géographique et démographique.
            Bornes inclusives : min ≤ population ≤ max
            
            **Applications :**
            - Analyse des villes moyennes d'une région
            - Identification des centres urbains secondaires
            - Études de répartition démographique
            """,
            tags = {"🏙️ Villes", "🏛️ Département", "📊 Population"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Villes du département dans la plage",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Paramètres invalides",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Département non trouvé",
                    content = @Content()
            )
    })
    List<VilleDTO> findByDepartementAndPopulationRange(
            @Parameter(
                    description = "Code du département",
                    example = "13",
                    required = true
            ) @PathVariable String code,

            @Parameter(
                    description = "Population minimum (inclusive)",
                    example = "20000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam Integer min,

            @Parameter(
                    description = "Population maximum (inclusive)",
                    example = "100000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam Integer max
    );

    @Operation(
            summary = "🏆 Top N des villes les plus peuplées d'un département",
            description = """
            **Récupère les N villes les plus peuplées d'un département donné.**
            
            Classement par population décroissante.
            Parfait pour identifier les centres urbains principaux.
            
            **Cas d'usage :**
            - Identification des pôles urbains
            - Analyses de hiérarchie urbaine
            - Planification territoriale
            """,
            tags = {"🏙️ Villes", "🏛️ Département", "🏆 Top"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Top N des villes du département",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class)),
                            examples = @ExampleObject(
                                    name = "Top 3 Hérault",
                                    value = """
                        [
                          {
                            "id": 15,
                            "nom": "Montpellier",
                            "nbHabitants": 295542,
                            "departement": {"code": "34", "nom": "Hérault"}
                          },
                          {
                            "id": 127,
                            "nom": "Béziers", 
                            "nbHabitants": 77177,
                            "departement": {"code": "34", "nom": "Hérault"}
                          },
                          {
                            "id": 891,
                            "nom": "Sète",
                            "nbHabitants": 44270,
                            "departement": {"code": "34", "nom": "Hérault"}
                          }
                        ]
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Département non trouvé",
                    content = @Content()
            )
    })
    List<VilleDTO> findTopNVillesByDepartement(
            @Parameter(
                    description = "Code du département",
                    example = "34",
                    required = true
            ) @PathVariable String code,

            @Parameter(
                    description = "Nombre de villes à retourner",
                    example = "10",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
            ) @RequestParam(defaultValue = "10") int n
    );

    // ==================== STATISTIQUES ====================

    @Operation(
            summary = "📈 Nombre total de villes",
            description = """
            **Retourne le nombre total de villes enregistrées dans le système.**
            
            Utile pour les tableaux de bord et les statistiques générales.
            """,
            tags = {"🏙️ Villes", "📈 Statistiques"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Nombre total de villes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "integer", format = "int64", example = "34827")
                    )
            )
    })
    long getTotalCount();

    @Operation(
            summary = "📊 Statistiques détaillées d'un département",
            description = """
            **Récupère les statistiques complètes d'un département.**
            
            **Informations retournées :**
            - Nombre total de villes
            - Population totale
            - Population moyenne
            - Ville la plus/moins peuplée
            - Répartition par taille de ville
            
            **Applications :** Tableaux de bord, analyses territoriales, études démographiques.
            """,
            tags = {"🏙️ Villes", "🏛️ Département", "📊 Statistiques"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Statistiques du département",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleRepositoryHelper.DepartementStats.class),
                            examples = @ExampleObject(
                                    name = "Stats Hérault",
                                    value = """
                        {
                          "codeDepartement": "34",
                          "nomDepartement": "Hérault",
                          "nombreVilles": 342,
                          "populationTotale": 1192605,
                          "populationMoyenne": 3487,
                          "villeLaPluspeuplee": "Montpellier",
                          "populationMax": 295542,
                          "villeLaMoinspeuplee": "Celles",
                          "populationMin": 26
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Département non trouvé",
                    content = @Content()
            )
    })
    VilleRepositoryHelper.DepartementStats getDepartementStats(
            @Parameter(
                    description = "Code du département",
                    example = "34",
                    required = true
            ) @PathVariable String code
    );

    @Operation(
            summary = "🏆 Ville la plus peuplée d'un département",
            description = """
            **Identifie la ville ayant la plus grande population dans un département donné.**
            
            Retourne les détails complets de la ville championne.
            En cas d'égalité, retourne la première trouvée par ordre alphabétique.
            """,
            tags = {"🏙️ Villes", "🏛️ Département", "🏆 Records"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Ville la plus peuplée trouvée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Département non trouvé ou sans villes",
                    content = @Content()
            )
    })
    VilleDTO getMostPopulatedVilleInDepartement(
            @Parameter(
                    description = "Code du département",
                    example = "75",
                    required = true
            ) @PathVariable String code
    );

    // ==================== GESTION AVANCÉE ====================

    @Operation(
            summary = "🔄 Mettre à jour uniquement la population d'une ville",
            description = """
            **Met à jour seulement le nombre d'habitants d'une ville.**
            
            Opération optimisée pour les mises à jour fréquentes de population
            (recensements, estimations, corrections).
            
            **Avantages :**
            - Plus rapide qu'une mise à jour complète
            - Pas de risque de modifier d'autres champs
            - Historique des changements possible
            """,
            tags = {"🏙️ Villes", "✏️ Mise à jour"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Population mise à jour",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Nouvelle population invalide",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "❌ Ville non trouvée",
                    content = @Content()
            )
    })
    VilleDTO updatePopulation(
            @Parameter(
                    description = "Identifiant de la ville",
                    example = "123",
                    required = true
            ) @PathVariable Long id,

            @Parameter(
                    description = "Nouveau nombre d'habitants",
                    example = "150000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0", maximum = "20000000")
            ) @RequestParam Integer nouveauNb
    );

    @Operation(
            summary = "⚡ Création rapide d'une ville",
            description = """
            **Crée rapidement une ville avec les paramètres essentiels.**
            
            Alternative à la création complète par JSON, utilise des paramètres URL simples.
            Parfait pour les formulaires web ou les imports automatisés.
            
            **Validation :**
            - Nom unique dans le département
            - Département existant
            - Population >= 0
            """,
            tags = {"🏙️ Villes", "⚡ Création rapide"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "✅ Ville créée rapidement",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VilleDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Paramètres invalides",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "❌ Ville déjà existante ou département inexistant",
                    content = @Content()
            )
    })
    VilleDTO createVilleRapide(
            @Parameter(
                    description = "Nom de la ville",
                    example = "Nouvelle-Cité",
                    required = true,
                    schema = @Schema(minLength = 2, maxLength = 100)
            ) @RequestParam String nom,

            @Parameter(
                    description = "Nombre d'habitants",
                    example = "25000",
                    required = true,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam Integer nbHabitants,

            @Parameter(
                    description = "Code du département",
                    example = "34",
                    required = true
            ) @RequestParam String codeDepartement
    );


    // ==================== RECHERCHE MULTI-CRITÈRES ====================

    @Operation(
            summary = "🔍 Recherche avancée multi-critères",
            description = """
            **Recherche flexible combinant plusieurs critères optionnels.**
            
            **Critères disponibles :**
            - `nom` : Nom de ville (recherche partielle)
            - `minPop` : Population minimum
            - `maxPop` : Population maximum
            - `dept` : Code département
            
            **Logique :** Les critères sont combinés avec ET logique.
            Si aucun critère n'est fourni, retourne toutes les villes.
            
            **Exemples :**
            - `?nom=Saint&dept=34` → Villes avec "Saint" dans l'Hérault
            - `?minPop=50000&maxPop=200000` → Villes moyennes de France
            - `?dept=75&minPop=100000` → Grandes villes de Paris
            """,
            tags = {"🏙️ Villes", "🔍 Recherche avancée"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Résultats de recherche (peut être vide)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Critères de recherche invalides",
                    content = @Content()
            )
    })
    List<VilleDTO> rechercheAvancee(
            @Parameter(
                    description = "Fragment du nom de ville (optionnel)",
                    example = "Mont",
                    required = false,
                    schema = @Schema(minLength = 1, maxLength = 50)
            ) @RequestParam(required = false) String nom,

            @Parameter(
                    description = "Population minimum (optionnel)",
                    example = "10000",
                    required = false,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam(required = false) Integer minPop,

            @Parameter(
                    description = "Population maximum (optionnel)",
                    example = "100000",
                    required = false,
                    schema = @Schema(type = "integer", minimum = "0")
            ) @RequestParam(required = false) Integer maxPop,

            @Parameter(
                    description = "Code département (optionnel)",
                    example = "34",
                    required = false,
                    schema = @Schema(type = "string", pattern = "^[0-9]{1,3}[AB]?$")
            ) @RequestParam(required = false) String dept
    );
}