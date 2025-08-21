package fr.diginamic.hello.services;

import fr.diginamic.hello.dao.DepartementDao;
import fr.diginamic.hello.dao.VilleDao;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant la logique métier des départements
 * 
 * Cette classe implémente la couche service de l'architecture Spring MVC :
 * - Sépare la logique métier du contrôleur REST
 * - Gère les transactions avec @Transactional
 * - Effectue les validations métier avant persistance
 * - Fait le lien entre le contrôleur et la couche DAO
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Service // Annotation Spring pour marquer cette classe comme un service
@Transactional // Toutes les méthodes sont transactionnelles par défaut
public class DepartementService {

    /**
     * Injection de dépendance du DAO Departement pour l'accès aux données
     * Spring injecte automatiquement une instance de DepartementDao
     */
    @Autowired
    private DepartementDao departementDao;

    /**
     * Injection de dépendance du DAO Ville pour les requêtes croisées
     * Spring injecte automatiquement une instance de VilleDao
     */
    @Autowired
    private VilleDao villeDao;

    // ========== OPÉRATIONS CRUD DE BASE ==========

    /**
     * Récupère tous les départements de la base de données
     * 
     * @return List<Departement> liste complète des départements
     */
    @Transactional(readOnly = true) // Optimisation pour les lectures seules
    public List<Departement> extractDepartements() {
        // Délégation directe au DAO pour récupérer tous les départements
        return departementDao.findAll();
    }

    /**
     * Récupère un département par son identifiant
     * 
     * @param idDepartement identifiant unique du département
     * @return Departement département trouvé ou null si inexistant
     */
    @Transactional(readOnly = true)
    public Departement extractDepartement(Long idDepartement) {
        // Recherche par ID avec Optional pour gérer l'absence
        Optional<Departement> departement = departementDao.findById(idDepartement);
        return departement.orElse(null); // Retourne null si non trouvé
    }

    /**
     * Récupère un département par son code
     * 
     * @param code code du département à rechercher
     * @return Departement département trouvé ou null si inexistant
     */
    @Transactional(readOnly = true)
    public Departement extractDepartementByCode(String code) {
        // Recherche par code avec Optional pour gérer l'absence
        Optional<Departement> departement = departementDao.findByCode(code);
        return departement.orElse(null); // Retourne null si non trouvé
    }

    /**
     * Insère un nouveau département après vérifications métier
     * 
     * @param departement nouveau département à insérer
     * @return List<Departement> liste mise à jour des départements
     * @throws IllegalArgumentException si le code ou nom existe déjà
     */
    public List<Departement> insertDepartement(Departement departement) {
        // Vérification métier : le code doit être unique
        if (departementDao.existsByCode(departement.getCode())) {
            throw new IllegalArgumentException("Un département avec ce code existe déjà");
        }

        // Vérification métier : le nom doit être unique (insensible à la casse)
        if (departementDao.existsByNomIgnoreCase(departement.getNom())) {
            throw new IllegalArgumentException("Un département avec ce nom existe déjà");
        }

        // Sauvegarde du nouveau département
        departementDao.save(departement);

        // Retourne la liste complète mise à jour
        return departementDao.findAll();
    }

    /**
     * Modifie un département existant après vérifications métier
     * 
     * @param idDepartement identifiant du département à modifier
     * @param departementModifie nouvelles données du département
     * @return List<Departement> liste mise à jour des départements
     * @throws IllegalArgumentException si département inexistant ou code/nom déjà pris
     */
    public List<Departement> modifierDepartement(Long idDepartement, Departement departementModifie) {
        // Vérification que le département à modifier existe
        Optional<Departement> departementExistant = departementDao.findById(idDepartement);
        if (departementExistant.isEmpty()) {
            throw new IllegalArgumentException("Département non trouvé avec l'ID: " + idDepartement);
        }

        Departement departement = departementExistant.get();

        // Vérification que le nouveau code n'est pas déjà pris par un autre département
        if (!departement.getCode().equals(departementModifie.getCode()) &&
                departementDao.existsByCode(departementModifie.getCode())) {
            throw new IllegalArgumentException("Le code de département est déjà utilisé");
        }

        // Vérification que le nouveau nom n'est pas déjà pris par un autre département
        if (!departement.getNom().equalsIgnoreCase(departementModifie.getNom()) &&
                departementDao.existsByNomIgnoreCase(departementModifie.getNom())) {
            throw new IllegalArgumentException("Le nom de département est déjà utilisé");
        }

        // Mise à jour des données (l'ID reste inchangé)
        departement.setCode(departementModifie.getCode());
        departement.setNom(departementModifie.getNom());

        // Sauvegarde des modifications
        departementDao.save(departement);

        // Retourne la liste complète mise à jour
        return departementDao.findAll();
    }

