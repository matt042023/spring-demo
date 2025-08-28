package fr.diginamic.hello.swagger;

import fr.diginamic.hello.dto.DepartementDTO;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interface de documentation Swagger pour les opérations sur les départements français.
 * Cette interface définit toutes les annotations de documentation OpenAPI/Swagger
 * pour l'API de gestion des départements français.
 *
 * <p>Le contrôleur DepartementController hérite de cette interface pour bénéficier
 * automatiquement de toute la documentation API sans polluer le code métier.</p>
 *
 * <p><strong>Fonctionnalités documentées :</strong></p>
 * <ul>
 *   <li>CRUD complet des départements</li>
 *   <li>Recherches par code, nom, type géographique</li>
 *   <li>Statistiques démographiques et territoriales</li>
 *   <li>Gestion des relations avec les villes</li>
 *   <li>Classifications par zones géographiques</li>
 * </ul>
 *
 * @author Matthieu - Développeur Full Stack
 * @version 1.0.0
 * @since 2025-08-26
 */
@Tag(name = "🏛️ Départements", description = """
    **API de gestion des départements français**
    
    Cette API permet de gérer l'ensemble des départements français avec leurs spécificités :
    - **Données** : code, nom, classification géographique
    - **Recherches** : par code, nom, type (métropolitain, outre-mer, Corse)
    - **Relations** : gestion des villes associées
    - **Statistiques** : analyses démographiques et territoriales
    
    **Classification des départements :**
    - **Métropolitains** : 01-95 (sauf 20, 2A, 2B)
    - **Outre-mer** : 971, 972, 973, 974, 975, 976
    - **Corse** : 2A (Corse-du-Sud), 2B (Haute-Corse)
    - **Collectivités** : codes spéciaux (Saint-Pierre, Mayotte, etc.)
    
    **Codes d'erreur :**
    - `400` : Paramètres invalides
    - `404` : Département non trouvé
    - `422` : Erreur métier (code déjà existant)
    """)
public interface SwaggerDepartementController {

    // ==================== CRUD DE BASE ====================

