package fr.diginamic.hello.controlers;

import fr.diginamic.hello.dto.DepartementDTO;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.DepartementMapper;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.services.DepartementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les départements - CRUD complet avec validation
 * 
 * Ce contrôleur expose les endpoints REST pour la gestion des départements :
 * - Opérations CRUD de base (Create, Read, Update, Delete)
 * - Endpoints spécialisés pour les villes d'un département
 * - Gestion complète des erreurs avec ResponseEntity
 * - Validation automatique des données avec @Valid
 * 
 * @RestController = @Controller + @ResponseBody
 * Toutes les méthodes renvoient directement des données JSON
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/departements") // URL de base pour toutes les méthodes
public class DepartementController {

    /**
     * Injection de dépendance du service Departement
     * Spring injecte automatiquement une instance de DepartementService
     */
    @Autowired
    private DepartementService departementService;

    /**
     * Injection de dépendance du mapper Departement pour la conversion entité/DTO
     * Spring injecte automatiquement une instance de DepartementMapper
     */
    @Autowired
    private DepartementMapper departementMapper;

    /**
     * Injection de dépendance du mapper Ville pour la conversion entité/DTO
     * Spring injecte automatiquement une instance de VilleMapper
     */
    @Autowired
    private VilleMapper villeMapper;

    // ========== OPÉRATIONS DE LECTURE (READ) ==========

    /**
     * GET /departements - Récupère la liste de tous les départements
     * @return List<Departement> Liste complète des départements
     */
    @GetMapping
    public List<Departement> getAllDepartements() {
        // Délégation au service pour récupérer tous les départements
        return departementService.extractDepartements();
    }

