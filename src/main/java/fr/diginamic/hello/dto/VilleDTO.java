package fr.diginamic.hello.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) pour l'entité Ville
 * 
 * Cette classe sert à transférer les données de ville entre les couches de l'application :
 * - Sépare les données exposées via l'API des entités internes
 * - Inclut les informations du département associé
 * - Évite les références circulaires dans la sérialisation JSON
 * - Permet de contrôler précisément les données exposées
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
public class VilleDTO {

    /**
     * Identifiant unique de la ville
     */
    private Long id;

    /**
     * Nom de la ville avec contraintes de validation
     */
    @NotNull(message = "Le nom de la ville ne peut pas être null")
    @Size(min = 2, max = 100, message = "Le nom de la ville doit contenir entre 2 et 100 caractères")
    private String nom;

    /**
     * Nombre d'habitants avec contraintes de validation
     */
    @NotNull(message = "Le nombre d'habitants ne peut pas être null")
    @Min(value = 1, message = "Le nombre d'habitants doit être au minimum de 1")
    @Max(value = 50000000, message = "Le nombre d'habitants ne peut pas dépasser 50 millions")
    private Integer nbHabitants;

    /**
     * Informations du département associé à cette ville
     * DTO imbriqué pour éviter les références circulaires
     */
    @NotNull(message = "Le département ne peut pas être null")
    @Valid
    private DepartementSimplifieDTO departement;

    /**
     * Constructeur par défaut (obligatoire pour la sérialisation JSON)
     */
    public VilleDTO() {
    }

    /**
     * Constructeur complet
     * @param id identifiant de la ville
     * @param nom nom de la ville
     * @param nbHabitants nombre d'habitants
     * @param departement département associé
     */
    public VilleDTO(Long id, String nom, Integer nbHabitants, DepartementSimplifieDTO departement) {
        this.id = id;
        this.nom = nom;
        this.nbHabitants = nbHabitants;
        this.departement = departement;
    }

    // ========== GETTERS ET SETTERS ==========

    /**
     * Récupère l'ID de la ville
     * @return ID de la ville
     */
    public Long getId() {
        return id;
    }

    /**
     * Définit l'ID de la ville
     * @param id nouvel ID de la ville
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Récupère le nom de la ville
     * @return nom de la ville
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom de la ville
     * @param nom nouveau nom de la ville
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Récupère le nombre d'habitants
     * @return nombre d'habitants
     */
    public Integer getNbHabitants() {
        return nbHabitants;
    }

    /**
     * Définit le nombre d'habitants
     * @param nbHabitants nouveau nombre d'habitants
     */
    public void setNbHabitants(Integer nbHabitants) {
        this.nbHabitants = nbHabitants;
    }

    /**
     * Récupère les informations du département
     * @return département associé
     */
    public DepartementSimplifieDTO getDepartement() {
        return departement;
    }

    /**
     * Définit le département associé
     * @param departement nouveau département
     */
    public void setDepartement(DepartementSimplifieDTO departement) {
        this.departement = departement;
    }

    // ========== MÉTHODES STANDARD ==========

    /**
     * Méthode toString() pour faciliter le débogage
     */
    @Override
    public String toString() {
        return "VilleDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nbHabitants=" + nbHabitants +
                ", departement=" + departement +
                '}';
    }

    /**
     * Méthode equals basée sur l'ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VilleDTO)) return false;
        VilleDTO villeDTO = (VilleDTO) o;
        return id != null ? id.equals(villeDTO.id) : villeDTO.id == null;
    }

    /**
     * Méthode hashCode basée sur l'ID
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * DTO simplifié pour les informations de département dans VilleDTO
     * Évite les références circulaires et limite les données exposées
     */
    public static class DepartementSimplifieDTO {
        
        /**
         * Identifiant du département
         */
        private Long id;

        /**
         * Code du département
         */
        private String code;

        /**
         * Nom du département
         */
        private String nom;

        /**
         * Constructeur par défaut
         */
        public DepartementSimplifieDTO() {
        }

        /**
         * Constructeur complet
         * @param id identifiant du département
         * @param code code du département
         * @param nom nom du département
         */
        public DepartementSimplifieDTO(Long id, String code, String nom) {
            this.id = id;
            this.code = code;
            this.nom = nom;
        }

        // Getters et Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        @Override
        public String toString() {
            return "DepartementSimplifieDTO{" +
                    "id=" + id +
                    ", code='" + code + '\'' +
                    ", nom='" + nom + '\'' +
                    '}';
        }
    }
}