package fr.diginamic.hello.controlers;

import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.services.VilleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private VilleService villeService;

    /**
     * Injection de dépendance du mapper pour la conversion entité/DTO
     * Spring injecte automatiquement une instance de VilleMapper
     */
    @Autowired
    private VilleMapper villeMapper;


    /**
     * GET /villes - Récupère la liste de toutes les villes avec leurs départements
     * @return List<VilleDTO> Liste complète des villes avec départements associés
     */
    @GetMapping
    public List<VilleDTO> getAllVilles() {
        // Délégation au service pour récupérer toutes les villes
        List<Ville> villes = villeService.extractVilles();
        // Conversion des entités en DTO avec départements inclus
        return villeMapper.toDTOList(villes);
    }

    /**
     * GET /villes/{id} - Récupère une ville par son ID avec son département
     * @param id identifiant unique de la ville
     * @return ResponseEntity<VilleDTO> ville trouvée avec département ou 404 si non trouvée
     */
    @GetMapping("/{id}")
    public ResponseEntity<VilleDTO> getVilleById(@PathVariable Long id) {
        // Recherche de la ville par ID via le service
        Ville ville = villeService.extractVille(id);

        // Retour de la ville si trouvée, sinon erreur 404
        if (ville != null) {
            VilleDTO villeDTO = villeMapper.toDTO(ville);
            return ResponseEntity.ok(villeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /villes/nom/{nom} - Récupère une ville par son nom avec son département
     * @param nom nom de la ville à rechercher (insensible à la casse)
     * @return ResponseEntity<VilleDTO> ville trouvée avec département ou 404 si non trouvée
     */
    @GetMapping("/nom/{nom}")
    public ResponseEntity<VilleDTO> getVilleByNom(@PathVariable String nom) {
        // Recherche de la ville par nom via le service
        Ville ville = villeService.extractVille(nom);

        // Retour de la ville si trouvée, sinon erreur 404
        if (ville != null) {
            VilleDTO villeDTO = villeMapper.toDTO(ville);
            return ResponseEntity.ok(villeDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== CREATE OPERATION ==========

    /**
     * POST /villes - Crée une nouvelle ville avec validation (département obligatoire)
     * @param nouvelleVilleDTO données de la nouvelle ville à créer avec département
     * @param bindingResult résultat de la validation des données
     * @return ResponseEntity<VilleDTO> ville créée avec département ou erreur
     */
    @PostMapping
    public ResponseEntity<?> createVille(@Valid @RequestBody VilleDTO nouvelleVilleDTO,
                                        BindingResult bindingResult) {
        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            StringBuilder erreurs = new StringBuilder("Erreur de validation : ");
            bindingResult.getAllErrors().forEach(error ->
                    erreurs.append(error.getDefaultMessage()).append(";")
            );
            return ResponseEntity.badRequest().body(erreurs.toString());
        }

        try {
            // Conversion DTO -> Entité
            Ville nouvelleVille = villeMapper.toEntity(nouvelleVilleDTO);
            // Délégation au service pour l'insertion avec vérifications métier
            List<Ville> villesMisesAjour = villeService.insertVille(nouvelleVille);
            
            // Retour de la ville créée en DTO
            Ville villeCree = villesMisesAjour.stream()
                    .filter(v -> v.getNom().equals(nouvelleVille.getNom()))
                    .findFirst()
                    .orElse(nouvelleVille);
            
            VilleDTO villeCreeDTO = villeMapper.toDTO(villeCree);
            return ResponseEntity.ok(villeCreeDTO);
        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (nom déjà existant, département manquant, etc.)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== UPDATE OPERATION ==========

    /**
     * PUT /villes/{id} - Met à jour une ville existante avec validation
     * @param id identifiant de la ville à modifier
     * @param villeModifieeDTO nouvelles données de la ville avec département
     * @param bindingResult résultat de la validation
     * @return ResponseEntity<VilleDTO> ville mise à jour avec département ou erreur
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVille(@PathVariable Long id,
                                        @Valid @RequestBody VilleDTO villeModifieeDTO,
                                        BindingResult bindingResult) {

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            StringBuilder erreurs = new StringBuilder("Erreurs de validation : ");
            bindingResult.getAllErrors().forEach(error ->
                    erreurs.append(error.getDefaultMessage()).append("; ")
            );
            return ResponseEntity.badRequest().body(erreurs.toString());
        }

        try {
            // Conversion DTO -> Entité
            Ville villeModifiee = villeMapper.toEntity(villeModifieeDTO);
            // Délégation au service pour la logique métier de modification
            List<Ville> villesMisesAJour = villeService.modifierVille(id, villeModifiee);
            
            // Retour de la ville modifiée en DTO
            Ville villeModifieeResult = villesMisesAJour.stream()
                    .filter(v -> v.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (villeModifieeResult != null) {
                VilleDTO villeModifieeResultDTO = villeMapper.toDTO(villeModifieeResult);
                return ResponseEntity.ok(villeModifieeResultDTO);
            } else {
                return ResponseEntity.ok("Ville mise à jour avec succès");
            }

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (ville inexistante, nom déjà pris, etc.)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ========== DELETE OPERATION ==========

    /**
     * DELETE /villes/{id} - Supprime une ville
     * @param id identifiant de la ville à supprimer
     * @return ResponseEntity<String> message de succès ou d'erreur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVille(@PathVariable Long id) {

        try {
            // Délégation au service pour la logique métier de suppression
            List<Ville> villesMisesAJour = villeService.supprimerVille(id);
            return ResponseEntity.ok("Ville supprimée avec succès");

        } catch (IllegalArgumentException e) {
            // Gestion des erreurs métier (ville inexistante)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}