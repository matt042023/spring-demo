package fr.diginamic.hello.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un département français avec validation des données
 * 
 * Cette entité JPA gère la relation One-to-Many avec les villes :
 * - Un département peut contenir plusieurs villes
 * - Une ville appartient à un seul département
 * - Utilisation de @JsonManagedReference pour éviter les références circulaires
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "departement")
public class Departement {

    /**
     * ID généré automatiquement - clé primaire
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code du département (ex: "75", "13", "2A")
     */
    @NotNull(message = "Le code du département ne peut pas être null")
    @Size(min = 2, max = 3, message = "Le code du département doit contenir entre 2 et 3 caractères")
    @Column(name = "code", unique = true)
    private String code;

    /**
     * Nom du département (ex: "Paris", "Bouches-du-Rhône")
     */
    @NotNull(message = "Le nom du département ne peut pas être null")
    @Size(min = 2, max = 100, message = "Le nom du département doit contenir entre 2 et 100 caractères")
    @Column(name = "nom")
    private String nom;

    /**
     * Liste des villes appartenant à ce département
     * Relation One-to-Many avec cascade et fetch lazy pour les performances
     */
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Gestion des références circulaires JSON
    private List<Ville> villes = new ArrayList<>();

    /**
     * Constructeur par défaut (obligatoire pour JPA et la sérialisation JSON)
     */
    public Departement() {
    }

    /**
     * Constructeur pour création d'un département
     * @param code code du département
     * @param nom nom du département
     */
    public Departement(String code, String nom) {
        this.code = code;
        this.nom = nom;
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
     * Récupère la liste des villes du département
     * @return List<Ville> liste des villes
     */
    public List<Ville> getVilles() {
        return villes;
    }

    /**
     * Définit la liste des villes du département
     * @param villes nouvelle liste de villes
     */
    public void setVilles(List<Ville> villes) {
        this.villes = villes;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Ajoute une ville à ce département et définit la relation bidirectionnelle
     * @param ville ville à ajouter
     */
    public void addVille(Ville ville) {
        if (ville != null) {
            this.villes.add(ville);
            ville.setDepartement(this);
        }
    }

    /**
     * Retire une ville de ce département
     * @param ville ville à retirer
     */
    public void removeVille(Ville ville) {
        if (ville != null) {
            this.villes.remove(ville);
            ville.setDepartement(null);
        }
    }

    /**
     * Calcule la population totale du département
     * @return population totale de toutes les villes du département
     */
    public Integer getPopulationTotale() {
        return villes.stream()
                .mapToInt(ville -> ville.getNbHabitants() != null ? ville.getNbHabitants() : 0)
                .sum();
    }

    /**
     * Récupère le nombre de villes dans ce département
     * @return nombre de villes
     */
    public int getNombreVilles() {
        return villes.size();
    }

    // ========== MÉTHODES STANDARD ==========

    /**
     * Méthode toString() pour faciliter le débogage
     */
    @Override
    public String toString() {
        return "Departement{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", nom='" + nom + '\'' +
                ", nombreVilles=" + villes.size() +
                '}';
    }

    /**
     * Méthode equals basée sur le code du département
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Departement)) return false;
        Departement that = (Departement) o;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    /**
     * Méthode hashCode basée sur le code du département
     */
    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}