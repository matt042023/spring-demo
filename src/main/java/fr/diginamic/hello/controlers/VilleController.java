package fr.diginamic.hello.controlers;

import fr.diginamic.hello.models.Ville;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contrôleur REST pour gérer les villes - CRUD complet avec validation
 *
 * @RestController = @Controller + @ResponseBody
 * Toutes les méthodes renvoient directement des données JSON
 */
@RestController
@RequestMapping("/villes") // URL de base pour toutes les méthodes
public class VilleController {

    /**
     * Liste partagée de villes (simulation base de données)
     * Dans un vrai projet, cela serait géré par JPA/Hibernate
     */
    private List<Ville> villes;

    /**
     * Générateur d'ID automatique thread-safe
     * AtomicInteger garantit l'unicité même en cas d'accès concurrent
     */
    private AtomicInteger idGenerator = new AtomicInteger(1);

    /**
     * Constructeur - initialise la liste avec des données par défaut
     */
    public VilleController() {
        this.villes = new ArrayList<>();

        // Ajout de villes avec des IDs générés automatiquement
        villes.add(new Ville(generateId(), "Paris", 2161000));
        villes.add(new Ville(generateId(), "Marseille", 861635));
        villes.add(new Ville(generateId(), "Lyon", 513275));
        villes.add(new Ville(generateId(), "Toulouse", 471941));
        villes.add(new Ville(generateId(), "Nice", 342637));
    }

    /**
     * Génère un nouvel ID unique de manière thread-safe
     * @return nouvel ID unique
     */
    private int generateId() {
        return idGenerator.getAndIncrement();
    }

    // ========== READ OPERATIONS ==========

    /**
     * GET /villes - Récupère toutes les villes
     * @return liste complète des villes
     */
    @GetMapping
    public List<Ville> getAllVilles() {
        return villes;
    }

    /**
     * GET /villes/{id} - Récupère une ville par son ID
     * @param id identifiant de la ville recherchée
     * @return ResponseEntity avec la ville ou erreur 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ville> getVilleById(@PathVariable int id) {
        // Recherche de la ville par ID avec programmation fonctionnelle
        return villes.stream()
                .filter(ville -> ville.getId() == id)
                .findFirst()
                .map(ResponseEntity::ok) // 200 OK avec la ville
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    // ========== CREATE OPERATION ==========

    /**
     * POST /villes - Crée une nouvelle ville avec validation automatique
     * @param nouvelleVille ville à créer (JSON converti automatiquement)
     * @param bindingResult résultat de la validation
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @PostMapping
    public ResponseEntity<String> createVille(@Valid @RequestBody Ville nouvelleVille,
                                              BindingResult bindingResult) {

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            StringBuilder erreurs = new StringBuilder("Erreurs de validation : ");
            bindingResult.getAllErrors().forEach(error ->
                    erreurs.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(erreurs.toString());
        }

        // Vérification nom unique (business logic)
        boolean nomExiste = villes.stream()
                .anyMatch(ville -> ville.getNom().equalsIgnoreCase(nouvelleVille.getNom()));

        if (nomExiste) {
            return ResponseEntity.badRequest()
                    .body("Une ville avec ce nom existe déjà");
        }

        // Génération automatique de l'ID (l'utilisateur ne doit PAS le fournir)
        nouvelleVille.setId(generateId());

        // Ajout de la ville
        villes.add(nouvelleVille);

        return ResponseEntity.ok("Ville créée avec succès (ID généré automatiquement: " + nouvelleVille.getId() + ")");
    }

    // ========== UPDATE OPERATION ==========

    /**
     * PUT /villes/{id} - Met à jour une ville existante avec validation
     * @param id identifiant de la ville à modifier
     * @param villeModifiee nouvelles données de la ville
     * @param bindingResult résultat de la validation
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateVille(@PathVariable int id,
                                              @Valid @RequestBody Ville villeModifiee,
                                              BindingResult bindingResult) {

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            StringBuilder erreurs = new StringBuilder("Erreurs de validation : ");
            bindingResult.getAllErrors().forEach(error ->
                    erreurs.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(erreurs.toString());
        }

        // Recherche de la ville à modifier
        for (Ville ville : villes) {
            if (ville.getId() == id) {

                // Vérification que le nouveau nom n'est pas déjà pris (sauf si même ville)
                boolean nomPris = villes.stream()
                        .anyMatch(autreVille -> autreVille.getId() != id &&
                                autreVille.getNom().equalsIgnoreCase(villeModifiee.getNom()));

                if (nomPris) {
                    return ResponseEntity.badRequest()
                            .body("Le nom de ville est déjà utilisé");
                }

                // Mise à jour des données (on garde le même ID - pas de modification d'ID en REST)
                ville.setNom(villeModifiee.getNom());
                ville.setNbHabitants(villeModifiee.getNbHabitants());

                return ResponseEntity.ok("Ville modifiée avec succès");
            }
        }

        // Ville non trouvée
        return ResponseEntity.notFound().build(); // 404 Not Found
    }

    // ========== DELETE OPERATION ==========

    /**
     * DELETE /villes/{id} - Supprime une ville
     * @param id identifiant de la ville à supprimer
     * @return ResponseEntity avec message de succès ou d'erreur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVille(@PathVariable int id) {

        // Recherche et suppression avec programmation fonctionnelle
        boolean removed = villes.removeIf(ville -> ville.getId() == id);

        if (removed) {
            return ResponseEntity.ok("Ville supprimée avec succès");
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}