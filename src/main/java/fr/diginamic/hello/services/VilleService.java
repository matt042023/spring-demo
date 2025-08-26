package fr.diginamic.hello.services;

import fr.diginamic.hello.exceptions.ExceptionFonctionnelle;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.repositories.DepartementRepository;
import fr.diginamic.hello.repositories.VilleRepository;
import fr.diginamic.hello.repositories.VilleRepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant la logique métier des villes
 *
 * Version mise à jour utilisant les Repositories Spring Data JPA
 * au lieu des DAOs traditionnels.
 *
 * Cette classe implémente la couche service de l'architecture Spring MVC :
 * - Sépare la logique métier du contrôleur REST
 * - Gère les transactions avec @Transactional
 * - Effectue les validations métier avant persistance
 * - Utilise les repositories pour l'accès aux données
 *
 * @author Votre nom
 * @version 2.0 - Migration vers Spring Data JPA Repositories
 * @since 1.0
 */
@Service
@Transactional
public class VilleService {

    // ==================== INJECTION DES DÉPENDANCES ====================

    @Autowired
    private VilleRepository villeRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private VilleRepositoryHelper repositoryHelper;

    // ==================== MÉTHODES CRUD DE BASE ====================

    /**
     * Récupère toutes les villes
     * @return List<Ville> toutes les villes
     */
    @Transactional(readOnly = true)
    public List<Ville> findAll() {
        return villeRepository.findAll();
    }

