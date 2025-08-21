package fr.diginamic.hello.dao;

import fr.diginamic.hello.models.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO pour la gestion des entités Departement
 * 
 * Cette interface étend JpaRepository pour bénéficier automatiquement des opérations CRUD :
 * - findAll(), findById(), save(), delete(), etc.
 * - Méthodes de requête dérivées basées sur les noms de propriétés
 * - Requêtes JPQL personnalisées avec @Query
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Repository // Annotation Spring pour marquer cette interface comme un repository
public interface DepartementDao extends JpaRepository<Departement, Long> {

    /**
     * Recherche un département par son code
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param code code du département à rechercher
     * @return Optional<Departement> département trouvé ou Optional.empty()
     */
    Optional<Departement> findByCode(String code);

    /**
     * Recherche un département par son nom (insensible à la casse)
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param nom nom du département à rechercher
     * @return Optional<Departement> département trouvé ou Optional.empty()
     */
    Optional<Departement> findByNomIgnoreCase(String nom);

    /**
     * Vérifie si un département existe avec ce code
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param code code à vérifier
     * @return boolean true si un département existe avec ce code
     */
    boolean existsByCode(String code);

    /**
     * Vérifie si un département existe avec ce nom (insensible à la casse)
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param nom nom à vérifier
     * @return boolean true si un département existe avec ce nom
     */
    boolean existsByNomIgnoreCase(String nom);

    /**
     * Recherche les départements dont le nom contient une chaîne de caractères
     * Requête JPQL personnalisée avec recherche par motif
     * 
     * @param nom motif à rechercher dans le nom
     * @return List<Departement> départements dont le nom contient le motif
     */
    @Query("SELECT d FROM Departement d WHERE d.nom LIKE %:nom%")
    List<Departement> findDepartementsContainingNom(@Param("nom") String nom);

    /**
     * Récupère tous les départements triés par code
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @return List<Departement> départements triés par code croissant
     */
    List<Departement> findAllByOrderByCodeAsc();

    /**
     * Récupère tous les départements triés par nom
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @return List<Departement> départements triés par nom croissant
     */
    List<Departement> findAllByOrderByNomAsc();

    /**
     * Compte le nombre de villes dans un département
     * Requête JPQL personnalisée avec fonction COUNT
     * 
     * @param departementId identifiant du département
     * @return Long nombre de villes dans le département
     */
    @Query("SELECT COUNT(v) FROM Ville v WHERE v.departement.id = :departementId")
    Long countVillesInDepartement(@Param("departementId") Long departementId);

    /**
     * Calcule la population totale d'un département
     * Requête JPQL personnalisée avec fonction SUM
     * 
     * @param departementId identifiant du département
     * @return Long population totale du département (peut être null si aucune ville)
     */
    @Query("SELECT COALESCE(SUM(v.nbHabitants), 0) FROM Ville v WHERE v.departement.id = :departementId")
    Long getPopulationTotaleDepartement(@Param("departementId") Long departementId);
}