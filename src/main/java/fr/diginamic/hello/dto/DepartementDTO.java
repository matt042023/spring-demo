package fr.diginamic.hello.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO (Data Transfer Object) pour l'entité Departement
 * 
 * Cette classe sert à transférer les données de département entre les couches de l'application :
 * - Sépare les données exposées via l'API des entités internes
 * - Inclut la liste des villes associées
 * - Évite les références circulaires dans la sérialisation JSON
 * - Permet de contrôler précisément les données exposées
 * - Ajoute des propriétés calculées (population totale, nombre de villes)
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
public class DepartementDTO {

    /**
     * Identifiant unique du département
     */
    private Long id;

    /**
     * Code du département avec contraintes de validation
     */
    @NotNull(message = "Le code du département ne peut pas être null")
    @Size(min = 2, max = 3, message = "Le code du département doit contenir entre 2 et 3 caractères")
    private String code;

    /**
     * Nom du département avec contraintes de validation
     */
    @NotNull(message = "Le nom du département ne peut pas être null")
    @Size(min = 2, max = 100, message = "Le nom du département doit contenir entre 2 et 100 caractères")
    private String nom;

    /**
     * Liste des villes appartenant à ce département
     * DTO simplifiés pour éviter les références circulaires
     */
    private List<VilleSimplifieDTO> villes;

    /**
     * Population totale du département (calculée)
     */
    private Long populationTotale;

    /**
     * Nombre de villes dans le département (calculé)
     */
    private Integer nombreVilles;

    /**
     * Constructeur par défaut (obligatoire pour la sérialisation JSON)
     */
    public DepartementDTO() {
    }

    /**
     * Constructeur complet
     * @param id identifiant du département
     * @param code code du département
     * @param nom nom du département
     * @param villes liste des villes
     * @param populationTotale population totale calculée
     * @param nombreVilles nombre de villes calculé
     */
    public DepartementDTO(Long id, String code, String nom, List<VilleSimplifieDTO> villes, 
                         Long populationTotale, Integer nombreVilles) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.villes = villes;
        this.populationTotale = populationTotale;
        this.nombreVilles = nombreVilles;
    }

    // ========== GETTERS ET SETTERS ==========

    /**
     * Récupère l'ID du département
     * @return ID du département
     */
    public Long getId() {
        return id;
    }

    /**
     * Définit l'ID du département
     * @param id nouvel ID du département
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Récupère le code du département
     * @return code du département
     */
    public String getCode() {
        return code;
    }

    /**
     * Définit le code du département
     * @param code nouveau code du département
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Récupère le nom du département
     * @return nom du département
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du département
     * @param nom nouveau nom du département
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Récupère la liste des villes
     * @return liste des villes du département
     */
    public List<VilleSimplifieDTO> getVilles() {
        return villes;
    }

    /**
     * Définit la liste des villes
     * @param villes nouvelle liste de villes
     */
    public void setVilles(List<VilleSimplifieDTO> villes) {
        this.villes = villes;
        // Recalcul automatique des statistiques
        if (villes != null) {
            this.nombreVilles = villes.size();
            this.populationTotale = villes.stream()
                    .mapToLong(ville -> ville.getNbHabitants() != null ? ville.getNbHabitants().longValue() : 0L)
                    .sum();
        } else {
            this.nombreVilles = 0;
            this.populationTotale = 0L;
        }
    }

    /**
     * Récupère la population totale du département
     * @return population totale
     */
    public Long getPopulationTotale() {
        return populationTotale;
    }

    /**
     * Définit la population totale
     * @param populationTotale nouvelle population totale
     */
    public void setPopulationTotale(Long populationTotale) {
        this.populationTotale = populationTotale;
    }

    /**
     * Récupère le nombre de villes
     * @return nombre de villes
     */
    public Integer getNombreVilles() {
        return nombreVilles;
    }

    /**
     * Définit le nombre de villes
     * @param nombreVilles nouveau nombre de villes
     */
    public void setNombreVilles(Integer nombreVilles) {
        this.nombreVilles = nombreVilles;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Calcule et met à jour les statistiques du département
     * Méthode utilitaire pour recalculer population totale et nombre de villes
     */
    public void calculerStatistiques() {
        if (villes != null) {
            this.nombreVilles = villes.size();
            this.populationTotale = villes.stream()
                    .mapToLong(ville -> ville.getNbHabitants() != null ? ville.getNbHabitants().longValue() : 0L)
                    .sum();
        } else {
            this.nombreVilles = 0;
            this.populationTotale = 0L;
        }
    }

    /**
     * Vérifie si le département contient des villes
     * @return true si le département a des villes, false sinon
     */
    public boolean hasVilles() {
        return villes != null && !villes.isEmpty();
    }

    // ========== MÉTHODES STANDARD ==========

    /**
     * Méthode toString() pour faciliter le débogage
     */
    @Override
    public String toString() {
        return "DepartementDTO{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", nom='" + nom + '\'' +
                ", nombreVilles=" + nombreVilles +
                ", populationTotale=" + populationTotale +
                '}';
    }

    /**
     * Méthode equals basée sur le code du département
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepartementDTO)) return false;
        DepartementDTO that = (DepartementDTO) o;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    /**
     * Méthode hashCode basée sur le code du département
     */
    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    /**
     * DTO simplifié pour les informations de ville dans DepartementDTO
     * Évite les références circulaires et limite les données exposées
     */
    public static class VilleSimplifieDTO {
        
        /**
         * Identifiant de la ville
         */
        private Long id;

        /**
         * Nom de la ville
         */
        private String nom;

        /**
         * Nombre d'habitants
         */
        private Integer nbHabitants;

        /**
         * Constructeur par défaut
         */
        public VilleSimplifieDTO() {
        }

        /**
         * Constructeur complet
         * @param id identifiant de la ville
         * @param nom nom de la ville
         * @param nbHabitants nombre d'habitants
         */
        public VilleSimplifieDTO(Long id, String nom, Integer nbHabitants) {
            this.id = id;
            this.nom = nom;
            this.nbHabitants = nbHabitants;
        }

        // Getters et Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public Integer getNbHabitants() {
            return nbHabitants;
        }

        public void setNbHabitants(Integer nbHabitants) {
            this.nbHabitants = nbHabitants;
        }

        @Override
        public String toString() {
            return "VilleSimplifieDTO{" +
                    "id=" + id +
                    ", nom='" + nom + '\'' +
                    ", nbHabitants=" + nbHabitants +
                    '}';
        }
    }
}