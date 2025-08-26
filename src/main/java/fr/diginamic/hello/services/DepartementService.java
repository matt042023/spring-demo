package fr.diginamic.hello.services;

import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant la logique métier des départements
 *
 * Version mise à jour utilisant les Repositories Spring Data JPA
 * au lieu des DAOs traditionnels.
 *
 * @author Votre nom
 * @version 2.0 - Migration vers Spring Data JPA Repositories
 * @since 1.0
 */
@Service
@Transactional
public class DepartementService {

    // ==================== INJECTION DES DÉPENDANCES ====================

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private VilleRepository villeRepository;

    // ==================== MÉTHODES CRUD DE BASE ====================

    /**
     * Récupère tous les départements
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findAll() {
        return departementRepository.findAll();
    }

    /**
     * Récupère tous les départements avec pagination
     * @param pageable paramètres de pagination
     * @return Page<Departement>
     */
    @Transactional(readOnly = true)
    public Page<Departement> findAll(Pageable pageable) {
        return departementRepository.findAll(pageable);
    }

    /**
     * Récupère tous les départements avec pagination et tri personnalisé
     * @param page numéro de la page
     * @param size taille de la page
     * @param sort type de tri (nom, code, population, nombreVilles)
     * @return Page<Departement>
     */
    @Transactional(readOnly = true)
    public Page<Departement> findAllWithSort(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        
        switch (sort.toLowerCase()) {
            case "nom":
                return departementRepository.findAllOrderByNom(pageable);
            case "code":
                return departementRepository.findAllOrderByCode(pageable);
            case "population":
                return departementRepository.findAllOrderByPopulation(pageable);
            case "nombrevilles":
                return departementRepository.findAllOrderByNombreVilles(pageable);
            default:
                // Par défaut, tri par nom
                return departementRepository.findAllOrderByNom(pageable);
        }
    }

    /**
     * Recherche un département par son ID
     * @param id identifiant du département
     * @return Optional<Departement>
     */
    @Transactional(readOnly = true)
    public Optional<Departement> findById(Long id) {
        return departementRepository.findById(id);
    }

    /**
     * Recherche un département par son code
     * @param code code du département
     * @return Optional<Departement>
     */
    @Transactional(readOnly = true)
    public Optional<Departement> findByCode(String code) {
        return departementRepository.findByCode(code);
    }

    /**
     * Recherche un département par son nom
     * @param nom nom du département
     * @return Optional<Departement>
     */
    @Transactional(readOnly = true)
    public Optional<Departement> findByNom(String nom) {
        return departementRepository.findByNom(nom);
    }

    /**
     * Recherche des départements par nom ou code contenant la chaîne de recherche
     * @param searchTerm terme de recherche
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> searchDepartements(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        return departementRepository.findByNomContainingOrCodeContainingIgnoreCase(searchTerm.trim());
    }

    /**
     * Sauvegarde ou met à jour un département
     * @param departement département à sauvegarder
     * @return Departement sauvegardé
     */
    public Departement save(Departement departement) {
        validateDepartement(departement);
        return departementRepository.save(departement);
    }

    /**
     * Supprime un département par son ID
     * @param id identifiant du département
     */
    public void deleteById(Long id) {
        // Vérification que le département existe
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", id));

        // Vérification qu'il n'y a pas de villes associées
        Long nombreVilles = villeRepository.countByDepartement(departement);
        if (nombreVilles > 0) {
            throw ExceptionFonctionnelle.suppressionImpossible("le département", 
                    nombreVilles + " ville(s) y sont encore rattachées");
        }

        departementRepository.deleteById(id);
    }

    // ==================== MÉTHODES DE RECHERCHE SPÉCIALISÉES ====================

    /**
     * Récupère les départements qui ont des villes
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsWithVilles() {
        return departementRepository.findDepartementsWithVilles();
    }

    /**
     * Récupère les départements qui ont un nom (non null)
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsWithNom() {
        return departementRepository.findByNomIsNotNull();
    }

    /**
     * Récupère les départements qui n'ont pas de nom (null)
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsWithoutNom() {
        return departementRepository.findByNomIsNull();
    }

    /**
     * Récupère les départements avec un nombre minimum de villes
     * @param minNombreVilles nombre minimum de villes
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsWithMinVilles(int minNombreVilles) {
        return departementRepository.findDepartementsWithMinVilles(minNombreVilles);
    }

    /**
     * Récupère les départements avec une population minimum
     * @param minPopulation population minimum
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsWithMinPopulation(Long minPopulation) {
        return departementRepository.findDepartementsWithMinPopulation(minPopulation);
    }

    // ==================== MÉTHODES PAR TYPE DE DÉPARTEMENT ====================

    /**
     * Récupère les départements métropolitains
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsMetropolitains() {
        return departementRepository.findDepartementsMetropolitains();
    }

    /**
     * Récupère les départements d'outre-mer
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsOutreMer() {
        return departementRepository.findDepartementsOutreMer();
    }

    /**
     * Récupère les départements corses
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsCorse() {
        return departementRepository.findDepartementsCorse();
    }

    /**
     * Récupère les départements dont le code commence par un préfixe
     * @param prefix préfixe du code
     * @return List<Departement>
     */
    @Transactional(readOnly = true)
    public List<Departement> findByCodeStartingWith(String prefix) {
        return departementRepository.findByCodeStartingWith(prefix);
    }

