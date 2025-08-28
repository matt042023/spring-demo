package fr.diginamic.hello.repositories;

import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.models.Departement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository pour l'entité Ville
 *
 * Utilise Spring Data JPA pour générer automatiquement les implémentations
 * des méthodes de base (save, findById, findAll, delete, etc.)
 *
 * Méthodes personnalisées selon les besoins du TP :
 * - Recherche par nom (exact et par préfixe)
 * - Recherche par population (min, max, plage)
 * - Recherche par département avec filtres de population
 * - Recherche des N villes les plus peuplées d'un département
 */
@Repository
public interface VilleRepository extends JpaRepository<Ville, Long> {

    // ==================== MÉTHODES DE BASE ====================

    /**
     * Recherche une ville par son nom exact (méthode générée automatiquement)
     * @param nom nom de la ville
     * @return Optional<Ville>
     */
    Optional<Ville> findByNom(String nom);

    /**
     * Recherche toutes les villes par nom (si plusieurs villes ont le même nom)
     * @param nom nom de la ville
     * @return List<Ville>
     */
    List<Ville> findByNomContainingIgnoreCase(String nom);

    // ==================== RECHERCHES PAR PRÉFIXE ====================

    /**
     * Recherche de toutes les villes dont le nom commence par une chaîne de caractères donnée
     * @param prefix chaîne de caractères de début
     * @return List<Ville> triée par nom
     */
    List<Ville> findByNomStartingWithIgnoreCaseOrderByNom(String prefix);

    // ==================== RECHERCHES PAR POPULATION ====================

    /**
     * Recherche de toutes les villes dont la population est supérieure à min
     * Les villes sont retournées par population descendante
     * @param minPopulation population minimum
     * @return List<Ville> triée par population décroissante
     */
    List<Ville> findByNbHabitantsGreaterThanOrderByNbHabitantsDesc(Integer minPopulation);

    /**
     * Recherche de toutes les villes dont la population est entre min et max (inclus)
     * Les villes sont retournées par population descendante
     * @param minPopulation population minimum (incluse)
     * @param maxPopulation population maximum (incluse)
     * @return List<Ville> triée par population décroissante
     */
    List<Ville> findByNbHabitantsBetweenOrderByNbHabitantsDesc(Integer minPopulation, Integer maxPopulation);

    // ==================== RECHERCHES PAR DÉPARTEMENT ====================

    /**
     * Recherche de toutes les villes d'un département dont la population est supérieure à min
     * Les villes sont retournées par population descendante
     * @param departement le département
     * @param minPopulation population minimum
     * @return List<Ville> triée par population décroissante
     */
    List<Ville> findByDepartementAndNbHabitantsGreaterThanOrderByNbHabitantsDesc(
            Departement departement, Integer minPopulation);

    /**
     * Recherche de toutes les villes d'un département dont la population est entre min et max
     * Les villes sont retournées par population descendante
     * @param departement le département
     * @param minPopulation population minimum (incluse)
     * @param maxPopulation population maximum (incluse)
     * @return List<Ville> triée par population décroissante
     */
    List<Ville> findByDepartementAndNbHabitantsBetweenOrderByNbHabitantsDesc(
            Departement departement, Integer minPopulation, Integer maxPopulation);

    // ==================== RECHERCHES AVANCÉES AVEC @Query ====================

    /**
     * Recherche des n villes les plus peuplées d'un département donné
     * Utilise une requête JPQL personnalisée
     * @param departement le département
     * @param limit nombre maximum de villes à retourner
     * @return List<Ville> des n villes les plus peuplées du département
     */
    @Query("SELECT v FROM Ville v WHERE v.departement = :departement ORDER BY v.nbHabitants DESC")
    List<Ville> findTopNVillesByDepartementOrderByPopulation(
            @Param("departement") Departement departement,
            Pageable pageable);

    /**
     * Recherche des n villes les plus peuplées d'un département par code département
     * Version alternative utilisant le code du département
     * @param codeDepartement code du département (ex: "75", "13")
     * @param limit nombre de villes à retourner
     * @return List<Ville>
     */
    @Query("SELECT v FROM Ville v WHERE v.departement.code = :codeDepartement ORDER BY v.nbHabitants DESC")
    List<Ville> findTopNVillesByCodeDepartement(
            @Param("codeDepartement") String codeDepartement,
            Pageable pageable);

    // ==================== MÉTHODES STATISTIQUES ====================

    /**
     * Compte le nombre de villes d'un département
     * @param departement le département
     * @return nombre de villes
     */
    Long countByDepartement(Departement departement);

    /**
     * Calcule la population totale d'un département
     * @param departement le département
     * @return population totale
     */
    @Query("SELECT SUM(v.nbHabitants) FROM Ville v WHERE v.departement = :departement")
    Long sumPopulationByDepartement(@Param("departement") Departement departement);

    /**
     * Trouve la ville la plus peuplée d'un département
     * @param departement le département
     * @return Optional<Ville> la ville la plus peuplée
     */
    @Query("SELECT v FROM Ville v WHERE v.departement = :departement ORDER BY v.nbHabitants DESC")
    List<Ville> findVillesByDepartementOrderByPopulationDesc(@Param("departement") Departement departement, Pageable pageable);
    
    default Optional<Ville> findMostPopulatedVilleInDepartement(Departement departement) {
        List<Ville> villes = findVillesByDepartementOrderByPopulationDesc(departement, PageRequest.of(0, 1));
        return villes.isEmpty() ? Optional.empty() : Optional.of(villes.get(0));
    }

    // ==================== RECHERCHE PAGINÉE ====================

    /**
     * Recherche paginée de toutes les villes
     * @param pageable informations de pagination
     * @return Page<Ville>
     */
    Page<Ville> findAll(Pageable pageable);

    /**
     * Recherche paginée des villes d'un département
     * @param departement le département
     * @param pageable informations de pagination
     * @return Page<Ville>
     */
    Page<Ville> findByDepartement(Departement departement, Pageable pageable);

    // ==================== REQUÊTES NATIVES (OPTIONNELLES) ====================

    /**
     * Recherche par requête SQL native (exemple pour des cas très spécifiques)
     * @param minPopulation population minimum
     * @return List<Ville>
     */
    @Query(value = "SELECT * FROM ville WHERE nb_habs > :minPopulation ORDER BY nb_habs DESC",
            nativeQuery = true)
    List<Ville> findVillesWithPopulationGreaterThanNative(@Param("minPopulation") Integer minPopulation);


    /**
     * Trouve toutes les villes dont la population est supérieure au seuil donné
     * Avec jointure pour récupérer les informations du département
     */
    @Query("SELECT v FROM Ville v " +
            "JOIN FETCH v.departement d " +
            "WHERE v.nbHabitants > :populationMinimum " +
            "ORDER BY v.nbHabitants DESC")
    List<Ville> findByNbHabitantsGreaterThan(@Param("populationMinimum") int populationMinimum);
}