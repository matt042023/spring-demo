package fr.diginamic.hello.repositories;

import fr.diginamic.hello.models.Departement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository pour l'entité Departement
 *
 * Utilise Spring Data JPA pour générer automatiquement les implémentations
 * des méthodes CRUD de base.
 *
 * Remplace l'ancien DepartementDao pour une approche plus moderne
 * et plus simple avec Spring Data.
 */
@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {

    // ==================== MÉTHODES DE RECHERCHE DE BASE ====================

    /**
     * Trouve un département par son code avec ses villes
     */
    @Query("SELECT d FROM Departement d " +
            "LEFT JOIN FETCH d.villes v " +
            "WHERE d.code = :code " +
            "ORDER BY v.nbHabitants DESC")
    Optional<Departement> findByCode(@Param("code") String code);

    /**
     * Recherche un département par son nom (si le nom n'est pas null)
     * @param nom nom du département
     * @return Optional<Departement>
     */
    Optional<Departement> findByNom(String nom);

    /**
     * Recherche des départements par nom ou code contenant la chaîne de recherche (insensible à la casse)
     * @param searchTerm terme de recherche
     * @return List<Departement>
     */
    @Query("SELECT d FROM Departement d WHERE " +
            "LOWER(d.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Departement> findByNomContainingOrCodeContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Recherche tous les départements qui ont un nom (nom non null)
     * @return List<Departement>
     */
    List<Departement> findByNomIsNotNull();

    /**
     * Recherche tous les départements qui n'ont pas de nom (nom = null)
     * @return List<Departement>
     */
    List<Departement> findByNomIsNull();

    // ==================== MÉTHODES AVEC REQUÊTES PERSONNALISÉES ====================

    /**
     * Recherche les départements contenant au moins une ville
     * @return List<Departement>
     */
    @Query("SELECT DISTINCT d FROM Departement d JOIN d.villes v")
    List<Departement> findDepartementsWithVilles();

    /**
     * Recherche les départements par nombre minimum de villes
     * @param minNombreVilles nombre minimum de villes
     * @return List<Departement>
     */
    @Query("SELECT d FROM Departement d WHERE SIZE(d.villes) >= :minNombreVilles")
    List<Departement> findDepartementsWithMinVilles(@Param("minNombreVilles") int minNombreVilles);

    /**
     * Recherche les départements par population totale minimum
     * @param minPopulation population minimum totale
     * @return List<Departement>
     */
    @Query("SELECT d FROM Departement d WHERE " +
            "(SELECT SUM(v.nbHabitants) FROM Ville v WHERE v.departement = d) >= :minPopulation")
    List<Departement> findDepartementsWithMinPopulation(@Param("minPopulation") Long minPopulation);

    /**
     * Compte le nombre de villes par département
     * @param departement le département
     * @return nombre de villes
     */
    @Query("SELECT COUNT(v) FROM Ville v WHERE v.departement = :departement")
    Long countVillesByDepartement(@Param("departement") Departement departement);

    /**
     * Calcule la population totale d'un département
     * @param departement le département
     * @return population totale
     */
    @Query("SELECT SUM(v.nbHabitants) FROM Ville v WHERE v.departement = :departement")
    Long getTotalPopulationByDepartement(@Param("departement") Departement departement);

    // ==================== RECHERCHES PAR CODE ====================

    /**
     * Vérifie si un département existe par son code
     * @param code code du département
     * @return true si le département existe
     */
    boolean existsByCode(String code);

    /**
     * Recherche tous les départements dont le code commence par un préfixe
     * Utile pour les DOM-TOM (codes à 3 chiffres)
     * @param prefix préfixe du code
     * @return List<Departement>
     */
    List<Departement> findByCodeStartingWith(String prefix);

    /**
     * Recherche les départements métropolitains (codes numériques à 2 chiffres)
     * @return List<Departement>
     */
    @Query("SELECT d FROM Departement d WHERE d.code NOT LIKE '97%' AND d.code NOT LIKE '2%'")
    List<Departement> findDepartementsMetropolitains();

    /**
     * Recherche les départements d'outre-mer (codes commençant par 97)
     * @return List<Departement>
     */
    @Query("SELECT d FROM Departement d WHERE d.code LIKE '97%'")
    List<Departement> findDepartementsOutreMer();

    /**
     * Recherche les départements corses (codes 2A et 2B)
     * @return List<Departement>
     */
    @Query("SELECT d FROM Departement d WHERE d.code IN ('2A', '2B')")
    List<Departement> findDepartementsCorse();

    // ==================== REQUÊTES POUR TRI AVEC PAGINATION ====================

    /**
     * Recherche tous les départements triés par nom avec pagination
     * @param pageable informations de pagination
     * @return Page<Departement>
     */
    @Query("SELECT d FROM Departement d ORDER BY d.nom ASC")
    Page<Departement> findAllOrderByNom(Pageable pageable);

    /**
     * Recherche tous les départements triés par code avec pagination
     * @param pageable informations de pagination
     * @return Page<Departement>
     */
    @Query("SELECT d FROM Departement d ORDER BY d.code ASC")
    Page<Departement> findAllOrderByCode(Pageable pageable);

    /**
     * Recherche tous les départements triés par population totale (décroissant) avec pagination
     * @param pageable informations de pagination
     * @return Page<Departement>
     */
    @Query("SELECT d FROM Departement d " +
           "ORDER BY (SELECT COALESCE(SUM(v.nbHabitants), 0) FROM Ville v WHERE v.departement = d) DESC")
    Page<Departement> findAllOrderByPopulation(Pageable pageable);

    /**
     * Recherche tous les départements triés par nombre de villes (décroissant) avec pagination
     * @param pageable informations de pagination
     * @return Page<Departement>
     */
    @Query("SELECT d FROM Departement d " +
           "ORDER BY (SELECT COUNT(v) FROM Ville v WHERE v.departement = d) DESC")
    Page<Departement> findAllOrderByNombreVilles(Pageable pageable);


}