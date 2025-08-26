package fr.diginamic.hello.controlers;

import fr.diginamic.hello.dto.DepartementDTO;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.DepartementMapper;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.services.DepartementService;
import fr.diginamic.hello.services.VilleService;
import jakarta.validation.Valid;
import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les départements - Version étendue avec Repositories
 *
 * Version mise à jour utilisant les nouveaux repositories et services
 * Compatible avec le style du VilleController.
 *
 * Nouvelles fonctionnalités :
 * - Recherches par type de département (métropolitain, outre-mer, Corse)
 * - Statistiques avancées
 * - Gestion des noms de départements
 * - Routes cohérentes avec VilleController
 *
 * @RestController = @Controller + @ResponseBody
 * Toutes les méthodes renvoient directement des données JSON
 *
 * @author Votre nom
 * @version 2.0 - Migration vers Repositories
 * @since 1.0
 */
@RestController
@RequestMapping("/departements") // URL de base pour toutes les méthodes
public class DepartementController {

    // ==================== INJECTION DES DÉPENDANCES ====================

    @Autowired
    private DepartementService departementService;

    @Autowired
    private VilleService villeService;

    @Autowired
    private DepartementMapper departementMapper;

    @Autowired
    private VilleMapper villeMapper;

    // ==================== ROUTES CRUD DE BASE ====================

    /**
     * GET /departements - Récupère tous les départements avec pagination
     * @param page numéro de la page (défaut: 0)
     * @param size taille de la page (défaut: 20)
     * @param sort tri (défaut: nom) - Valeurs possibles: nom, code, population, nombreVilles
     * @return Page<DepartementDTO> Page des départements
     */
    @GetMapping
    public Page<DepartementDTO> getAllDepartements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nom") String sort) {
        