    /**
     * Supprime un département de la base de données
     * 
     * @param idDepartement identifiant du département à supprimer
     * @return List<Departement> liste mise à jour des départements
     * @throws IllegalArgumentException si le département n'existe pas ou contient des villes
     */
    public List<Departement> supprimerDepartement(Long idDepartement) {
        // Vérification que le département à supprimer existe
        if (!departementDao.existsById(idDepartement)) {
            throw new IllegalArgumentException("Département non trouvé avec l'ID: " + idDepartement);
        }

        // Vérification que le département ne contient pas de villes
        Long nombreVilles = departementDao.countVillesInDepartement(idDepartement);
        if (nombreVilles > 0) {
            throw new IllegalArgumentException("Impossible de supprimer un département qui contient des villes (" + nombreVilles + " ville(s))");
        }

        // Suppression physique en base de données
        departementDao.deleteById(idDepartement);

        // Retourne la liste complète mise à jour
        return departementDao.findAll();
    }

    // ========== MÉTHODES DE RECHERCHE AVANCÉES ==========

    /**
     * Recherche des départements par tranche de code
     * 
     * @param codePattern motif à rechercher dans les codes
     * @return List<Departement> départements dont le code contient le motif
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsParCode(String codePattern) {
        // Utilisation d'une méthode de requête dérivée personnalisée
        return departementDao.findDepartementsContainingNom(codePattern);
    }

    /**
     * Récupère tous les départements triés par code
     * 
     * @return List<Departement> départements triés par code croissant
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsOrderByCode() {
        // Utilisation d'une méthode de requête dérivée Spring Data JPA
        return departementDao.findAllByOrderByCodeAsc();
    }

    /**
     * Récupère tous les départements triés par nom
     * 
     * @return List<Departement> départements triés par nom croissant
     */
    @Transactional(readOnly = true)
    public List<Departement> findDepartementsOrderByNom() {
        // Utilisation d'une méthode de requête dérivée Spring Data JPA
        return departementDao.findAllByOrderByNomAsc();
    }

    // ========== MÉTHODES SPÉCIFIQUES AUX VILLES ==========

    /**
     * Liste les n plus grandes villes d'un département
     * 
     * @param idDepartement identifiant du département
     * @param limit nombre maximum de villes à retourner
     * @return List<Ville> villes triées par population décroissante
     * @throws IllegalArgumentException si le département n'existe pas
     */
    @Transactional(readOnly = true)
    public List<Ville> getTopVillesByDepartement(Long idDepartement, int limit) {
        // Vérification que le département existe
        if (!departementDao.existsById(idDepartement)) {
            throw new IllegalArgumentException("Département non trouvé avec l'ID: " + idDepartement);
        }

        // Utilisation d'une requête JPQL personnalisée avec limitation
        return villeDao.findTopVillesByDepartement(idDepartement, limit);
    }

    /**
     * Liste les villes d'un département avec population dans une fourchette
     * 
     * @param idDepartement identifiant du département
     * @param min population minimum (incluse)
     * @param max population maximum (incluse)
     * @return List<Ville> villes du département dans la fourchette
     * @throws IllegalArgumentException si le département n'existe pas ou fourchette invalide
     */
    @Transactional(readOnly = true)
    public List<Ville> getVillesByDepartementAndPopulation(Long idDepartement, Integer min, Integer max) {
        // Vérification que le département existe
        if (!departementDao.existsById(idDepartement)) {
            throw new IllegalArgumentException("Département non trouvé avec l'ID: " + idDepartement);
        }

        // Vérification de la cohérence de la fourchette
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("La population minimum ne peut pas être supérieure à la population maximum");
        }

        // Utilisation d'une requête JPQL personnalisée avec conditions multiples
        return villeDao.findVillesByDepartementAndPopulation(idDepartement, min, max);
    }

    // ========== MÉTHODES D'ANALYSE ==========

    /**
     * Calcule la population totale d'un département
     * 
     * @param idDepartement identifiant du département
     * @return Long population totale du département
     * @throws IllegalArgumentException si le département n'existe pas
     */
    @Transactional(readOnly = true)
    public Long getPopulationTotaleDepartement(Long idDepartement) {
        // Vérification que le département existe
        if (!departementDao.existsById(idDepartement)) {
            throw new IllegalArgumentException("Département non trouvé avec l'ID: " + idDepartement);
        }

        // Utilisation d'une requête JPQL avec fonction d'agrégation
        return departementDao.getPopulationTotaleDepartement(idDepartement);
    }

    /**
     * Compte le nombre de villes dans un département
     * 
     * @param idDepartement identifiant du département
     * @return Long nombre de villes dans le département
     * @throws IllegalArgumentException si le département n'existe pas
     */
    @Transactional(readOnly = true)
    public Long getNombreVillesDepartement(Long idDepartement) {
        // Vérification que le département existe
        if (!departementDao.existsById(idDepartement)) {
            throw new IllegalArgumentException("Département non trouvé avec l'ID: " + idDepartement);
        }

        // Utilisation d'une requête JPQL avec fonction COUNT
        return departementDao.countVillesInDepartement(idDepartement);
    }
}