    /**
     * GET /departements/{id} - Récupère un département par son ID
     * @param id identifiant unique du département
     * @return ResponseEntity<Departement> département trouvé ou 404 si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartementDTO> getDepartementById(@PathVariable Long id) {
        // Recherche du département par ID via le service
        Departement departement = departementService.extractDepartement(id);

        // Retour du département si trouvé, sinon erreur 404
        if (departement != null) {
            DepartementDTO departementDTO = departementMapper.toDTO(departement);
            return ResponseEntity.ok(departementDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /departements/code/{code} - Récupère un département par son code
     * @param code code du département à rechercher (ex: \"75\", \"13\")
     * @return ResponseEntity<Departement> département trouvé ou 404 si non trouvé
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<DepartementDTO> getDepartementByCode(@PathVariable String code) {
        // Recherche du département par code via le service
        Departement departement = departementService.extractDepartementByCode(code);

        // Retour du département si trouvé, sinon erreur 404
        if (departement != null) {
            DepartementDTO departementDTO = departementMapper.toDTO(departement);
            return ResponseEntity.ok(departementDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /departements/ordered-by-code - Récupère tous les départements triés par code
     * @return List<Departement> départements triés par code croissant
     */
    @GetMapping("/ordered-by-code")
    public List<DepartementDTO> getDepartementsOrderedByCode() {
        // Délégation au service pour récupérer les départements triés par code
        List<Departement> departements = departementService.findDepartementsOrderByCode();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/ordered-by-nom - Récupère tous les départements triés par nom
     * @return List<Departement> départements triés par nom croissant
     */
    @GetMapping("/ordered-by-nom")
    public List<DepartementDTO> getDepartementsOrderedByNom() {
        // Délégation au service pour récupérer les départements triés par nom
        List<Departement> departements = departementService.findDepartementsOrderByNom();
        return departementMapper.toDTOList(departements);
    }

    // ========== OPÉRATION DE CRÉATION (CREATE) ==========

    /**
     * POST /departements - Crée un nouveau département avec validation
     * @param nouveauDepartement données du nouveau département à créer
     * @param bindingResult résultat de la validation des données
     * @return ResponseEntity<String> message de succès ou d'erreur
     */
    @PostMapping
    public ResponseEntity<?> createDepartement(@Valid @RequestBody DepartementDTO nouveauDepartementDTO,
                                                    BindingResult bindingResult) {
        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            StringBuilder erreurs = new StringBuilder("Erreur de validation : ");
            bindingResult.getAllErrors().forEach(error ->
                    erreurs.append(error.getDefaultMessage()).append(";")
            );
            return ResponseEntity.badRequest().body(erreurs.toString());
        }

        try {
            // Conversion DTO -> Entité
            Departement nouveauDepartement = departementMapper.toEntity(nouveauDepartementDTO);
            // Délégation au service pour l'insertion avec vérifications métier
            List<Departement> departementsAJour = departementService.insertDepartement(nouveauDepartement);
            
            // Retour du département créé en DTO
            Departement departementCree = departementsAJour.stream()
                    .filter(d -> d.getCode().equals(nouveauDepartement.getCode()))
                    .findFirst()
                    .orElse(nouveauDepartement);
            
            DepartementDTO departementCreeDTO = departementMapper.toDTO(departementCree);
            return ResponseEntity.ok(departementCreeDTO);
        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (code ou nom déjà existant, etc.)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== OPÉRATION DE MISE À JOUR (UPDATE) ==========

    /**
     * PUT /departements/{id} - Met à jour un département existant avec validation
     * @param id identifiant du département à modifier
     * @param departementModifie nouvelles données du département
     * @param bindingResult résultat de la validation
     * @return ResponseEntity<String> message de succès ou d'erreur
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateDepartement(@PathVariable Long id,
                                                    @Valid @RequestBody Departement departementModifie,
                                                    BindingResult bindingResult) {

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            StringBuilder erreurs = new StringBuilder("Erreurs de validation : ");
            bindingResult.getAllErrors().forEach(error ->
                    erreurs.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(erreurs.toString());
        }

        try {
            // Délégation au service pour la logique métier de modification
            List<Departement> departementsAJour = departementService.modifierDepartement(id, departementModifie);
            return ResponseEntity.ok("Département mis à jour avec succès");

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (département inexistant, code/nom déjà pris, etc.)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== OPÉRATION DE SUPPRESSION (DELETE) ==========

    /**
     * DELETE /departements/{id} - Supprime un département
     * @param id identifiant du département à supprimer
     * @return ResponseEntity<String> message de succès ou d'erreur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDepartement(@PathVariable Long id) {

        try {
            // Délégation au service pour la logique métier de suppression
            List<Departement> departementsAJour = departementService.supprimerDepartement(id);
            return ResponseEntity.ok("Département supprimé avec succès");

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (département inexistant ou contenant des villes)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== ENDPOINTS SPÉCIALISÉS POUR LES VILLES ==========

    /**
     * GET /departements/{id}/top-villes/{limit} - Liste les n plus grandes villes d'un département
     * @param id identifiant du département
     * @param limit nombre maximum de villes à retourner
     * @return ResponseEntity<List<Ville>> villes triées par population décroissante ou erreur
     */
    @GetMapping("/{id}/top-villes/{limit}")
    public ResponseEntity<List<VilleDTO>> getTopVillesByDepartement(@PathVariable Long id, 
                                                                @PathVariable int limit) {
        try {
            // Validation du paramètre limit
            if (limit <= 0) {
                return ResponseEntity.badRequest().body(null);
            }

            // Délégation au service pour récupérer les plus grandes villes
            List<Ville> topVilles = departementService.getTopVillesByDepartement(id, limit);
            List<VilleDTO> topVillesDTO = villeMapper.toDTOList(topVilles);
            return ResponseEntity.ok(topVillesDTO);

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (département inexistant)
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * GET /departements/{id}/villes-population?min={min}&max={max} - 
     * Liste les villes d'un département avec population dans une fourchette
     * @param id identifiant du département
     * @param min population minimum (optionnel)
     * @param max population maximum (optionnel)
     * @return ResponseEntity<List<Ville>> villes dans la fourchette ou erreur
     */
    @GetMapping("/{id}/villes-population")
    public ResponseEntity<List<VilleDTO>> getVillesByDepartementAndPopulation(
            @PathVariable Long id,
            @RequestParam(required = false) Integer min,
            @RequestParam(required = false) Integer max) {
        
        try {
            // Validation des paramètres de fourchette
            if (min != null && min < 0) {
                return ResponseEntity.badRequest().body(null);
            }
            if (max != null && max < 0) {
                return ResponseEntity.badRequest().body(null);
            }

            // Délégation au service pour récupérer les villes dans la fourchette
            List<Ville> villes = departementService.getVillesByDepartementAndPopulation(id, min, max);
            List<VilleDTO> villesDTO = villeMapper.toDTOList(villes);
            return ResponseEntity.ok(villesDTO);

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (département inexistant, fourchette invalide)
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ========== ENDPOINTS D'ANALYSE ==========

    /**
     * GET /departements/{id}/population-totale - Récupère la population totale d'un département
     * @param id identifiant du département
     * @return ResponseEntity<Long> population totale ou erreur
     */
    @GetMapping("/{id}/population-totale")
    public ResponseEntity<Long> getPopulationTotaleDepartement(@PathVariable Long id) {
        try {
            // Délégation au service pour calculer la population totale
            Long populationTotale = departementService.getPopulationTotaleDepartement(id);
            return ResponseEntity.ok(populationTotale);

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (département inexistant)
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * GET /departements/{id}/nombre-villes - Récupère le nombre de villes d'un département
     * @param id identifiant du département
     * @return ResponseEntity<Long> nombre de villes ou erreur
     */
    @GetMapping("/{id}/nombre-villes")
    public ResponseEntity<Long> getNombreVillesDepartement(@PathVariable Long id) {
        try {
            // Délégation au service pour compter les villes
            Long nombreVilles = departementService.getNombreVillesDepartement(id);
            return ResponseEntity.ok(nombreVilles);

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (département inexistant)
            return ResponseEntity.badRequest().body(null);
        }
    }
}