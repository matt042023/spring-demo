package fr.diginamic.hello.controlers;

import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.repositories.VilleRepositoryHelper;
import fr.diginamic.hello.services.VilleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les villes - Version étendue avec toutes les routes du TP
 *
 * Nouvelles routes ajoutées pour l'étape 4 du TP :
 * - Recherche par nom commençant par...
 * - Recherche par population (min, max, plage)
 * - Recherche par département avec critères de population
 * - Top N villes d'un département
 * - Pagination améliorée
 *
 * @RestController = @Controller + @ResponseBody
 * Toutes les méthodes renvoient directement des données JSON
 */
@RestController
@RequestMapping("/villes") // URL de base pour toutes les méthodes
public class VilleController {

    @Autowired
    private VilleService villeService;

    @Autowired
    private VilleMapper villeMapper;

    // ==================== ROUTES CRUD DE BASE ====================

    /**
     * GET /villes - Récupère toutes les villes avec pagination
     * @param page numéro de la page (défaut: 0)
     * @param size taille de la page (défaut: 20)
     * @param sort tri (défaut: id)
     * @return Page<VilleDTO> Page des villes
     */
    @GetMapping
    public Page<VilleDTO> getAllVilles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Ville> villesPage = villeService.findAll(pageable);
        return villesPage.map(villeMapper::toDTO);
    }

    /**
     * GET /villes/{id} - Récupère une ville par son ID
     * @param id identifiant de la ville
     * @return VilleDTO
     */
    @GetMapping("/{id}")
    public VilleDTO getVilleById(@PathVariable Long id) {
        Optional<Ville> ville = villeService.findById(id);
        return ville.map(villeMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville", id));
    }

    /**
     * POST /villes - Crée une nouvelle ville
     * @param villeDTO données de la ville
     * @return VilleDTO
     */
    @PostMapping
    public VilleDTO createVille(@Valid @RequestBody VilleDTO villeDTO) {
    Ville ville = villeMapper.toEntity(villeDTO);
        Ville savedVille = villeService.save(ville);
        return villeMapper.toDTO(savedVille);
    }

    /**
     * PUT /villes/{id} - Met à jour une ville
     * @param id identifiant de la ville
     * @param villeDTO nouvelles données
     * @return VilleDTO
     */
    @PutMapping("/{id}")
    public VilleDTO updateVille(@PathVariable Long id, @Valid @RequestBody VilleDTO villeDTO) {
        // Vérifier que la ville existe
        if (!villeService.findById(id).isPresent()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Ville", id);
        }

        Ville ville = villeMapper.toEntity(villeDTO);
        ville.setId(id); // S'assurer que l'ID est correct
        Ville updatedVille = villeService.save(ville);
        return villeMapper.toDTO(updatedVille);
    }

    /**
     * DELETE /villes/{id} - Supprime une ville
     * @param id identifiant de la ville
     */
    @DeleteMapping("/{id}")
    public void deleteVille(@PathVariable Long id) {
        // Vérifier que la ville existe avant suppression
        if (!villeService.findById(id).isPresent()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Ville", id);
        }
        
        villeService.deleteById(id);
    }

    // ==================== NOUVELLES ROUTES DU TP - ÉTAPE 4 ====================

    /**
     * GET /villes/search/nom?nom=Paris - Recherche par nom exact
     * @param nom nom de la ville
     * @return VilleDTO
     */
    @GetMapping("/search/nom")
    public VilleDTO findByNom(@RequestParam String nom) {
        Optional<Ville> ville = villeService.findByNom(nom);
        return ville.map(villeMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville", nom));
    }

    /**
     * GET /villes/search/nom-contient?nom=Saint - Recherche par nom partiel
     * @param nom nom partiel de la ville
     * @return List<VilleDTO>
     */
    @GetMapping("/search/nom-contient")
    public List<VilleDTO> findByNomContaining(@RequestParam String nom) {
        List<Ville> villes = villeService.findByNomContaining(nom);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/search/nom-commence?prefix=Saint - Recherche des villes dont le nom commence par...
     * @param prefix préfixe du nom
     * @return List<VilleDTO> villes triées par nom
     */
    @GetMapping("/search/nom-commence")
    public List<VilleDTO> findByNomStartingWith(@RequestParam String prefix) {
        List<Ville> villes = villeService.findByNomStartingWith(prefix);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/search/population-min?min=100000 - Villes avec population > min
     * @param min population minimum
     * @return List<VilleDTO> villes triées par population décroissante
     */
    @GetMapping("/search/population-min")
    public List<VilleDTO> findByPopulationGreaterThan(@RequestParam Integer min) {
        List<Ville> villes = villeService.findByPopulationGreaterThan(min);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/search/population-plage?min=50000&max=200000 - Villes avec population entre min et max
     * @param min population minimum
     * @param max population maximum
     * @return List<VilleDTO> villes triées par population décroissante
     */
    @GetMapping("/search/population-plage")
    public List<VilleDTO> findByPopulationBetween(@RequestParam Integer min,
                                                  @RequestParam Integer max) {
        List<Ville> villes = villeService.findByPopulationBetween(min, max);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/departement/{code}?min=10000 - Villes d'un département avec population > min
     * @param code code du département (ex: "75", "13")
     * @param min population minimum (optionnel)
     * @return List<VilleDTO> villes du département triées par population décroissante
     */
    @GetMapping("/departement/{code}")
    public List<VilleDTO> findByDepartementAndMinPopulation(
            @PathVariable String code,
            @RequestParam(required = false) Integer min) {

        List<Ville> villes;
        if (min != null) {
            villes = villeService.findByDepartementAndMinPopulation(code, min);
        } else {
            // Si pas de minimum, récupérer toutes les villes du département
            villes = villeService.exportVillesByDepartement(code);
        }
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/departement/{code}/plage?min=10000&max=100000 - Villes d'un département avec population dans une plage
     * @param code code du département
     * @param min population minimum
     * @param max population maximum
     * @return List<VilleDTO> villes du département triées par population décroissante
     */
    @GetMapping("/departement/{code}/plage")
    public List<VilleDTO> findByDepartementAndPopulationRange(
            @PathVariable String code,
            @RequestParam Integer min,
            @RequestParam Integer max) {

        List<Ville> villes = villeService.findByDepartementAndPopulationRange(code, min, max);
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/departement/{code}/top?n=10 - Les N villes les plus peuplées d'un département
     * @param code code du département
     * @param n nombre de villes à récupérer (défaut: 10)
     * @return List<VilleDTO> les N villes les plus peuplées du département
     */
    @GetMapping("/departement/{code}/top")
    public List<VilleDTO> findTopNVillesByDepartement(
            @PathVariable String code,
            @RequestParam(defaultValue = "10") int n) {

        List<Ville> villes = villeService.findTopNVillesByDepartement(code, n);
        return villeMapper.toDTOList(villes);
    }

    // ==================== ROUTES STATISTIQUES ====================

    /**
     * GET /villes/count - Nombre total de villes
     * @return nombre de villes
     */
    @GetMapping("/count")
    public long getTotalCount() {
        return villeService.count();
    }

    /**
     * GET /villes/departement/{code}/stats - Statistiques d'un département
     * @param code code du département
     * @return VilleRepositoryHelper.DepartementStats
     */
    @GetMapping("/departement/{code}/stats")
    public VilleRepositoryHelper.DepartementStats getDepartementStats(@PathVariable String code) {
        VilleRepositoryHelper.DepartementStats stats = villeService.getDepartementStats(code);
        if (stats == null) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", code);
        }
        return stats;
    }

    /**
     * GET /villes/departement/{code}/plus-peuplee - Ville la plus peuplée d'un département
     * @param code code du département
     * @return VilleDTO
     */
    @GetMapping("/departement/{code}/plus-peuplee")
    public VilleDTO getMostPopulatedVilleInDepartement(@PathVariable String code) {
        Optional<Ville> ville = villeService.findMostPopulatedVilleInDepartement(code);
        return ville.map(villeMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville la plus peuplée du département", code));
    }

    // ==================== ROUTES DE GESTION AVANCÉE ====================

    /**
     * PUT /villes/{id}/population?nouveauNb=150000 - Met à jour uniquement la population
     * @param id identifiant de la ville
     * @param nouveauNb nouveau nombre d'habitants
     * @return VilleDTO
     */
    @PutMapping("/{id}/population")
    public VilleDTO updatePopulation(@PathVariable Long id,
                                     @RequestParam Integer nouveauNb) {
        Ville ville = villeService.updatePopulation(id, nouveauNb);
        return villeMapper.toDTO(ville);
    }

    /**
     * POST /villes/creation-rapide - Création rapide d'une ville
     * @param nom nom de la ville
     * @param nbHabitants nombre d'habitants
     * @param codeDepartement code du département
     * @return VilleDTO
     */
    @PostMapping("/creation-rapide")
    public VilleDTO createVilleRapide(
            @RequestParam String nom,
            @RequestParam Integer nbHabitants,
            @RequestParam String codeDepartement) {
        Ville ville = villeService.createVille(nom, nbHabitants, codeDepartement);
        return villeMapper.toDTO(ville);
    }

    /**
     * POST /villes/import - Import en lot de villes
     * @param villesDTO liste de villes à importer
     * @return List<VilleDTO>
     */
    @PostMapping("/import")
    public List<VilleDTO> importVilles(@RequestBody List<VilleDTO> villesDTO) {
        List<Ville> villes = villeMapper.toEntityList(villesDTO);
        List<Ville> villesImportees = villeService.importVilles(villes);
        return villeMapper.toDTOList(villesImportees);
    }

    /**
     * GET /villes/export/departement/{code} - Export de toutes les villes d'un département
     * @param code code du département
     * @return List<VilleDTO>
     */
    @GetMapping("/export/departement/{code}")
    public List<VilleDTO> exportVillesByDepartement(@PathVariable String code) {
        List<Ville> villes = villeService.exportVillesByDepartement(code);
        return villeMapper.toDTOList(villes);
    }

    // ==================== ROUTES DE RECHERCHE AVANCÉE ====================

    /**
     * GET /villes/search/avancee?nom=&minPop=&maxPop=&dept= - Recherche multi-critères
     * @param nom nom (optionnel)
     * @param minPop population minimum (optionnel)
     * @param maxPop population maximum (optionnel)
     * @param dept code département (optionnel)
     * @return List<VilleDTO>
     */
    @GetMapping("/search/avancee")
    public List<VilleDTO> rechercheAvancee(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Integer minPop,
            @RequestParam(required = false) Integer maxPop,
            @RequestParam(required = false) String dept) {

        List<Ville> villes;

        // Logique de recherche selon les critères fournis
        if (dept != null && minPop != null && maxPop != null) {
            villes = villeService.findByDepartementAndPopulationRange(dept, minPop, maxPop);
        } else if (dept != null && minPop != null) {
            villes = villeService.findByDepartementAndMinPopulation(dept, minPop);
        } else if (minPop != null && maxPop != null) {
            villes = villeService.findByPopulationBetween(minPop, maxPop);
        } else if (minPop != null) {
            villes = villeService.findByPopulationGreaterThan(minPop);
        } else if (nom != null) {
            villes = villeService.findByNomContaining(nom);
        } else {
            villes = villeService.findAll();
        }

        return villeMapper.toDTOList(villes);
    }

    // ==================== GESTION DES ERREURS ====================

}