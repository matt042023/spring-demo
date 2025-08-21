package fr.diginamic.hello.dao;

import fr.diginamic.hello.models.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO pour la gestion des entités Ville
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
public interface VilleDao extends JpaRepository<Ville, Long> {

    /**
     * Recherche une ville par son nom en ignorant la casse
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param nom nom de la ville à rechercher
     * @return Optional<Ville> ville trouvée ou Optional.empty()
     */
    Optional<Ville> findByNomIgnoreCase(String nom);

    /**
     * Vérifie si une ville existe avec ce nom (insensible à la casse)
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param nom nom à vérifier
     * @return boolean true si une ville existe avec ce nom
     */
    boolean existsByNomIgnoreCase(String nom);

    /**
     * Recherche les villes avec un nombre d'habitants dans une fourchette
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param min nombre minimum d'habitants (inclus)
     * @param max nombre maximum d'habitants (inclus)
     * @return List<Ville> liste des villes dans la fourchette
     */
    List<Ville> findByNbHabitantsBetween(Integer min, Integer max);

    /**
     * Recherche les villes avec plus d'habitants qu'un seuil donné,
     * triées par nombre d'habitants décroissant
     * Méthode de requête dérivée Spring Data JPA
     * 
     * @param nbHabitant seuil minimum d'habitants
     * @return List<Ville> villes triées par population décroissante
     */
    List<Ville> findByNbHabitantsGreaterThanOrderByNbHabitantsDesc(Integer nbHabitant);

    /**
     * Récupère les villes triées par nombre d'habitants décroissant
     * Requête JPQL personnalisée
     * 
     * Note : Le paramètre limit n'est actuellement pas utilisé dans la requête.
     * Pour limiter les résultats, utiliser Pageable ou modifier la requête.
     * 
     * @param limit nombre maximum de résultats souhaités (non implémenté)
     * @return List<Ville> toutes les villes triées par population décroissante
     */
    @Query(value = "SELECT v FROM Ville v ORDER BY v.nbHabitants DESC", 
           nativeQuery = false)
    List<Ville> findTopVillesByNbHabitants(@Param("limit") int limit);

    /**
     * Recherche les villes dont le nom contient une chaîne de caractères
     * Requête JPQL personnalisée avec recherche par motif
     * 
     * @param nom motif à rechercher dans le nom
     * @return List<Ville> villes dont le nom contient le motif
     */
    @Query("SELECT v FROM Ville v WHERE v.nom LIKE %:nom%")
    List<Ville> findVillesContainingNom(@Param("nom")String nom);

    /**
     * Recherche les n plus grandes villes d'un département
     * Requête JPQL personnalisée avec tri et limitation
     * 
     * @param departementId identifiant du département
     * @param limit nombre maximum de villes à retourner
     * @return List<Ville> villes triées par population décroissante
     */
    @Query(value = "SELECT v FROM Ville v WHERE v.departement.id = :departementId ORDER BY v.nbHabitants DESC", 
           nativeQuery = false)
    List<Ville> findTopVillesByDepartement(@Param("departementId") Long departementId, @Param("limit") int limit);

    /**
     * Recherche les villes d'un département avec population dans une fourchette
     * Requête JPQL personnalisée avec conditions multiples
     * 
     * @param departementId identifiant du département
     * @param min population minimum (incluse)
     * @param max population maximum (incluse)
     * @return List<Ville> villes du département dans la fourchette de population
     */
    @Query("SELECT v FROM Ville v WHERE v.departement.id = :departementId AND v.nbHabitants BETWEEN :min AND :max")
    List<Ville> findVillesByDepartementAndPopulation(@Param("departementId") Long departementId, 
                                                     @Param("min") Integer min, 
                                                     @Param("max") Integer max);




}
