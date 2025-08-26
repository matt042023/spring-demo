package fr.diginamic.hello.repositories;

import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.models.Departement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Classe utilitaire pour faciliter l'utilisation des repositories
 *
 * Fournit des méthodes simplifiées pour les cas d'usage courants
 * et encapsule la logique de création des objets Pageable.
 */
@Component
public class VilleRepositoryHelper {

    @Autowired
    private VilleRepository villeRepository;

    @Autowired
    private DepartementRepository departementRepository;

    // ==================== MÉTHODES SIMPLIFIÉES POUR LES CAS D'USAGE DU TP ====================

    /**
     * Recherche des N villes les plus peuplées d'un département par code
     * Méthode simplifiée qui prend en charge la création du Pageable
     * @param codeDepartement code du département
     * @param n nombre de villes à récupérer
     * @return List<Ville> les N villes les plus peuplées
     */
    public List<Ville> findTopNVillesByCodeDepartement(String codeDepartement, int n) {
        Pageable pageable = PageRequest.of(0, n);
        return villeRepository.findTopNVillesByCodeDepartement(codeDepartement, pageable);
    }

    /**
     * Recherche des N villes les plus peuplées d'un département
     * @param departement le département
     * @param n nombre de villes à récupérer
     * @return List<Ville>
     */
    public List<Ville> findTopNVillesByDepartement(Departement departement, int n) {
        Pageable pageable = PageRequest.of(0, n);
        return villeRepository.findTopNVillesByDepartementOrderByPopulation(departement, pageable);
    }

    /**
     * Recherche d'un département par code avec gestion d'erreur
     * @param code code du département
     * @return Departement
     * @throws RuntimeException si le département n'existe pas
     */
    public Departement findDepartementByCodeOrThrow(String code) {
        return departementRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec le code : " + code));
    }

    /**
     * Recherche des villes d'un département par code département et population minimum
     * @param codeDepartement code du département
     * @param minPopulation population minimum
     * @return List<Ville>
     */
    public List<Ville> findVillesByCodeDepartementAndMinPopulation(String codeDepartement, Integer minPopulation) {
        Optional<Departement> departement = departementRepository.findByCode(codeDepartement);
        if (departement.isPresent()) {
            return villeRepository.findByDepartementAndNbHabitantsGreaterThanOrderByNbHabitantsDesc(
                    departement.get(), minPopulation);
        }
        return List.of(); // Retourne une liste vide si le département n'existe pas
    }

    /**
     * Recherche des villes d'un département par code département et plage de population
     * @param codeDepartement code du département
     * @param minPopulation population minimum
     * @param maxPopulation population maximum
     * @return List<Ville>
     */
    public List<Ville> findVillesByCodeDepartementAndPopulationRange(
            String codeDepartement, Integer minPopulation, Integer maxPopulation) {
        Optional<Departement> departement = departementRepository.findByCode(codeDepartement);
        if (departement.isPresent()) {
            return villeRepository.findByDepartementAndNbHabitantsBetweenOrderByNbHabitantsDesc(
                    departement.get(), minPopulation, maxPopulation);
        }
        return List.of();
    }

    // ==================== MÉTHODES D'AGRÉGATION ====================

    /**
     * Récupère des statistiques complètes sur un département
     * @param codeDepartement code du département
     * @return DepartementStats objet contenant les statistiques
     */
    public DepartementStats getDepartementStats(String codeDepartement) {
        Optional<Departement> deptOpt = departementRepository.findByCode(codeDepartement);
        if (deptOpt.isEmpty()) {
            return null;
        }

        Departement dept = deptOpt.get();
        Long nombreVilles = villeRepository.countByDepartement(dept);
        Long populationTotale = villeRepository.sumPopulationByDepartement(dept);
        Optional<Ville> villeLaPlusPeuplee = villeRepository.findMostPopulatedVilleInDepartement(dept);

        return new DepartementStats(dept, nombreVilles, populationTotale, villeLaPlusPeuplee.orElse(null));
    }

    // ==================== CLASSE INTERNE POUR LES STATISTIQUES ====================

    /**
     * Classe interne pour encapsuler les statistiques d'un département
     */
    public static class DepartementStats {
        private final Departement departement;
        private final Long nombreVilles;
        private final Long populationTotale;
        private final Ville villeLaPlusPeuplee;

        public DepartementStats(Departement departement, Long nombreVilles,
                                Long populationTotale, Ville villeLaPlusPeuplee) {
            this.departement = departement;
            this.nombreVilles = nombreVilles;
            this.populationTotale = populationTotale;
            this.villeLaPlusPeuplee = villeLaPlusPeuplee;
        }

        // Getters
        public Departement getDepartement() { return departement; }
        public Long getNombreVilles() { return nombreVilles; }
        public Long getPopulationTotale() { return populationTotale; }
        public Ville getVilleLaPlusPeuplee() { return villeLaPlusPeuplee; }

        @Override
        public String toString() {
            return String.format("DepartementStats{departement=%s (%s), nombreVilles=%d, populationTotale=%d, villeLaPlusPeuplee=%s}",
                    departement.getNom(), departement.getCode(), nombreVilles, populationTotale,
                    villeLaPlusPeuplee != null ? villeLaPlusPeuplee.getNom() : "N/A");
        }
    }
}