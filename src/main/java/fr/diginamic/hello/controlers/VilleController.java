package fr.diginamic.hello.controlers;

import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.repositories.VilleRepositoryHelper;
import fr.diginamic.hello.services.VilleService;
import fr.diginamic.hello.swagger.SwaggerVilleController;
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
 *
 * @author Matthieu - Développeur Full Stack
 * @version 2.0.0 - Avec documentation Swagger intégrée
 * @since 2025-08-26
 */
@RestController
@RequestMapping("/villes") // URL de base pour toutes les méthodes
public class VilleController implements SwaggerVilleController {

    @Autowired
    private VilleService villeService;

    @Autowired
    private VilleMapper villeMapper;

    // ==================== ROUTES CRUD DE BASE ====================

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}")
    public VilleDTO getVilleById(@PathVariable Long id) {
        Optional<Ville> ville = villeService.findById(id);
        return ville.map(villeMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville", id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public VilleDTO createVille(@Valid @RequestBody VilleDTO villeDTO) {
        Ville ville = villeMapper.toEntity(villeDTO);
        Ville savedVille = villeService.save(ville);
        return villeMapper.toDTO(savedVille);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search/nom")
    public VilleDTO findByNom(@RequestParam String nom) {
        Optional<Ville> ville = villeService.findByNom(nom);
        return ville.map(villeMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville", nom));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search/nom-contient")
    public List<VilleDTO> findByNomContaining(@RequestParam String nom) {
        List<Ville> villes = villeService.findByNomContaining(nom);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search/nom-commence")
    public List<VilleDTO> findByNomStartingWith(@RequestParam String prefix) {
        List<Ville> villes = villeService.findByNomStartingWith(prefix);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search/population-min")
    public List<VilleDTO> findByPopulationGreaterThan(@RequestParam Integer min) {
        List<Ville> villes = villeService.findByPopulationGreaterThan(min);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search/population-plage")
    public List<VilleDTO> findByPopulationBetween(@RequestParam Integer min,
                                                  @RequestParam Integer max) {
        List<Ville> villes = villeService.findByPopulationBetween(min, max);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/departement/{code}/plage")
    public List<VilleDTO> findByDepartementAndPopulationRange(
            @PathVariable String code,
            @RequestParam Integer min,
            @RequestParam Integer max) {

        List<Ville> villes = villeService.findByDepartementAndPopulationRange(code, min, max);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/departement/{code}/top")
    public List<VilleDTO> findTopNVillesByDepartement(
            @PathVariable String code,
            @RequestParam(defaultValue = "10") int n) {

        List<Ville> villes = villeService.findTopNVillesByDepartement(code, n);
        return villeMapper.toDTOList(villes);
    }

    // ==================== ROUTES STATISTIQUES ====================

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/count")
    public long getTotalCount() {
        return villeService.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/departement/{code}/stats")
    public VilleRepositoryHelper.DepartementStats getDepartementStats(@PathVariable String code) {
        VilleRepositoryHelper.DepartementStats stats = villeService.getDepartementStats(code);
        if (stats == null) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", code);
        }
        return stats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/departement/{code}/plus-peuplee")
    public VilleDTO getMostPopulatedVilleInDepartement(@PathVariable String code) {
        Optional<Ville> ville = villeService.findMostPopulatedVilleInDepartement(code);
        return ville.map(villeMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville la plus peuplée du département", code));
    }

    // ==================== ROUTES DE GESTION AVANCÉE ====================

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping("/{id}/population")
    public VilleDTO updatePopulation(@PathVariable Long id,
                                     @RequestParam Integer nouveauNb) {
        Ville ville = villeService.updatePopulation(id, nouveauNb);
        return villeMapper.toDTO(ville);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping("/creation-rapide")
    public VilleDTO createVilleRapide(
            @RequestParam String nom,
            @RequestParam Integer nbHabitants,
            @RequestParam String codeDepartement) {
        Ville ville = villeService.createVille(nom, nbHabitants, codeDepartement);
        return villeMapper.toDTO(ville);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping("/import")
    public List<VilleDTO> importVilles(@RequestBody List<VilleDTO> villesDTO) {
        List<Ville> villes = villeMapper.toEntityList(villesDTO);
        List<Ville> villesImportees = villeService.importVilles(villes);
        return villeMapper.toDTOList(villesImportees);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/export/departement/{code}")
    public List<VilleDTO> exportVillesByDepartement(@PathVariable String code) {
        List<Ville> villes = villeService.exportVillesByDepartement(code);
        return villeMapper.toDTOList(villes);
    }

    // ==================== ROUTES DE RECHERCHE AVANCÉE ====================

    /**
     * {@inheritDoc}
     */
    @Override
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