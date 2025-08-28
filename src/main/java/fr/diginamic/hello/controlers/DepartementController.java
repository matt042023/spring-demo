package fr.diginamic.hello.controlers;

import fr.diginamic.hello.dto.DepartementDTO;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.DepartementMapper;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.services.DepartementService;
import fr.diginamic.hello.services.VilleService;
import fr.diginamic.hello.services.ExportService;
import fr.diginamic.hello.services.PdfExportService;
import fr.diginamic.hello.swagger.SwaggerDepartementController;
import jakarta.validation.Valid;
import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.DocumentException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * @author Matthieu - Développeur Full Stack
 * @version 2.0.0 - Avec documentation Swagger intégrée
 * @since 2025-08-26
 */
@RestController
@RequestMapping("/departements") // URL de base pour toutes les méthodes
public class DepartementController implements SwaggerDepartementController {

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
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    public Page<DepartementDTO> getAllDepartements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nom") String sort) {

        Page<Departement> departements = departementService.findAllWithSort(page, size, sort);
        return departements.map(departementMapper::toDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}")
    public DepartementDTO getDepartementById(@PathVariable Long id) {
        Optional<Departement> departement = departementService.findById(id);
        return departement.map(departementMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/code/{code}")
    public DepartementDTO getDepartementByCode(@PathVariable String code) {
        Optional<Departement> departement = departementService.findByCode(code);
        return departement.map(departementMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", code));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public DepartementDTO createDepartement(@Valid @RequestBody DepartementDTO departementDTO) {
        Departement departement = departementMapper.toEntity(departementDTO);
        Departement savedDepartement = departementService.save(departement);
        return departementMapper.toDTO(savedDepartement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search/nom")
    public DepartementDTO findByNom(@RequestParam String nom) {
        Optional<Departement> departement = departementService.findByNom(nom);
        return departement.map(departementMapper::toDTO)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", nom));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/search")
    public List<DepartementDTO> searchDepartements(@RequestParam String q) {
        List<Departement> departements = departementService.searchDepartements(q);
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/avec-nom")
    public List<DepartementDTO> getDepartementsWithNom() {
        List<Departement> departements = departementService.findDepartementsWithNom();
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/sans-nom")
    public List<DepartementDTO> getDepartementsWithoutNom() {
        List<Departement> departements = departementService.findDepartementsWithoutNom();
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/avec-villes")
    public List<DepartementDTO> getDepartementsWithVilles() {
        List<Departement> departements = departementService.findDepartementsWithVilles();
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/min-villes")
    public List<DepartementDTO> getDepartementsWithMinVilles(@RequestParam int min) {
        List<Departement> departements = departementService.findDepartementsWithMinVilles(min);
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/min-population")
    public List<DepartementDTO> getDepartementsWithMinPopulation(@RequestParam Long min) {
        List<Departement> departements = departementService.findDepartementsWithMinPopulation(min);
        return departementMapper.toDTOList(departements);
    }

    // ==================== ROUTES PAR TYPE DE DÉPARTEMENT ====================

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/metropolitains")
    public List<DepartementDTO> getDepartementsMetropolitains() {
        List<Departement> departements = departementService.findDepartementsMetropolitains();
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/outre-mer")
    public List<DepartementDTO> getDepartementsOutreMer() {
        List<Departement> departements = departementService.findDepartementsOutreMer();
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/corse")
    public List<DepartementDTO> getDepartementsCorse() {
        List<Departement> departements = departementService.findDepartementsCorse();
        return departementMapper.toDTOList(departements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/code-commence")
    public List<DepartementDTO> findByCodeStartingWith(@RequestParam String prefix) {
        List<Departement> departements = departementService.findByCodeStartingWith(prefix);
        return departementMapper.toDTOList(departements);
    }

    // ==================== ROUTES DES VILLES PAR DÉPARTEMENT ====================

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/code/{code}/villes")
    public List<VilleDTO> getVillesByDepartementCode(@PathVariable String code) {
        List<Ville> villes = villeService.exportVillesByDepartement(code);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/code/{code}/villes/top")
    public List<VilleDTO> getTopVillesByDepartement(
            @PathVariable String code,
            @RequestParam(defaultValue = "10") int n) {
        List<Ville> villes = villeService.findTopNVillesByDepartement(code, n);
        return villeMapper.toDTOList(villes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/count")
    public long getTotalCount() {
        return departementService.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/code/{code}/population-totale")
    public Long getPopulationTotalByCode(@PathVariable String code) {
        Optional<Departement> departement = departementService.findByCode(code);
        if (departement.isEmpty()) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", code);
        }

        return departementService.getTotalPopulationById(departement.get().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    @PostMapping("/creation-rapide")
    public DepartementDTO createDepartementRapide(
            @RequestParam String code,
            @RequestParam(required = false) String nom) {
        Departement departement = departementService.createDepartement(code, nom);
        return departementMapper.toDTO(departement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping("/code/{code}/nom")
    public DepartementDTO updateNomDepartement(@PathVariable String code, @RequestParam String nom) {
        Departement departement = departementService.updateNom(code, nom);
        return departementMapper.toDTO(departement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping("/update-noms-manquants")
    public String updateNomsManquants() {
        departementService.updateNomsManquants();
        return "Noms des départements mis à jour avec succès";
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    @Autowired
    private PdfExportService pdfExportService;

    /**
     * Exporte les détails d'un département au format PDF
     *
     * @param codeDepartement code du département à exporter
     * @return fichier PDF en téléchargement
     */
    @GetMapping("/{codeDepartement}/export/pdf")
    public ResponseEntity<byte[]> exportDepartementToPdf(@PathVariable String codeDepartement) {

        try {
            ByteArrayOutputStream pdfData = pdfExportService.exportDepartementToPdf(codeDepartement);

            // Génération du nom de fichier avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("departement_%s_%s.pdf", codeDepartement, timestamp);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfData.size());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData.toByteArray());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (DocumentException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Alternative pour prévisualiser le PDF dans le navigateur
     */
    @GetMapping("/{codeDepartement}/preview/pdf")
    public ResponseEntity<byte[]> previewDepartementPdf(@PathVariable String codeDepartement) {

        try {
            ByteArrayOutputStream pdfData = pdfExportService.exportDepartementToPdf(codeDepartement);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Pas de Content-Disposition pour affichage inline

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData.toByteArray());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (DocumentException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}