    // ==================== MÉTHODES STATISTIQUES ====================

    /**
     * Compte le nombre total de départements
     * @return nombre de départements
     */
    @Transactional(readOnly = true)
    public long count() {
        return departementRepository.count();
    }

    /**
     * Compte le nombre de villes d'un département
     * @param id identifiant du département
     * @return nombre de villes
     */
    @Transactional(readOnly = true)
    public Long countVillesById(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", id));
        return villeRepository.countByDepartement(departement);
    }

    /**
     * Calcule la population totale d'un département
     * @param id identifiant du département
     * @return population totale
     */
    @Transactional(readOnly = true)
    public Long getTotalPopulationById(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", id));
        return villeRepository.sumPopulationByDepartement(departement);
    }

    /**
     * Vérifie si un département existe par son code
     * @param code code du département
     * @return true si le département existe
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return departementRepository.existsByCode(code);
    }

    // ==================== MÉTHODES DE GESTION DES NOMS ====================

    /**
     * Met à jour le nom d'un département
     * @param code code du département
     * @param nom nouveau nom
     * @return Departement mis à jour
     */
    public Departement updateNom(String code, String nom) {
        Departement departement = departementRepository.findByCode(code)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", code));

        departement.setNom(nom);
        return departementRepository.save(departement);
    }

    /**
     * Met à jour tous les noms de départements vides
     * Méthode utile pour remplir les noms manquants du fichier SQL
     */
    public void updateNomsManquants() {
        List<Departement> departementsWithoutNom = departementRepository.findByNomIsNull();

        for (Departement dept : departementsWithoutNom) {
            String nom = getNomDepartementParCode(dept.getCode());
            if (nom != null) {
                dept.setNom(nom);
                departementRepository.save(dept);
            }
        }
    }

    // ==================== MÉTHODES DE VALIDATION ====================

    /**
     * Valide les données d'un département
     * @param departement département à valider
     */
    private void validateDepartement(Departement departement) {
        if (departement == null) {
            throw ExceptionFonctionnelle.donneesInvalides("Le département ne peut pas être null");
        }

        if (departement.getCode() == null || departement.getCode().trim().isEmpty()) {
            throw ExceptionFonctionnelle.donneesInvalides("Le code du département est obligatoire");
        }

        // Validation du format du code
        String code = departement.getCode().toUpperCase();
        if (!isValidCodeDepartement(code)) {
            throw new ExceptionFonctionnelle(
                "CONSTRAINT_VIOLATION",
                "Le code département '%s' n'est pas valide. Utilisez un code français valide: 01-95 (sauf 20), 2A, 2B, ou 971-976",
                code
            );
        }

        // Vérification de l'unicité du code (sauf pour les mises à jour)
        if (departement.getId() == null) { // Nouveau département
            if (departementRepository.existsByCode(code)) {
                throw ExceptionFonctionnelle.ressourceDejaExistante("Département", "code", code);
            }
        }
    }

    /**
     * Valide le format d'un code département
     * @param code code à valider
     * @return true si le format est valide
     */
    private boolean isValidCodeDepartement(String code) {
        // Codes métropolitains : 01-19, 21-95 (sauf 20)
        // Codes Corse : 2A, 2B
        // Codes DOM-TOM : 971, 972, 973, 974, 975, 976, 977, 978
        return code.matches("^(0[1-9]|[1-8][0-9]|9[0-5]|2[AB]|97[1-8])$");
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère le nom complet d'un département par son code
     * @param code code du département
     * @return nom du département ou null
     */
    private String getNomDepartementParCode(String code) {
        // Map des codes vers noms (simplifié pour l'exemple)
        return switch (code) {
            case "01" -> "Ain";
            case "02" -> "Aisne";
            case "03" -> "Allier";
            case "13" -> "Bouches-du-Rhône";
            case "34" -> "Hérault";
            case "75" -> "Paris";
            case "2A" -> "Corse-du-Sud";
            case "2B" -> "Haute-Corse";
            case "971" -> "Guadeloupe";
            case "972" -> "Martinique";
            case "973" -> "Guyane";
            case "974" -> "La Réunion";
            // Ajouter tous les autres codes selon vos besoins
            default -> null;
        };
    }

    /**
     * Crée un nouveau département
     * @param code code du département
     * @param nom nom du département (optionnel)
     * @return Departement créé
     */
    public Departement createDepartement(String code, String nom) {
        Departement departement = new Departement(code, nom);
        return save(departement);
    }
}