    /**
     * Récupère toutes les villes avec pagination
     * @param page numéro de page (commence à 0)
     * @param size taille de la page
     * @return Page<Ville>
     */
    @Transactional(readOnly = true)
    public Page<Ville> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return villeRepository.findAll(pageable);
    }

    /**
     * Récupère toutes les villes avec pagination et tri
     * @param pageable paramètres de pagination et tri
     * @return Page<Ville>
     */
    @Transactional(readOnly = true)
    public Page<Ville> findAll(Pageable pageable) {
        return villeRepository.findAll(pageable);
    }

    /**
     * Recherche une ville par son ID
     * @param id identifiant de la ville
     * @return Optional<Ville>
     */
    @Transactional(readOnly = true)
    public Optional<Ville> findById(Long id) {
        return villeRepository.findById(id);
    }

    /**
     * Sauvegarde ou met à jour une ville
     * @param ville ville à sauvegarder
     * @return Ville sauvegardée
     */
    public Ville save(Ville ville) {
        // Validations métier
        validateVille(ville);
        return villeRepository.save(ville);
    }

    /**
     * Supprime une ville par son ID
     * @param id identifiant de la ville
     */
    public void deleteById(Long id) {
        if (!villeRepository.existsById(id)) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Ville", id);
        }
        villeRepository.deleteById(id);
    }

    // ==================== MÉTHODES DE RECHERCHE SPÉCIALISÉES ====================

    /**
     * Recherche une ville par nom exact
     * @param nom nom de la ville
     * @return Optional<Ville>
     */
    @Transactional(readOnly = true)
    public Optional<Ville> findByNom(String nom) {
        return villeRepository.findByNom(nom);
    }

    /**
     * Recherche des villes par nom partiel (insensible à la casse)
     * @param nom nom partiel de la ville
     * @return List<Ville>
     */
    @Transactional(readOnly = true)
    public List<Ville> findByNomContaining(String nom) {
        return villeRepository.findByNomContainingIgnoreCase(nom);
    }

    /**
     * Recherche des villes dont le nom commence par un préfixe
     * @param prefix préfixe du nom
     * @return List<Ville> triée par nom
     */
    @Transactional(readOnly = true)
    public List<Ville> findByNomStartingWith(String prefix) {
        return villeRepository.findByNomStartingWithIgnoreCaseOrderByNom(prefix);
    }

    // ==================== MÉTHODES DE RECHERCHE PAR POPULATION ====================

    /**
     * Recherche des villes avec une population supérieure à un minimum
     * @param minPopulation population minimum
     * @return List<Ville> triée par population décroissante
     */
    @Transactional(readOnly = true)
    public List<Ville> findByPopulationGreaterThan(Integer minPopulation) {
        return villeRepository.findByNbHabitantsGreaterThanOrderByNbHabitantsDesc(minPopulation);
    }

    /**
     * Recherche des villes avec une population dans une plage donnée
     * @param minPopulation population minimum (incluse)
     * @param maxPopulation population maximum (incluse)
     * @return List<Ville> triée par population décroissante
     */
    @Transactional(readOnly = true)
    public List<Ville> findByPopulationBetween(Integer minPopulation, Integer maxPopulation) {
        return villeRepository.findByNbHabitantsBetweenOrderByNbHabitantsDesc(minPopulation, maxPopulation);
    }

    // ==================== MÉTHODES DE RECHERCHE PAR DÉPARTEMENT ====================

    /**
     * Recherche des villes d'un département avec population minimum
     * @param codeDepartement code du département
     * @param minPopulation population minimum
     * @return List<Ville>
     */
    @Transactional(readOnly = true)
    public List<Ville> findByDepartementAndMinPopulation(String codeDepartement, Integer minPopulation) {
        return repositoryHelper.findVillesByCodeDepartementAndMinPopulation(codeDepartement, minPopulation);
    }

    /**
     * Recherche des villes d'un département avec population dans une plage
     * @param codeDepartement code du département
     * @param minPopulation population minimum
     * @param maxPopulation population maximum
     * @return List<Ville>
     */
    @Transactional(readOnly = true)
    public List<Ville> findByDepartementAndPopulationRange(String codeDepartement,
                                                           Integer minPopulation,
                                                           Integer maxPopulation) {
        return repositoryHelper.findVillesByCodeDepartementAndPopulationRange(
                codeDepartement, minPopulation, maxPopulation);
    }

    /**
     * Recherche des N villes les plus peuplées d'un département
     * @param codeDepartement code du département
     * @param n nombre de villes à récupérer
     * @return List<Ville>
     */
    @Transactional(readOnly = true)
    public List<Ville> findTopNVillesByDepartement(String codeDepartement, int n) {
        return repositoryHelper.findTopNVillesByCodeDepartement(codeDepartement, n);
    }

    // ==================== MÉTHODES STATISTIQUES ====================

    /**
     * Compte le nombre total de villes
     * @return nombre total de villes
     */
    @Transactional(readOnly = true)
    public long count() {
        return villeRepository.count();
    }

    /**
     * Récupère les statistiques complètes d'un département
     * @param codeDepartement code du département
     * @return DepartementStats statistiques du département
     */
    @Transactional(readOnly = true)
    public VilleRepositoryHelper.DepartementStats getDepartementStats(String codeDepartement) {
        return repositoryHelper.getDepartementStats(codeDepartement);
    }

    /**
     * Recherche la ville la plus peuplée d'un département
     * @param codeDepartement code du département
     * @return Optional<Ville>
     */
    @Transactional(readOnly = true)
    public Optional<Ville> findMostPopulatedVilleInDepartement(String codeDepartement) {
        Optional<Departement> departement = departementRepository.findByCode(codeDepartement);
        if (departement.isPresent()) {
            return villeRepository.findMostPopulatedVilleInDepartement(departement.get());
        }
        return Optional.empty();
    }

    // ==================== MÉTHODES DE VALIDATION ====================

    /**
     * Valide les données d'une ville avant sauvegarde
     * @param ville ville à valider
     * @throws RuntimeException si les données ne sont pas valides
     */
    private void validateVille(Ville ville) {
        if (ville == null) {
            throw ExceptionFonctionnelle.donneesInvalides("La ville ne peut pas être null");
        }

        if (ville.getNom() == null || ville.getNom().trim().isEmpty()) {
            throw ExceptionFonctionnelle.donneesInvalides("Le nom de la ville est obligatoire");
        }

        if (ville.getNbHabitants() == null || ville.getNbHabitants() <= 0) {
            throw ExceptionFonctionnelle.contrainteViolee("nombre_habitants_positif", ville.getNbHabitants());
        }

        if (ville.getDepartement() == null) {
            throw ExceptionFonctionnelle.donneesInvalides("Le département est obligatoire");
        }

        // Vérification que le département existe
        if (!departementRepository.existsById(ville.getDepartement().getId())) {
            throw ExceptionFonctionnelle.ressourceNonTrouvee("Département", ville.getDepartement().getId());
        }

        // Vérification de l'unicité du nom dans le département (optionnel)
        if (ville.getId() == null) { // Nouveau ville
            Optional<Ville> existingVille = villeRepository.findByNom(ville.getNom());
            if (existingVille.isPresent()) {
                throw ExceptionFonctionnelle.ressourceDejaExistante("Ville", "nom", ville.getNom());
            }
        }
    }

    // ==================== MÉTHODES DE CRÉATION RAPIDE ====================

    /**
     * Crée une nouvelle ville avec validation
     * @param nom nom de la ville
     * @param nbHabitants nombre d'habitants
     * @param codeDepartement code du département
     * @return Ville créée
     */
    public Ville createVille(String nom, Integer nbHabitants, String codeDepartement) {
        // Recherche du département
        Departement departement = departementRepository.findByCode(codeDepartement)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", codeDepartement));

        // Création de la ville
        Ville ville = new Ville(nom, nbHabitants, departement);

        return save(ville);
    }

    /**
     * Met à jour le nombre d'habitants d'une ville
     * @param id identifiant de la ville
     * @param nouveauNbHabitants nouveau nombre d'habitants
     * @return Ville mise à jour
     */
    public Ville updatePopulation(Long id, Integer nouveauNbHabitants) {
        Ville ville = villeRepository.findById(id)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Ville", id));

        if (nouveauNbHabitants <= 0) {
            throw ExceptionFonctionnelle.contrainteViolee("nombre_habitants_positif", nouveauNbHabitants);
        }

        ville.setNbHabitants(nouveauNbHabitants);
        return villeRepository.save(ville);
    }

    // ==================== MÉTHODES D'IMPORT/EXPORT ====================

    /**
     * Importe une liste de villes en lot
     * @param villes liste des villes à importer
     * @return List<Ville> villes importées
     */
    public List<Ville> importVilles(List<Ville> villes) {
        // Validation de chaque ville
        for (Ville ville : villes) {
            validateVille(ville);
        }

        // Sauvegarde en lot
        return villeRepository.saveAll(villes);
    }

    /**
     * Exporte toutes les villes d'un département
     * @param codeDepartement code du département
     * @return List<Ville>
     */
    @Transactional(readOnly = true)
    public List<Ville> exportVillesByDepartement(String codeDepartement) {
        Departement departement = departementRepository.findByCode(codeDepartement)
                .orElseThrow(() -> ExceptionFonctionnelle.ressourceNonTrouvee("Département", codeDepartement));

        return villeRepository.findByDepartement(departement, Pageable.unpaged()).getContent();
    }
}