package fr.diginamic.hello.mappers;

import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour la conversion entre les entités Ville et les DTO VilleDTO
 * 
 * Cette classe utilitaire gère les conversions bidirectionnelles :
 * - Entité -> DTO (pour les réponses API)
 * - DTO -> Entité (pour les requêtes API)
 * - Conversion en lot pour les listes
 * - Gestion des références circulaires avec les départements
 * 
 * @Component permet l'injection de dépendance Spring
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Component
public class VilleMapper {

    /**
     * Convertit une entité Ville en DTO VilleDTO
     * 
     * @param ville entité Ville à convertir
     * @return VilleDTO correspondant ou null si l'entité est null
     */
    public VilleDTO toDTO(Ville ville) {
        if (ville == null) {
            return null;
        }

        VilleDTO dto = new VilleDTO();
        dto.setId(ville.getId());
        dto.setNom(ville.getNom());
        dto.setNbHabitants(ville.getNbHabitants());

        // Conversion du département associé en DTO simplifié
        if (ville.getDepartement() != null) {
            VilleDTO.DepartementSimplifieDTO departementDTO = new VilleDTO.DepartementSimplifieDTO();
            departementDTO.setId(ville.getDepartement().getId());
            departementDTO.setCode(ville.getDepartement().getCode());
            departementDTO.setNom(ville.getDepartement().getNom());
            dto.setDepartement(departementDTO);
        }

        return dto;
    }

    /**
     * Convertit un DTO VilleDTO en entité Ville
     * 
     * @param villeDTO DTO VilleDTO à convertir
     * @return entité Ville correspondante ou null si le DTO est null
     */
    public Ville toEntity(VilleDTO villeDTO) {
        if (villeDTO == null) {
            return null;
        }

        Ville ville = new Ville();
        // L'ID n'est pas défini lors de la conversion - il sera généré automatiquement par JPA
        ville.setNom(villeDTO.getNom());
        ville.setNbHabitants(villeDTO.getNbHabitants());

        // Conversion du département DTO en entité
        if (villeDTO.getDepartement() != null) {
            Departement departement = new Departement();
            departement.setId(villeDTO.getDepartement().getId());
            departement.setCode(villeDTO.getDepartement().getCode());
            departement.setNom(villeDTO.getDepartement().getNom());
            ville.setDepartement(departement);
        }

        return ville;
    }

    /**
     * Convertit une liste d'entités Ville en liste de DTO VilleDTO
     * 
     * @param villes liste des entités Ville à convertir
     * @return liste des DTO VilleDTO correspondants ou liste vide si null
     */
    public List<VilleDTO> toDTOList(List<Ville> villes) {
        if (villes == null) {
            return List.of(); // Retourne une liste vide au lieu de null
        }

        return villes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de DTO VilleDTO en liste d'entités Ville
     * 
     * @param villeDTOs liste des DTO VilleDTO à convertir
     * @return liste des entités Ville correspondantes ou liste vide si null
     */
    public List<Ville> toEntityList(List<VilleDTO> villeDTOs) {
        if (villeDTOs == null) {
            return List.of(); // Retourne une liste vide au lieu de null
        }

        return villeDTOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une entité Ville existante avec les données d'un DTO
     * Utile pour les opérations de mise à jour (PUT/PATCH)
     * 
     * @param villeExistante entité Ville existante à mettre à jour
     * @param villeDTO DTO contenant les nouvelles données
     * @return entité Ville mise à jour
     */
    public Ville updateEntityFromDTO(Ville villeExistante, VilleDTO villeDTO) {
        if (villeExistante == null || villeDTO == null) {
            return villeExistante;
        }

        // Mise à jour des champs modifiables
        if (villeDTO.getNom() != null) {
            villeExistante.setNom(villeDTO.getNom());
        }
        
        if (villeDTO.getNbHabitants() != null) {
            villeExistante.setNbHabitants(villeDTO.getNbHabitants());
        }

        // Mise à jour du département si fourni
        if (villeDTO.getDepartement() != null && villeDTO.getDepartement().getId() != null) {
            // Le département sera résolu par le service via son ID
            // On ne peut pas créer un objet Departement partiel ici
            // Cette logique doit être gérée au niveau du service
        }

        return villeExistante;
    }

    /**
     * Crée un DTO VilleDTO simplifié (sans département) à partir d'une entité Ville
     * Utile pour éviter les références circulaires dans certains contextes
     * 
     * @param ville entité Ville à convertir
     * @return VilleDTO simplifié ou null si l'entité est null
     */
    public VilleDTO toSimplifiedDTO(Ville ville) {
        if (ville == null) {
            return null;
        }

        VilleDTO dto = new VilleDTO();
        dto.setId(ville.getId());
        dto.setNom(ville.getNom());
        dto.setNbHabitants(ville.getNbHabitants());
        // Pas de département pour éviter les références circulaires

        return dto;
    }

    /**
     * Convertit une entité Ville en DTO VilleSimplifieDTO pour l'inclusion dans DepartementDTO
     * 
     * @param ville entité Ville à convertir
     * @return DTO VilleSimplifieDTO ou null si l'entité est null
     */
    public fr.diginamic.hello.dto.DepartementDTO.VilleSimplifieDTO toVilleSimplifieDTO(Ville ville) {
        if (ville == null) {
            return null;
        }

        return new fr.diginamic.hello.dto.DepartementDTO.VilleSimplifieDTO(
                ville.getId(),
                ville.getNom(),
                ville.getNbHabitants()
        );
    }

    /**
     * Convertit une liste d'entités Ville en liste de DTO VilleSimplifieDTO
     * 
     * @param villes liste des entités Ville à convertir
     * @return liste des DTO VilleSimplifieDTO correspondants
     */
    public List<fr.diginamic.hello.dto.DepartementDTO.VilleSimplifieDTO> toVilleSimplifieDTO(List<Ville> villes) {
        if (villes == null) {
            return List.of();
        }

        return villes.stream()
                .map(this::toVilleSimplifieDTO)
                .collect(Collectors.toList());
    }
}