package fr.diginamic.hello.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Classe représentant une ville avec validation des données
 * Équivalent d'une entité Doctrine en Symfony avec contraintes
 */
@Entity
@Table(name = "ville")
public class Ville {
    /**
     * ID généré automatiquement - ne doit pas être fourni lors de la création
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * Département auquel appartient cette ville
     * Relation Many-to-One obligatoire
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    @NotNull(message = "Le département ne peut pas être null")
    @JsonBackReference // Gestion des références circulaires JSON
    private Departement departement;

    // Constructeur par défaut (obligatoire pour la sérialisation JSON)
    public Ville() {
    }


    // Constructeur sans ID (pour création - ID généré automatiquement)
    public Ville(String nom, Integer nbHabitants) {
        this.nom = nom;
        this.nbHabitants = nbHabitants;
    }

    // Constructeur complet avec département
    public Ville(String nom, Integer nbHabitants, Departement departement) {
        this.nom = nom;
        this.nbHabitants = nbHabitants;
        this.departement = departement;
    }

    // Getters et Setters

    /**
     * Récupère l'ID de la ville
     * @return ID de la ville
     */
    public Long getId() {
        return id;
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
     * Récupère le département de la ville
     * @return département de la ville
     */
    public Departement getDepartement() {
        return departement;
    }

    /**
     * Définit le département de la ville
     * @param departement nouveau département
     */
    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    // Méthode toString() pour faciliter le débogage
    @Override
    public String toString() {
        return "Ville{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nbHabitants=" + nbHabitants +
                ", departement=" + (departement != null ? departement.getCode() : "null") +
                '}';
    }
}