        Page<Departement> departements = departementService.findAllWithSort(page, size, sort);
        return departements.map(departementMapper::toDTO);
    }

    /**
     * GET /departements/{id} - Récupère un département par son ID
     * @param id identifiant du département
     * @return DepartementDTO
     */
    @GetMapping("/{id}")
    public DepartementDTO getDepartementById(@PathVariable Long id) {
        Optional<Departement> departement = departementService.findById(id);
        return departement.map(departementMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", id));
    }

    /**
     * GET /departements/code/{code} - Récupère un département par son code
     * @param code code du département (ex: "75", "13", "2A")
     * @return DepartementDTO
     */
    @GetMapping("/code/{code}")
    public DepartementDTO getDepartementByCode(@PathVariable String code) {
        Optional<Departement> departement = departementService.findByCode(code);
        return departement.map(departementMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", code));
    }

    /**
     * POST /departements - Crée un nouveau département
     * @param departementDTO données du département
     * @return DepartementDTO
     */
    @PostMapping
    public DepartementDTO createDepartement(@Valid @RequestBody DepartementDTO departementDTO) {
        Departement departement = departementMapper.toEntity(departementDTO);
        Departement savedDepartement = departementService.save(departement);
        return departementMapper.toDTO(savedDepartement);
    }

    /**
     * PUT /departements/{id} - Met à jour un département
     * @param id identifiant du département
     * @param departementDTO nouvelles données
     * @return DepartementDTO
     */
    @PutMapping("/{id}")
    public DepartementDTO updateDepartement(@PathVariable Long id, @Valid @RequestBody DepartementDTO departementDTO) {
        // Vérifier que le département existe
        if (!departementService.findById(id).isPresent()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", id);
        }

        Departement departement = departementMapper.toEntity(departementDTO);
        departement.setId(id); // S'assurer que l'ID est correct
        Departement updatedDepartement = departementService.save(departement);
        return departementMapper.toDTO(updatedDepartement);
    }

    /**
     * DELETE /departements/{id} - Supprime un département
     * @param id identifiant du département
     */
    @DeleteMapping("/{id}")
    public void deleteDepartement(@PathVariable Long id) {
        // Vérifier que le département existe avant suppression
        if (!departementService.findById(id).isPresent()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", id);
        }
        
        departementService.deleteById(id);
    }

    // ==================== ROUTES DE RECHERCHE SPÉCIALISÉES ====================

    /**
     * GET /departements/search/nom?nom=Paris - Recherche par nom
     * @param nom nom du département
     * @return DepartementDTO
     */
    @GetMapping("/search/nom")
    public DepartementDTO findByNom(@RequestParam String nom) {
        Optional<Departement> departement = departementService.findByNom(nom);
        return departement.map(departementMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", nom));
    }

    /**
     * GET /departements/search?q=herault - Recherche par nom ou code (partielle, insensible à la casse)
     * @param q terme de recherche
     * @return List<DepartementDTO>
     */
    @GetMapping("/search")
    public List<DepartementDTO> searchDepartements(@RequestParam String q) {
        List<Departement> departements = departementService.searchDepartements(q);
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/avec-nom - Départements qui ont un nom (non null)
     * @return List<DepartementDTO>
     */
    @GetMapping("/avec-nom")
    public List<DepartementDTO> getDepartementsWithNom() {
        List<Departement> departements = departementService.findDepartementsWithNom();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/sans-nom - Départements sans nom (nom = null)
     * @return List<DepartementDTO>
     */
    @GetMapping("/sans-nom")
    public List<DepartementDTO> getDepartementsWithoutNom() {
        List<Departement> departements = departementService.findDepartementsWithoutNom();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/avec-villes - Départements qui ont des villes
     * @return List<DepartementDTO>
     */
    @GetMapping("/avec-villes")
    public List<DepartementDTO> getDepartementsWithVilles() {
        List<Departement> departements = departementService.findDepartementsWithVilles();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/min-villes?min=5 - Départements avec au moins N villes
     * @param min nombre minimum de villes
     * @return List<DepartementDTO>
     */
    @GetMapping("/min-villes")
    public List<DepartementDTO> getDepartementsWithMinVilles(@RequestParam int min) {
        List<Departement> departements = departementService.findDepartementsWithMinVilles(min);
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/min-population?min=1000000 - Départements avec population minimum
     * @param min population minimum totale
     * @return List<DepartementDTO>
     */
    @GetMapping("/min-population")
    public List<DepartementDTO> getDepartementsWithMinPopulation(@RequestParam Long min) {
        List<Departement> departements = departementService.findDepartementsWithMinPopulation(min);
        return departementMapper.toDTOList(departements);
    }

    // ==================== ROUTES PAR TYPE DE DÉPARTEMENT ====================

    /**
     * GET /departements/metropolitains - Départements métropolitains
     * @return List<DepartementDTO>
     */
    @GetMapping("/metropolitains")
    public List<DepartementDTO> getDepartementsMetropolitains() {
        List<Departement> departements = departementService.findDepartementsMetropolitains();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/outre-mer - Départements d'outre-mer
     * @return List<DepartementDTO>
     */
    @GetMapping("/outre-mer")
    public List<DepartementDTO> getDepartementsOutreMer() {
        List<Departement> departements = departementService.findDepartementsOutreMer();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/corse - Départements corses (2A, 2B)
     * @return List<DepartementDTO>
     */
    @GetMapping("/corse")
    public List<DepartementDTO> getDepartementsCorse() {
        List<Departement> departements = departementService.findDepartementsCorse();
        return departementMapper.toDTOList(departements);
    }

    /**
     * GET /departements/code-commence?prefix=97 - Départements dont le code commence par...
     * @param prefix préfixe du code
     * @return List<DepartementDTO>
     */
    @GetMapping("/code-commence")
    public List<DepartementDTO> findByCodeStartingWith(@RequestParam String prefix) {
        List<Departement> departements = departementService.findByCodeStartingWith(prefix);
        return departementMapper.toDTOList(departements);
    }

    // ==================== ROUTES DES VILLES PAR DÉPARTEMENT ====================

    /**
     * GET /departements/{id}/villes - Toutes les villes d'un département
     * @param id identifiant du département
     * @return List<VilleDTO>
     */
    @GetMapping("/{id}/villes")
    public List<VilleDTO> getVillesByDepartement(@PathVariable Long id) {
        Optional<Departement> departement = departementService.findById(id);
        if (departement.isEmpty()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", id);
        }

        List<Ville> villes = villeService.exportVillesByDepartement(departement.get().getCode());
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /departements/code/{code}/villes - Toutes les villes d'un département par code
     * @param code code du département
     * @return List<VilleDTO>
     */
    @GetMapping("/code/{code}/villes")
    public List<VilleDTO> getVillesByDepartementCode(@PathVariable String code) {
        List<Ville> villes = villeService.exportVillesByDepartement(code);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /departements/code/{code}/villes/top?n=10 - Top N villes d'un département
     * @param code code du département
     * @param n nombre de villes (défaut: 10)
     * @return List<VilleDTO>
     */
    @GetMapping("/code/{code}/villes/top")
    public List<VilleDTO> getTopVillesByDepartement(
            @PathVariable String code,
            @RequestParam(defaultValue = "10") int n) {
        List<Ville> villes = villeService.findTopNVillesByDepartement(code, n);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /departements/code/{code}/villes/population?min=10000 - Villes avec population min
     * @param code code du département
     * @param min population minimum (optionnel)
     * @return List<VilleDTO>
     */
    @GetMapping("/code/{code}/villes/population")
    public List<VilleDTO> getVillesByDepartementAndPopulation(
            @PathVariable String code,
            @RequestParam(required = false) Integer min) {
        List<Ville> villes;
        if (min != null) {
            villes = villeService.findByDepartementAndMinPopulation(code, min);
        } else {
            villes = villeService.exportVillesByDepartement(code);
        }
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /departements/code/{code}/villes/population-plage?min=10000&max=100000 - Villes dans plage population
     * @param code code du département
     * @param min population minimum
     * @param max population maximum
     * @return List<VilleDTO>
     */
    @GetMapping("/code/{code}/villes/population-plage")
    public List<VilleDTO> getVillesByDepartementAndPopulationRange(
            @PathVariable String code,
            @RequestParam Integer min,
            @RequestParam Integer max) {
        List<Ville> villes = villeService.findByDepartementAndPopulationRange(code, min, max);
        return villeMapper.toDTOList(villes);
    }

    // ==================== ROUTES STATISTIQUES ====================

    /**
     * GET /departements/count - Nombre total de départements
     * @return nombre de départements
     */
    @GetMapping("/count")
    public long getTotalCount() {
        return departementService.count();
    }


    /**
     * GET /departements/code/{code}/stats - Statistiques détaillées d'un département
     * @param code code du département
     * @return Map avec les statistiques
     */
    @GetMapping("/code/{code}/stats")
    public Map<String, Object> getStatsByCode(@PathVariable String code) {
        Optional<Departement> departement = departementService.findByCode(code);
        if (departement.isEmpty()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", code);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("code", departement.get().getCode());
        stats.put("nom", departement.get().getNom());
        stats.put("nombreVilles", departementService.countVillesById(departement.get().getId()));
        stats.put("populationTotale", departementService.getTotalPopulationById(departement.get().getId()));
        
        return stats;
    }

    /**
     * GET /departements/code/{code}/population-totale - Population totale d'un département
     * @param code code du département
     * @return Long
     */
    @GetMapping("/code/{code}/population-totale")
    public Long getPopulationTotalByCode(@PathVariable String code) {
        Optional<Departement> departement = departementService.findByCode(code);
        if (departement.isEmpty()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", code);
        }

        return departementService.getTotalPopulationById(departement.get().getId());
    }

    /**
     * GET /departements/code/{code}/nombre-villes - Nombre de villes d'un département
     * @param code code du département
     * @return Long
     */
    @GetMapping("/code/{code}/nombre-villes")
    public Long getNombreVillesByCode(@PathVariable String code) {
        Optional<Departement> departement = departementService.findByCode(code);
        if (departement.isEmpty()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", code);
        }

        return departementService.countVillesById(departement.get().getId());
    }

    // ==================== ROUTES DE GESTION AVANCÉE ====================

    /**
     * POST /departements/creation-rapide - Création rapide d'un département
     * @param code code du département
     * @param nom nom du département (optionnel)
     * @return DepartementDTO
     */
    @PostMapping("/creation-rapide")
    public DepartementDTO createDepartementRapide(
            @RequestParam String code,
            @RequestParam(required = false) String nom) {
        Departement departement = departementService.createDepartement(code, nom);
        return departementMapper.toDTO(departement);
    }

    /**
     * PUT /departements/code/{code}/nom - Met à jour le nom d'un département
     * @param code code du département
     * @param nom nouveau nom
     * @return DepartementDTO
     */
    @PutMapping("/code/{code}/nom")
    public DepartementDTO updateNomDepartement(@PathVariable String code, @RequestParam String nom) {
        Departement departement = departementService.updateNom(code, nom);
        return departementMapper.toDTO(departement);
    }

    /**
     * PUT /departements/update-noms-manquants - Met à jour tous les noms manquants
     * @return String
     */
    @PutMapping("/update-noms-manquants")
    public String updateNomsManquants() {
        departementService.updateNomsManquants();
        return "Noms des départements mis à jour avec succès";
    }

    /**
     * GET /departements/exists/code/{code} - Vérifie si un département existe
     * @param code code du département
     * @return boolean
     */
    @GetMapping("/exists/code/{code}")
    public boolean existsByCode(@PathVariable String code) {
        return departementService.existsByCode(code);
    }

    // ==================== CLASSES INTERNES POUR RÉPONSES ====================

    /**
     * Classe pour encapsuler les statistiques d'un département
     */
    public static class DepartementStatsResponse {
        private String code;
        private String nom;
        private Long nombreVilles;
        private Long populationTotale;

        public DepartementStatsResponse(String code, String nom, Long nombreVilles, Long populationTotale) {
            this.code = code;
            this.nom = nom;
            this.nombreVilles = nombreVilles;
            this.populationTotale = populationTotale;
        }

        // Getters
        public String getCode() { return code; }
        public String getNom() { return nom; }
        public Long getNombreVilles() { return nombreVilles; }
        public Long getPopulationTotale() { return populationTotale; }
    }

    // ==================== GESTION DES ERREURS ====================

    /**
     * Gestion des erreurs de validation
     */
}