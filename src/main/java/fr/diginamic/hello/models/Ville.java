package fr.diginamic.hello.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Classe représentant une ville avec validation des données
 * Mapping JPA compatible avec le fichier tp-spring-07-recensement.sql
 *
 * Structure SQL correspondante :
 * - Table : ville
 * - Colonnes : id, nom, id_dept, nb_habs
 */
@Entity
@Table(name = "ville")
public class Ville {

    /**
     * ID de la ville - correspond à la colonne "id" du SQL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la ville - correspond à la colonne "nom" du SQL
     */
    @NotNull(message = "{ville.nom.notnull}")
    @Size(min = 2, max = 100, message = "{ville.nom.size}")
    @Column(name = "nom")
    private String nom;

    /**
     * Nombre d'habitants - correspond à la colonne "nb_habs" du SQL
     */
    @NotNull(message = "{ville.nbHabitants.notnull}")
    @Min(value = 1, message = "{ville.nbHabitants.min}")
    @Max(value = 50000000, message = "{ville.nbHabitants.max}")
    @Column(name = "nb_habs")
    private Integer nbHabitants;

    /**
     * Département - correspond à la colonne "id_dept" du SQL (clé étrangère)
     * Relation Many-to-One vers Departement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dept", nullable = false)
    @NotNull(message = "{ville.departement.notnull}")
    @JsonBackReference // Évite les références circulaires JSON
    private Departement departement;

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur par défaut (obligatoire pour JPA et JSON)
     */
    public Ville() {
    }

    /**
     * Constructeur sans département (pour création simple)
     * @param nom nom de la ville
     * @param nbHabitants nombre d'habitants
     */
    public Ville(String nom, Integer nbHabitants) {
        this.nom = nom;
        this.nbHabitants = nbHabitants;
    }

    /**
     * Constructeur complet pour data.sql
     * @param id id de la ville
     * @param nom nom de la ville
     * @param departement département de rattachement
     * @param nbHabitants nombre d'habitants
     */
    public Ville(int id,String nom,Departement departement, Integer nbHabitants) {
        this.nom = nom;
        this.nbHabitants = nbHabitants;
        this.departement = departement;
    }

    /**
     * Constructeur complet
     * @param nom nom de la ville
     * @param nbHabitants nombre d'habitants
     * @param departement département de rattachement
     */
    public Ville(String nom, Integer nbHabitants, Departement departement) {
        this.nom = nom;
        this.nbHabitants = nbHabitants;
        this.departement = departement;
    }

    // ==================== GETTERS ET SETTERS ====================

    /**
     * Récupère l'ID de la ville
     * @return ID de la ville
     */
    public Long getId() {
        return id;
    }

    /**
     * Définit l'ID de la ville (généralement pas utilisé car auto-généré)
     * @param id nouveau ID
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
     * @param nom nouveau nom
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
     * Récupère le département
     * @return département de la ville
     */
    public Departement getDepartement() {
        return departement;
    }

    /**
     * Définit le département
     * @param departement nouveau département
     */
    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Représentation textuelle de la ville
     */
    @Override
    public String toString() {
        return "Ville{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nbHabitants=" + nbHabitants +
                ", departement=" + (departement != null ? departement.getCode() : "null") +
                '}';
    }

    /**
     * Méthode equals basée sur l'ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ville)) return false;
        Ville ville = (Ville) o;
        return id != null ? id.equals(ville.id) : ville.id == null;
    }

    /**
     * Méthode hashCode basée sur l'ID
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