    @Operation(summary = "📋 Récupérer tous les départements avec pagination", description = """
        **Récupère la liste paginée de tous les départements français.**
        
        Support de la pagination et du tri pour une navigation efficace.
        
        **Options de tri disponibles :**
        - `nom` : Tri alphabétique par nom (défaut)
        - `code` : Tri par code département
        - `population` : Tri par population totale
        - `nombreVilles` : Tri par nombre de villes
        
        **Exemples d'usage :**
        - `GET /departements` → Première page, tri par nom
        - `GET /departements?page=1&size=30&sort=population` → Tri par population
        - `GET /departements?sort=code` → Ordre des codes département
        """, tags = {"🏛️ Départements", "📋 CRUD"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Liste des départements récupérée",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(name = "Exemple paginé", value = """
                    {
                      "content": [
                        {"id": 1, "code": "01", "nom": "Ain"},
                        {"id": 2, "code": "02", "nom": "Aisne"}
                      ],
                      "pageable": {"pageNumber": 0, "pageSize": 20, "sort": {"sorted": true}},
                      "totalElements": 101, "totalPages": 6
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "❌ Paramètres de pagination invalides", content = @Content())
    })
    Page<DepartementDTO> getAllDepartements(
            @Parameter(description = "Numéro de la page (commence à 0)", example = "0", schema = @Schema(minimum = "0", defaultValue = "0")) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20", schema = @Schema(minimum = "1", maximum = "100", defaultValue = "20")) @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Critère de tri", example = "nom", schema = @Schema(defaultValue = "nom", allowableValues = {"nom", "code", "population", "nombreVilles"})) @RequestParam(defaultValue = "nom") String sort
    );

    @Operation(summary = "🔍 Récupérer un département par son identifiant", description = """
        **Récupère les détails complets d'un département par son ID technique.**
        
        Retourne toutes les informations du département : code, nom officiel, statistiques associées si disponibles.
        
        **Usage recommandé :** Préférer la recherche par code département qui est plus stable et intuitive.
        """, tags = {"🏛️ Départements", "📋 CRUD"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Département trouvé",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class),
                            examples = @ExampleObject(name = "Département Hérault", value = """
                    {"id": 34, "code": "34", "nom": "Hérault"}
                    """))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionFonctionnelle.class)))
    })
    DepartementDTO getDepartementById(
            @Parameter(description = "Identifiant technique du département", example = "34", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "1")) @PathVariable Long id
    );

    @Operation(summary = "🔍 Récupérer un département par son code officiel", description = """
        **Récupère un département par son code officiel (méthode recommandée).**
        
        Les codes départementaux sont stables et correspondent aux usages administratifs :
        - Codes métropolitains : 01 à 95 (sauf 20)
        - Codes Corse : 2A, 2B  
        - Codes outre-mer : 971, 972, 973, 974, 975, 976
        
        **Avantage :** Plus intuitif que l'ID technique, stable dans le temps.
        """, tags = {"🏛️ Départements", "📋 CRUD"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Département trouvé par code",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class),
                            examples = @ExampleObject(name = "Paris par code", value = """
                    {"id": 75, "code": "75", "nom": "Paris"}
                    """))),
            @ApiResponse(responseCode = "404", description = "❌ Code département non trouvé", content = @Content())
    })
    DepartementDTO getDepartementByCode(
            @Parameter(description = "Code officiel du département", example = "34", required = true, schema = @Schema(type = "string", pattern = "^([0-9]{1,3}|2[AB])$")) @PathVariable String code
    );

    @Operation(summary = "➕ Créer un nouveau département", description = """
        **Crée un nouveau département dans le système.**
        
        ⚠️ **Usage limité :** Cette fonction est principalement destinée à l'administration du système. 
        Les départements français sont normalement prédéfinis et stables.
        
        **Validations :**
        - Code unique et valide
        - Nom non vide si fourni
        - Respect du format des codes départementaux
        """, tags = {"🏛️ Départements", "📋 CRUD"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "✅ Département créé avec succès", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class))),
            @ApiResponse(responseCode = "400", description = "❌ Données invalides", content = @Content()),
            @ApiResponse(responseCode = "422", description = "❌ Code département déjà existant", content = @Content())
    })
    DepartementDTO createDepartement(
            @Parameter(description = "Données du nouveau département", required = true, schema = @Schema(implementation = DepartementDTO.class)) @Valid @RequestBody DepartementDTO departementDTO
    );

    @Operation(summary = "✏️ Mettre à jour un département existant", description = """
        **Met à jour les informations d'un département existant.**
        
        ⚠️ **Attention :** La modification du code département peut avoir des impacts sur l'intégrité référentielle avec les villes associées.
        
        **Champs modifiables :**
        - Nom du département
        - Code (avec précaution)
        
        **L'ID dans l'URL prime sur l'ID dans le body.**
        """, tags = {"🏛️ Départements", "📋 CRUD"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Département mis à jour", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class))),
            @ApiResponse(responseCode = "400", description = "❌ Données invalides", content = @Content()),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content()),
            @ApiResponse(responseCode = "422", description = "❌ Nouveau code déjà existant", content = @Content())
    })
    DepartementDTO updateDepartement(
            @Parameter(description = "Identifiant du département à modifier", example = "34", required = true) @PathVariable Long id,
            @Parameter(description = "Nouvelles données du département", required = true) @Valid @RequestBody DepartementDTO departementDTO
    );

    @Operation(summary = "🗑️ Supprimer un département", description = """
        **Supprime définitivement un département du système.**
        
        ⚠️ **DANGER :** Cette opération est irréversible et peut affecter l'intégrité des données si des villes sont associées au département.
        
        **Vérifications préalables recommandées :**
        - Aucune ville n'est associée au département
        - Aucune référence externe
        
        **Usage :** Principalement pour la maintenance administrative.
        """, tags = {"🏛️ Départements", "📋 CRUD"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "✅ Département supprimé"),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content()),
            @ApiResponse(responseCode = "409", description = "❌ Impossible de supprimer (contraintes d'intégrité)", content = @Content())
    })
    void deleteDepartement(
            @Parameter(description = "Identifiant du département à supprimer", example = "34", required = true) @PathVariable Long id
    );

    // ==================== RECHERCHES SPÉCIALISÉES ====================

    @Operation(summary = "🔍 Rechercher un département par nom exact", description = """
        **Trouve un département par son nom officiel exact.**
        
        Recherche insensible à la casse et aux accents.
        
        **Exemples valides :**
        - `nom=Hérault` → Trouve "Hérault"
        - `nom=bouches-du-rhône` → Trouve "Bouches-du-Rhône"
        - `nom=PARIS` → Trouve "Paris"
        """, tags = {"🏛️ Départements", "🔍 Recherche"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Département trouvé", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé avec ce nom", content = @Content())
    })
    DepartementDTO findByNom(
            @Parameter(description = "Nom exact du département", example = "Hérault", required = true, schema = @Schema(minLength = 2, maxLength = 50)) @RequestParam String nom
    );

    @Operation(summary = "🔍 Recherche globale par nom ou code", description = """
        **Recherche flexible par nom ou code de département.**
        
        Effectue une recherche partielle insensible à la casse sur :
        - Le nom du département
        - Le code du département
        
        **Exemples d'usage :**
        - `q=hérault` → Trouve "Hérault" par nom
        - `q=34` → Trouve département code "34"
        - `q=rhô` → Trouve "Bouches-du-Rhône" par fragment
        - `q=2a` → Trouve "Corse-du-Sud" par code
        """, tags = {"🏛️ Départements", "🔍 Recherche"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Résultats de recherche (peut être vide)", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)))),
            @ApiResponse(responseCode = "400", description = "❌ Terme de recherche invalide", content = @Content())
    })
    List<DepartementDTO> searchDepartements(
            @Parameter(description = "Terme de recherche (nom ou code partiel)", example = "rhône", required = true, schema = @Schema(minLength = 1, maxLength = 30)) @RequestParam String q
    );

    @Operation(summary = "📝 Départements avec nom renseigné", description = """
        **Récupère tous les départements ayant un nom officiel renseigné.**
        
        Filtre les départements où le champ nom n'est pas null ou vide.
        Utile pour identifier les départements correctement configurés.
        
        **Usage :** Contrôle qualité des données, rapports d'administration.
        """, tags = {"🏛️ Départements", "📊 Qualité des données"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements avec nom", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class))))
    })
    List<DepartementDTO> getDepartementsWithNom();

    @Operation(summary = "❓ Départements sans nom renseigné", description = """
        **Récupère tous les départements sans nom officiel.**
        
        Identifie les départements où le champ nom est null ou vide.
        
        **Applications :**
        - Audit de qualité des données
        - Identification des manques à corriger
        - Maintenance des référentiels
        """, tags = {"🏛️ Départements", "📊 Qualité des données"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements sans nom", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class))))
    })
    List<DepartementDTO> getDepartementsWithoutNom();

    @Operation(summary = "🏙️ Départements ayant des villes", description = """
        **Récupère tous les départements qui ont au moins une ville associée.**
        
        Filtre basé sur les relations avec les villes. Exclut les départements "vides" ou administratifs.
        
        **Utilité :** Analyses territoriales, statistiques démographiques.
        """, tags = {"🏛️ Départements", "🔗 Relations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements avec villes", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class))))
    })
    List<DepartementDTO> getDepartementsWithVilles();

    @Operation(summary = "📊 Départements avec nombre minimum de villes", description = """
        **Récupère les départements ayant au moins N villes.**
        
        Filtre par seuil de nombre de communes. Utile pour identifier les départements de tailles différentes.
        
        **Exemples d'analyse :**
        - `min=500` → Départements très peuplés en communes
        - `min=100` → Départements de taille moyenne
        - `min=10` → Exclut les micro-départements
        """, tags = {"🏛️ Départements", "📊 Statistiques"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements avec minimum de villes", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)))),
            @ApiResponse(responseCode = "400", description = "❌ Nombre minimum invalide", content = @Content())
    })
    List<DepartementDTO> getDepartementsWithMinVilles(
            @Parameter(description = "Nombre minimum de villes", example = "100", required = true, schema = @Schema(type = "integer", minimum = "1")) @RequestParam int min
    );

    @Operation(summary = "👥 Départements avec population minimum", description = """
        **Récupère les départements ayant une population totale supérieure au seuil.**
        
        Calcul basé sur la somme des populations de toutes les villes du département.
        
        **Applications :**
        - Identification des grands départements démographiques
        - Seuils pour politiques publiques
        - Analyses de répartition territoriale
        
        **Exemples :**
        - `min=1000000` → Départements millionnaires
        - `min=500000` → Départements de forte densité
        """, tags = {"🏛️ Départements", "👥 Démographie"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements avec population minimum", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)))),
            @ApiResponse(responseCode = "400", description = "❌ Seuil de population invalide", content = @Content())
    })
    List<DepartementDTO> getDepartementsWithMinPopulation(
            @Parameter(description = "Population totale minimum", example = "500000", required = true, schema = @Schema(type = "integer", format = "int64", minimum = "1")) @RequestParam Long min
    );

    // ==================== CLASSIFICATION GÉOGRAPHIQUE ====================

    @Operation(summary = "🇫🇷 Départements métropolitains", description = """
        **Récupère tous les départements de France métropolitaine.**
        
        **Critères :** Codes 01 à 95, plus 2A et 2B (Corse), à l'exclusion des départements d'outre-mer.
        
        **Usage :** Analyses territoires continentaux, statistiques métropole vs outre-mer.
        """, tags = {"🏛️ Départements", "🗺️ Géographie"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements métropolitains", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)),
                    examples = @ExampleObject(name = "Exemple métropolitains", value = """
                [
                  {"code": "01", "nom": "Ain"},
                  {"code": "75", "nom": "Paris"},
                  {"code": "2A", "nom": "Corse-du-Sud"}
                ]
                """)))
    })
    List<DepartementDTO> getDepartementsMetropolitains();

    @Operation(summary = "🌴 Départements d'outre-mer", description = """
        **Récupère tous les départements et régions d'outre-mer (DROM).**
        
        **Codes concernés :**
        - 971 : Guadeloupe
        - 972 : Martinique  
        - 973 : Guyane
        - 974 : La Réunion
        - 975 : Saint-Pierre-et-Miquelon
        - 976 : Mayotte
        
        **Applications :** Politiques spécifiques outre-mer, statistiques insulaires.
        """, tags = {"🏛️ Départements", "🌴 Outre-mer"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements d'outre-mer", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)),
                    examples = @ExampleObject(name = "Exemple outre-mer", value = """
                [
                  {"code": "971", "nom": "Guadeloupe"},
                  {"code": "972", "nom": "Martinique"},
                  {"code": "974", "nom": "La Réunion"}
                ]
                """)))
    })
    List<DepartementDTO> getDepartementsOutreMer();

    @Operation(summary = "🏔️ Départements corses", description = """
        **Récupère les deux départements de Corse.**
        
        **Départements :**
        - 2A : Corse-du-Sud (Ajaccio)
        - 2B : Haute-Corse (Bastia)
        
        **Particularité :** Codes alphanumériques uniques en métropole.
        """, tags = {"🏛️ Départements", "🏔️ Corse"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements corses", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)),
                    examples = @ExampleObject(name = "Départements Corse", value = """
                [
                  {"code": "2A", "nom": "Corse-du-Sud"},
                  {"code": "2B", "nom": "Haute-Corse"}
                ]
                """)))
    })
    List<DepartementDTO> getDepartementsCorse();

    @Operation(summary = "🔍 Départements par préfixe de code", description = """
        **Récupère les départements dont le code commence par un préfixe donné.**
        
        **Exemples d'usage :**
        - `prefix=97` → Tous les départements d'outre-mer
        - `prefix=0` → Départements 01 à 09
        - `prefix=2` → Départements 20-29 + 2A, 2B
        - `prefix=1` → Départements 10-19
        
        **Applications :** Regroupements régionaux, analyses par zones.
        """, tags = {"🏛️ Départements", "🔍 Recherche"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Départements avec préfixe", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DepartementDTO.class)))),
            @ApiResponse(responseCode = "400", description = "❌ Préfixe invalide", content = @Content())
    })
    List<DepartementDTO> findByCodeStartingWith(
            @Parameter(description = "Préfixe du code département", example = "97", required = true, schema = @Schema(type = "string", minLength = 1, maxLength = 3, pattern = "^[0-9]{1,2}[AB]?$")) @RequestParam String prefix
    );

    // ==================== RELATIONS AVEC LES VILLES ====================

    @Operation(summary = "🏙️ Toutes les villes d'un département (par ID)", description = """
        **Récupère toutes les villes associées à un département par son identifiant.**
        
        **Format de réponse :** Liste complète des villes avec leurs détails.
        **Tri :** Par nom de ville (ordre alphabétique).
        
        ℹ️ **Recommandation :** Utiliser plutôt la recherche par code département.
        """, tags = {"🏛️ Départements", "🏙️ Villes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Villes du département", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class)))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    List<VilleDTO> getVillesByDepartement(
            @Parameter(description = "Identifiant du département", example = "34", required = true) @PathVariable Long id
    );

    @Operation(summary = "🏙️ Toutes les villes d'un département (par code)", description = """
        **Récupère toutes les villes d'un département par son code officiel.**
        
        **Méthode recommandée** pour récupérer les villes d'un département.
        Plus stable et intuitive que la recherche par ID.
        
        **Applications :**
        - Export de données départementales
        - Analyses territoriales locales
        - Listes de référence par département
        """, tags = {"🏛️ Départements", "🏙️ Villes"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Villes du département", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class)))),
            @ApiResponse(responseCode = "404", description = "❌ Code département non trouvé", content = @Content())
    })
    List<VilleDTO> getVillesByDepartementCode(
            @Parameter(description = "Code officiel du département", example = "34", required = true, schema = @Schema(pattern = "^([0-9]{1,3}|2[AB])$")) @PathVariable String code
    );

    @Operation(summary = "🏆 Top N des villes d'un département", description = """
        **Récupère les N villes les plus peuplées d'un département.**
        
        **Classement :** Par population décroissante.
        **Usage typique :** Identification des centres urbains principaux.
        
        **Applications :**
        - Hiérarchie urbaine départementale
        - Pôles d'attractivité
        - Planification territoriale
        """, tags = {"🏛️ Départements", "🏆 Top"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Top des villes du département", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class)))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    List<VilleDTO> getTopVillesByDepartement(
            @Parameter(description = "Code du département", example = "34", required = true) @PathVariable String code,
            @Parameter(description = "Nombre de villes à retourner", example = "10", schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")) @RequestParam(defaultValue = "10") int n
    );

    @Operation(summary = "👥 Villes avec population minimum d'un département", description = """
        **Récupère les villes d'un département ayant une population supérieure au seuil.**
        
        **Paramètres :**
        - Si `min` fourni : villes avec population ≥ min
        - Si `min` non fourni : toutes les villes du département
        
        **Tri :** Par population décroissante.
        """, tags = {"🏛️ Départements", "👥 Population"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Villes avec population minimum", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class)))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    List<VilleDTO> getVillesByDepartementAndPopulation(
            @Parameter(description = "Code du département", example = "34", required = true) @PathVariable String code,
            @Parameter(description = "Population minimum (optionnel)", example = "10000", required = false, schema = @Schema(type = "integer", minimum = "0")) @RequestParam(required = false) Integer min
    );

    @Operation(summary = "📊 Villes dans une plage de population d'un département", description = """
        **Récupère les villes d'un département avec population dans une plage donnée.**
        
        **Critères :** min ≤ population ≤ max
        **Tri :** Par population décroissante.
        
        **Exemples d'usage :**
        - Villes moyennes : 10000-50000 hab
        - Petites villes : 1000-10000 hab
        - Centres urbains : 50000-200000 hab
        """, tags = {"🏛️ Départements", "📊 Population"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Villes dans la plage de population", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VilleDTO.class)))),
            @ApiResponse(responseCode = "400", description = "❌ Plage de population invalide", content = @Content()),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    List<VilleDTO> getVillesByDepartementAndPopulationRange(
            @Parameter(description = "Code du département", example = "13", required = true) @PathVariable String code,
            @Parameter(description = "Population minimum (inclusive)", example = "10000", required = true, schema = @Schema(type = "integer", minimum = "0")) @RequestParam Integer min,
            @Parameter(description = "Population maximum (inclusive)", example = "100000", required = true, schema = @Schema(type = "integer", minimum = "0")) @RequestParam Integer max
    );

    // ==================== STATISTIQUES ====================

    @Operation(summary = "📊 Nombre total de départements", description = """
        **Retourne le nombre total de départements enregistrés.**
        
        **Usage :** Statistiques générales, vérification de complétude des données.
        
        **Note :** La France compte officiellement 101 départements (96 métropolitains + 5 d'outre-mer).
        """, tags = {"🏛️ Départements", "📊 Statistiques"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Nombre de départements", content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", format = "int64", example = "101")))
    })
    long getTotalCount();

    @Operation(summary = "📈 Statistiques détaillées d'un département par code", description = """
        **Récupère un tableau de bord statistique complet d'un département.**
        
        **Indicateurs fournis :**
        - Code et nom officiel
        - Nombre total de villes/communes
        - Population totale (somme des villes)
        - Données de contexte administratif
        
        **Applications :**
        - Tableaux de bord territoriaux
        - Rapports démographiques
        - Analyses comparatives inter-départements
        """, tags = {"🏛️ Départements", "📈 Statistiques avancées"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Statistiques du département", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "Stats Hérault", value = """
                {
                  "code": "34",
                  "nom": "Hérault",
                  "nombreVilles": 342,
                  "populationTotale": 1192605
                }
                """))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    Map<String, Object> getStatsByCode(
            @Parameter(description = "Code du département", example = "34", required = true, schema = @Schema(pattern = "^([0-9]{1,3}|2[AB])$")) @PathVariable String code
    );

    @Operation(summary = "👥 Population totale d'un département", description = """
        **Calcule et retourne la population totale d'un département.**
        
        **Calcul :** Somme des populations de toutes les villes du département.
        
        **Applications :**
        - Comparaisons démographiques
        - Indicateurs de développement territorial
        - Bases de calculs statistiques
        """, tags = {"🏛️ Départements", "👥 Démographie"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Population totale calculée", content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", format = "int64", example = "1192605"))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    Long getPopulationTotalByCode(
            @Parameter(description = "Code du département", example = "34", required = true) @PathVariable String code
    );

    @Operation(summary = "🏘️ Nombre de villes d'un département", description = """
        **Compte le nombre de villes/communes d'un département.**
        
        **Résultat :** Nombre entier de communes associées au département.
        
        **Applications :**
        - Analyses de maillage territorial
        - Comparaisons de densité administrative
        - Statistiques de gouvernance locale
        """, tags = {"🏛️ Départements", "🏘️ Territoires"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Nombre de villes compté", content = @Content(mediaType = "application/json", schema = @Schema(type = "integer", format = "int64", example = "342"))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    Long getNombreVillesByCode(
            @Parameter(description = "Code du département", example = "34", required = true) @PathVariable String code
    );

    // ==================== GESTION AVANCÉE ====================

    @Operation(summary = "⚡ Création rapide d'un département", description = """
        **Crée rapidement un département avec les paramètres essentiels.**
        
        Alternative à la création complète par JSON, utilise des paramètres URL simples.
        
        **Paramètres :**
        - Code : obligatoire, unique
        - Nom : optionnel, peut être ajouté ultérieurement
        
        **Validation :** Vérification de l'unicité du code.
        """, tags = {"🏛️ Départements", "⚡ Création rapide"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "✅ Département créé rapidement", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class))),
            @ApiResponse(responseCode = "400", description = "❌ Code invalide", content = @Content()),
            @ApiResponse(responseCode = "422", description = "❌ Code déjà existant", content = @Content())
    })
    DepartementDTO createDepartementRapide(
            @Parameter(description = "Code du département", example = "99", required = true, schema = @Schema(type = "string", pattern = "^([0-9]{1,3}|2[AB])$", minLength = 1, maxLength = 3)) @RequestParam String code,
            @Parameter(description = "Nom du département (optionnel)", example = "Nouveau-Département", required = false, schema = @Schema(minLength = 2, maxLength = 50)) @RequestParam(required = false) String nom
    );

    @Operation(summary = "✏️ Mettre à jour le nom d'un département", description = """
        **Met à jour uniquement le nom d'un département existant.**
        
        Opération optimisée pour corriger ou compléter les noms de départements.
        Plus sûre qu'une mise à jour complète.
        
        **Applications :**
        - Corrections orthographiques
        - Ajout de noms manquants
        - Standardisation des appellations
        """, tags = {"🏛️ Départements", "✏️ Mise à jour"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Nom mis à jour", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DepartementDTO.class))),
            @ApiResponse(responseCode = "400", description = "❌ Nom invalide", content = @Content()),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content())
    })
    DepartementDTO updateNomDepartement(
            @Parameter(description = "Code du département", example = "34", required = true) @PathVariable String code,
            @Parameter(description = "Nouveau nom du département", example = "Hérault", required = true, schema = @Schema(minLength = 2, maxLength = 50)) @RequestParam String nom
    );

    @Operation(summary = "🔧 Mettre à jour tous les noms manquants", description = """
        **Opération de maintenance pour compléter automatiquement les noms manquants.**
        
        ⚠️ **Opération d'administration système :** 
        Cette fonction applique une logique métier prédéfinie pour 
        compléter les noms de départements basés sur leurs codes.
        
        **Process :**
        1. Identifie les départements sans nom
        2. Applique la correspondance code → nom officiel
        3. Met à jour en lot
        
        **Usage :** Maintenance périodique, import de données.
        """, tags = {"🏛️ Départements", "🔧 Maintenance"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Noms mis à jour", content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(name = "Message de succès", value = "15 noms de départements mis à jour avec succès"))),
            @ApiResponse(responseCode = "500", description = "❌ Erreur lors de la mise à jour", content = @Content())
    })
    String updateNomsManquants();

    @Operation(summary = "🔍 Vérifier l'existence d'un département par code", description = """
        **Vérifie si un département existe dans le système par son code.**
        
        **Réponse :** Booléen simple (true/false).
        
        **Applications :**
        - Validation avant création de villes
        - Vérification d'intégrité référentielle
        - Contrôles dans les formulaires
        - Tests de présence avant opérations
        
        **Avantage :** Plus léger qu'une récupération complète des données.
        """, tags = {"🏛️ Départements", "🔍 Validation"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Résultat de vérification", content = @Content(mediaType = "application/json",
                    schema = @Schema(type = "boolean", description = "true si le département existe, false sinon"),
                    examples = @ExampleObject(name = "Département existant", value = "true")))
    })
    boolean existsByCode(
            @Parameter(description = "Code du département à vérifier", example = "34", required = true, schema = @Schema(type = "string", pattern = "^([0-9]{1,3}|2[AB])$")) @PathVariable String code
    );

    // ==================== EXPORT PDF ====================

    @Operation(summary = "📄 Export PDF d'un département", description = """
        **Exporte les détails complets d'un département au format PDF.**
        
        **Contenu du PDF :**
        - Informations du département (code, nom)
        - Liste de toutes les villes avec population
        - Statistiques départementales
        - Mise en forme professionnelle avec tableaux
        
        **Format :** Fichier PDF téléchargeable avec nom horodaté.
        **Utilisation :** Rapports officiels, documentation, archivage.
        
        ⚠️ **Attention :** Peut être volumineux pour les grands départements.
        """, tags = {"🏛️ Départements", "📄 Export"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ PDF généré avec succès",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary", description = "Fichier PDF du département")),
                    headers = @Header(name = "Content-Disposition", description = "Nom du fichier PDF", schema = @Schema(type = "string", example = "attachment; filename=departement_34_20250828_143022.pdf"))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "❌ Erreur lors de la génération du PDF", content = @Content())
    })
    ResponseEntity<byte[]> exportDepartementToPdf(
            @Parameter(description = "Code du département à exporter", example = "34", required = true, schema = @Schema(pattern = "^([0-9]{1,3}|2[AB])$")) @PathVariable String codeDepartement
    );

    @Operation(summary = "👁️ Prévisualisation PDF d'un département", description = """
        **Prévisualise le PDF d'un département directement dans le navigateur.**
        
        **Différence avec l'export :**
        - Pas de téléchargement automatique
        - Affichage inline dans le navigateur
        - Même contenu que l'export PDF
        
        **Utilisation :** Vérification avant téléchargement, prévisualisation rapide.
        """, tags = {"🏛️ Départements", "📄 Export"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ Prévisualisation PDF générée",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary", description = "Fichier PDF à prévisualiser"))),
            @ApiResponse(responseCode = "404", description = "❌ Département non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "❌ Erreur lors de la génération du PDF", content = @Content())
    })
    ResponseEntity<byte[]> previewDepartementPdf(
            @Parameter(description = "Code du département à prévisualiser", example = "34", required = true, schema = @Schema(pattern = "^([0-9]{1,3}|2[AB])$")) @PathVariable String codeDepartement
    );
}