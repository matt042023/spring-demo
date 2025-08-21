package fr.diginamic.hello.services;

import fr.diginamic.hello.dao.DepartementDao;
import fr.diginamic.hello.dao.VilleDao;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant la logique métier des villes
 * 
 * Cette classe implémente la couche service de l'architecture Spring MVC :
 * - Sépare la logique métier du contrôleur REST
 * - Gère les transactions avec @Transactional
 * - Effectue les validations métier avant persistance
 * - Fait le lien entre le contrôleur et la couche DAO
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Service // Annotation Spring pour marquer cette classe comme un service
@Transactional // Toutes les méthodes sont transactionnelles par défaut
public class VilleService {


    /**
     * Injection de dépendance du DAO Ville pour l'accès aux données
     * Spring injecte automatiquement une instance de VilleDao
     */
    @Autowired
    private VilleDao villeDao;

    /**
     * Injection de dépendance du DAO Departement pour les vérifications
     * Spring injecte automatiquement une instance de DepartementDao
     */
    @Autowired
    private DepartementDao departementDao;


    /**
     * Récupère toutes les villes de la base de données
     * 
     * @return List<Ville> liste complète des villes
     */
    @Transactional(readOnly = true) // Optimisation pour les lectures seules
    public List<Ville> extractVilles() {
        // Délégation directe au DAO pour récupérer toutes les villes
        return villeDao.findAll();
    }


    /**
     * Récupère une ville par son identifiant
     * 
     * @param idVille identifiant unique de la ville
     * @return Ville ville trouvée ou null si inexistante
     */
    @Transactional(readOnly = true)
    public Ville extractVille(Long idVille) {
        // Recherche par ID avec Optional pour gérer l'absence
        Optional<Ville> ville = villeDao.findById(idVille);
        return ville.orElse(null); // Retourne null si non trouvée
    }


    /**
     * Récupère une ville par son nom (insensible à la casse)
     * 
     * @param nom nom de la ville à rechercher
     * @return Ville ville trouvée ou null si inexistante
     */
    @Transactional(readOnly = true)
    public Ville extractVille(String nom) {
        // Recherche par nom avec ignorant la casse
        Optional<Ville> ville = villeDao.findByNomIgnoreCase(nom);
        return ville.orElse(null); // Retourne null si non trouvée
    }


    public List<Ville> insertVille(Ville ville) {
        // Vérification métier : nom unique
        if (villeDao.existsByNomIgnoreCase(ville.getNom())) {
            throw new IllegalArgumentException("Une ville avec ce nom existe déjà");
        }


        villeDao.save(ville);

        return villeDao.findAll();
    }


    public List<Ville> modifierVille(Long idVille, Ville villeModifiee) {
        // Vérification que la ville existe
        Optional<Ville> villeExistante = villeDao.findById(idVille);
        if (villeExistante.isEmpty()) {
            throw new IllegalArgumentException("Ville non trouvée avec l'ID: " + idVille);
        }

        Ville ville = villeExistante.get();

        // Vérification que le nouveau nom n'est pas déjà pris (sauf si même ville)
        if (!ville.getNom().equalsIgnoreCase(villeModifiee.getNom()) &&
                villeDao.existsByNomIgnoreCase(villeModifiee.getNom())) {
            throw new IllegalArgumentException("Le nom de ville est déjà utilisé");
        }

        // Mise à jour des données
        ville.setNom(villeModifiee.getNom());
        ville.setNbHabitants(villeModifiee.getNbHabitants());


        villeDao.save(ville);


        return villeDao.findAll();
    }


    public List<Ville> supprimerVille(Long idVille) {
        // Vérification que la ville existe
        if (!villeDao.existsById(idVille)) {
            throw new IllegalArgumentException("Ville non trouvée avec l'ID: " + idVille);
        }

        // Suppression en base
        villeDao.deleteById(idVille);

        // Retourne la liste mise à jour
        return villeDao.findAll();
    }


    @Transactional(readOnly = true)
    public List<Ville> findVillesParPopulation(Integer min, Integer max) {
        return villeDao.findByNbHabitantsBetween(min, max);
    }


    @Transactional(readOnly = true)
    public List<Ville> findTopVilles(int limit) {
        return villeDao.findTopVillesByNbHabitants(limit);
    }
}