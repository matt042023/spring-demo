package fr.diginamic.hello.mappers;

import fr.diginamic.hello.dto.DepartementDTO;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour la conversion entre les entités Departement et les DTO DepartementDTO
 * 
 * Cette classe utilitaire gère les conversions bidirectionnelles :
 * - Entité -> DTO (pour les réponses API)
 * - DTO -> Entité (pour les requêtes API)
 * - Conversion en lot pour les listes
 * - Gestion des références circulaires avec les villes
 * - Calcul des statistiques (population totale, nombre de villes)
 * 
 * @Component permet l'injection de dépendance Spring
 * 
 * @author Votre nom
 * @version 1.0
 * @since 1.0
 */
@Component
public class DepartementMapper {

    /**
     * Injection du mapper Ville pour la conversion des villes associées
     */
    @Autowired
    private VilleMapper villeMapper;

    /**
     * Convertit une entité Departement en DTO DepartementDTO
     * 
     * @param departement entité Departement à convertir
     * @return DepartementDTO correspondant ou null si l'entité est null
     */
    public DepartementDTO toDTO(Departement departement) {
        if (departement == null) {
            return null;
        }

        DepartementDTO dto = new DepartementDTO();
        dto.setId(departement.getId());
        dto.setCode(departement.getCode());
        dto.setNom(departement.getNom());

        // Conversion des villes associées en DTO simplifiés
        if (departement.getVilles() != null) {
            List<DepartementDTO.VilleSimplifieDTO> villesDTO = departement.getVilles().stream()
                    .map(ville -> new DepartementDTO.VilleSimplifieDTO(
                            ville.getId(),
                            ville.getNom(),
                            ville.getNbHabitants()
                    ))
                    .collect(Collectors.toList());
            
            dto.setVilles(villesDTO);
        } else {
            dto.setVilles(List.of());
        }

        // Calcul automatique des statistiques
        dto.calculerStatistiques();

        return dto;
    }

    /**
     * Convertit un DTO DepartementDTO en entité Departement
     * 
     * @param departementDTO DTO DepartementDTO à convertir
     * @return entité Departement correspondante ou null si le DTO est null
     */
    public Departement toEntity(DepartementDTO departementDTO) {
        if (departementDTO == null) {
            return null;
        }

        Departement departement = new Departement();
        // L'ID n'est pas défini lors de la conversion - il sera généré automatiquement par JPA
        departement.setCode(departementDTO.getCode());
        departement.setNom(departementDTO.getNom());

        // Note: La liste des villes n'est généralement pas convertie lors de la création/modification
        // Les relations sont gérées séparément par les services

        return departement;
    }

