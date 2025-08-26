package fr.diginamic.hello.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant un département français
 * Mapping JPA compatible avec le fichier tp-spring-07-recensement.sql
 *
 * Structure SQL correspondante :
 * - Table : departement
 * - Colonnes : id, code, nom
 *
 * Note : Dans le fichier SQL, la colonne 'nom' est NULL,
 * mais on la garde nullable pour permettre d'ajouter les noms plus tard
 */
@Entity
@Table(name = "departement")
public class Departement {

    /**
     * ID du département - correspond à la colonne "id" du SQL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Code du département - correspond à la colonne "code" du SQL
     * Ex: "75", "13", "2A", "974"
     */
    @NotNull(message = "{departement.code.notnull}")
    @Size(min = 2, max = 3, message = "{departement.code.size}")
    @Column(name = "code", unique = true, nullable = false)
    private String code;

    /**
     * Nom du département - correspond à la colonne "nom" du SQL
     * Dans le fichier SQL original, cette colonne est NULL, donc on la rend nullable
     */
    @Size(max = 100, message = "{departement.nom.size}")
    @Column(name = "nom", nullable = true)
    private String nom;

    /**
     * Liste des villes appartenant à ce département
     * Relation One-to-Many bidirectionnelle
     * mappedBy fait référence à l'attribut 'departement' dans la classe Ville
     */
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Gestion des références circulaires JSON
    private List<Ville> villes = new ArrayList<>();

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur par défaut (obligatoire pour JPA)
     */
    public Departement() {
    }

    /**
     * Constructeur avec code seulement
     * @param code code du département
     */
    public Departement(String code) {
        this.code = code;
    }

    /**
     * Constructeur complet
     * @param code code du département
     * @param nom nom du département
     */
    public Departement(String code, String nom) {
        this.code = code;
        this.nom = nom;
    }

    // ==================== GETTERS ET SETTERS ====================

    /**
     * Récupère l'ID du département
     * @return ID du département
     */
    public Long getId() {
        return id;
    }

    /**
     * Définit l'ID du département (généralement pas utilisé car auto-généré)
     * @param id nouveau ID
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
     * @param code nouveau code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Récupère le nom du département
     * @return nom du département (peut être null)
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du département
     * @param nom nouveau nom (peut être null)
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Récupère la liste des villes du département
     * @return liste des villes
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

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Ajoute une ville à ce département
     * Gère la relation bidirectionnelle
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
     * @return somme des habitants de toutes les villes
     */
    public Integer getPopulationTotale() {
        return villes.stream()
                .mapToInt(ville -> ville.getNbHabitants() != null ? ville.getNbHabitants() : 0)
                .sum();
    }

    /**
     * Récupère le nombre de villes dans le département
     * @return nombre de villes
     */
    public int getNombreVilles() {
        return villes.size();
    }

    // ==================== MÉTHODES STANDARD ====================

    /**
     * Représentation textuelle du département
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
