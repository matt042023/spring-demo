package fr.diginamic.hello.models;

import jakarta.validation.constraints.*;

/**
 * Classe représentant une ville avec validation des données
 * Équivalent d'une entité Doctrine en Symfony avec contraintes
 */
public class Ville {

    /**
     * ID généré automatiquement - ne doit pas être fourni lors de la création
     */
    private int id;

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

    // Constructeur par défaut (obligatoire pour la sérialisation JSON)
    public Ville() {
    }

    // Constructeur avec tous les paramètres (pour les données initiales)
    public Ville(int id, String nom, Integer nbHabitants) {
        this.id = id;
        this.nom = nom;
        this.nbHabitants = nbHabitants;
    }

    // Constructeur sans ID (pour création - ID généré automatiquement)
    public Ville(String nom, Integer nbHabitants) {

        this.nom = nom;
        this.nbHabitants = nbHabitants;
    }

    // Getters et Setters

    /**
     * Récupère l'ID de la ville
     * @return ID de la ville
     */
    public int getId() {
        return id;
    }

    /**
     * Définit l'ID de la ville (usage interne uniquement)
     * @param id nouvel ID de la ville
     */
    public void setId(int id) {
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

    // Méthode toString() pour faciliter le débogage
    @Override
    public String toString() {
        return "Ville{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nbHabitants=" + nbHabitants +
                '}';
    }
}