    /**
     * Convertit une liste d'entités Departement en liste de DTO DepartementDTO
     * 
     * @param departements liste des entités Departement à convertir
     * @return liste des DTO DepartementDTO correspondants ou liste vide si null
     */
    public List<DepartementDTO> toDTOList(List<Departement> departements) {
        if (departements == null) {
            return List.of(); // Retourne une liste vide au lieu de null
        }

        return departements.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de DTO DepartementDTO en liste d'entités Departement
     * 
     * @param departementDTOs liste des DTO DepartementDTO à convertir
     * @return liste des entités Departement correspondantes ou liste vide si null
     */
    public List<Departement> toEntityList(List<DepartementDTO> departementDTOs) {
        if (departementDTOs == null) {
            return List.of(); // Retourne une liste vide au lieu de null
        }

        return departementDTOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une entité Departement existante avec les données d'un DTO
     * Utile pour les opérations de mise à jour (PUT/PATCH)
     * 
     * @param departementExistant entité Departement existante à mettre à jour
     * @param departementDTO DTO contenant les nouvelles données
     * @return entité Departement mise à jour
     */
    public Departement updateEntityFromDTO(Departement departementExistant, DepartementDTO departementDTO) {
        if (departementExistant == null || departementDTO == null) {
            return departementExistant;
        }

        // Mise à jour des champs modifiables
        if (departementDTO.getCode() != null) {
            departementExistant.setCode(departementDTO.getCode());
        }
        
        if (departementDTO.getNom() != null) {
            departementExistant.setNom(departementDTO.getNom());
        }

        // Note: Les villes ne sont pas mises à jour via ce mapper
        // Les relations sont gérées par les services métier

        return departementExistant;
    }

    /**
     * Crée un DTO DepartementDTO simplifié (sans villes) à partir d'une entité Departement
     * Utile pour éviter les références circulaires dans certains contextes
     * 
     * @param departement entité Departement à convertir
     * @return DepartementDTO simplifié ou null si l'entité est null
     */
    public DepartementDTO toSimplifiedDTO(Departement departement) {
        if (departement == null) {
            return null;
        }

        DepartementDTO dto = new DepartementDTO();
        dto.setId(departement.getId());
        dto.setCode(departement.getCode());
        dto.setNom(departement.getNom());
        dto.setVilles(List.of()); // Liste vide pour éviter les références circulaires
        dto.setPopulationTotale(0L);
        dto.setNombreVilles(0);

        return dto;
    }

    /**
     * Convertit une entité Departement en DTO DepartementSimplifieDTO pour l'inclusion dans VilleDTO
     * 
     * @param departement entité Departement à convertir
     * @return DTO DepartementSimplifieDTO ou null si l'entité est null
     */
    public fr.diginamic.hello.dto.VilleDTO.DepartementSimplifieDTO toDepartementSimplifieDTO(Departement departement) {
        if (departement == null) {
            return null;
        }

        return new fr.diginamic.hello.dto.VilleDTO.DepartementSimplifieDTO(
                departement.getId(),
                departement.getCode(),
                departement.getNom()
        );
    }

    /**
     * Crée un DTO DepartementDTO avec calcul de statistiques personnalisées
     * Permet de spécifier manuellement les statistiques si elles sont calculées ailleurs
     * 
     * @param departement entité Departement
     * @param populationTotale population totale précalculée
     * @param nombreVilles nombre de villes précalculé
     * @return DepartementDTO avec statistiques personnalisées
     */
    public DepartementDTO toDTOWithStats(Departement departement, Long populationTotale, Integer nombreVilles) {
        if (departement == null) {
            return null;
        }

        DepartementDTO dto = toDTO(departement);
        if (dto != null) {
            dto.setPopulationTotale(populationTotale != null ? populationTotale : 0L);
            dto.setNombreVilles(nombreVilles != null ? nombreVilles : 0);
        }

        return dto;
    }

    /**
     * Crée un DTO DepartementDTO avec une liste spécifique de villes
     * Utile pour les cas où on veut limiter les villes affichées (top N, filtrage, etc.)
     * 
     * @param departement entité Departement
     * @param villes liste spécifique de villes à inclure
     * @return DepartementDTO avec la liste de villes spécifiée
     */
    public DepartementDTO toDTOWithSpecificVilles(Departement departement, List<Ville> villes) {
        if (departement == null) {
            return null;
        }

        DepartementDTO dto = new DepartementDTO();
        dto.setId(departement.getId());
        dto.setCode(departement.getCode());
        dto.setNom(departement.getNom());

        // Conversion de la liste spécifique de villes
        if (villes != null) {
            List<DepartementDTO.VilleSimplifieDTO> villesDTO = villes.stream()
                    .map(ville -> new DepartementDTO.VilleSimplifieDTO(
                            ville.getId(),
                            ville.getNom(),
                            ville.getNbHabitants()
                    ))
                    .collect(Collectors.toList());
            
            dto.setVilles(villesDTO);
        } else {
            dto.setVilles(List.of());
        }

        // Calcul des statistiques basé sur la liste fournie
        dto.calculerStatistiques();

        return dto;
    }
}