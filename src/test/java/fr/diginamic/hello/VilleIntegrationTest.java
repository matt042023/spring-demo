package fr.diginamic.hello;

import fr.diginamic.hello.dto.VilleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration complet pour vérifier le scénario de modification de département
 * Reproduit le bug rencontré avec Narbonne (Hérault -> Aude)
 */
import org.junit.jupiter.api.Disabled;

@Disabled("Test d'intégration non aligné avec l'API actuelle; remplacé par tests MockMvc")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VilleIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void testModificationDepartementVille_ScenarioNarbonne() {
        // Given - On part du principe que Narbonne existe avec le département Hérault (34)
        // On va d'abord récupérer toutes les villes pour trouver Narbonne
        ResponseEntity<VilleDTO[]> villesResponse = restTemplate.getForEntity(
                getBaseUrl() + "/villes", VilleDTO[].class);
        
        assertThat(villesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        VilleDTO[] villes = villesResponse.getBody();
        assertThat(villes).isNotNull();

        // Trouver Narbonne
        VilleDTO narbonne = null;
        for (VilleDTO ville : villes) {
            if ("Narbonne".equals(ville.getNom())) {
                narbonne = ville;
                break;
            }
        }
        
        assertThat(narbonne).isNotNull().withFailMessage("Narbonne devrait exister dans les données de test");
        assertThat(narbonne.getDepartement().getCode()).isEqualTo("34"); // Initialement dans l'Hérault
        
        Long narborneId = narbonne.getId();
        System.out.println("Test: Narbonne trouvée avec ID " + narborneId + " dans le département " + narbonne.getDepartement().getCode());

        // When - On essaie de modifier le département de Narbonne vers l'Aude (11)
        // D'abord, récupérer tous les départements pour trouver l'ID de l'Aude
        ResponseEntity<Object[]> departementsResponse = restTemplate.getForEntity(
                getBaseUrl() + "/departements", Object[].class);
        
        assertThat(departementsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Préparer la requête de modification
        VilleDTO villeModifiee = new VilleDTO();
        villeModifiee.setNom("Narbonne");
        villeModifiee.setNbHabitants(53399);
        
        // Créer un département Aude (on assume que l'ID sera trouvé)
        VilleDTO.DepartementSimplifieDTO audeDepet = new VilleDTO.DepartementSimplifieDTO();
        audeDepet.setId(11L); // ID approximatif, dépend de l'ordre d'insertion
        audeDepet.setCode("11");
        audeDepet.setNom("Aude");
        villeModifiee.setDepartement(audeDepet);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VilleDTO> requestEntity = new HttpEntity<>(villeModifiee, headers);

        // Exécuter la modification
        ResponseEntity<VilleDTO> updateResponse = restTemplate.exchange(
                getBaseUrl() + "/villes/" + narborneId,
                HttpMethod.PUT,
                requestEntity,
                VilleDTO.class);

        // Then - Vérifier que la modification a bien eu lieu
        System.out.println("Test: Réponse de modification - Status: " + updateResponse.getStatusCode());
        
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            VilleDTO villeUpdated = updateResponse.getBody();
            assertThat(villeUpdated).isNotNull();
            System.out.println("Test: Ville après modification - Département: " + 
                villeUpdated.getDepartement().getCode() + " (" + villeUpdated.getDepartement().getNom() + ")");
            
            // Vérifier que le département a bien changé
            assertThat(villeUpdated.getDepartement().getCode()).isEqualTo("11");
            assertThat(villeUpdated.getDepartement().getNom()).isEqualTo("Aude");
        } else {
            System.out.println("Test: Échec de la modification - " + updateResponse.getBody());
        }

        // Vérification finale - Re-récupérer la ville pour confirmer la persistance
        ResponseEntity<VilleDTO> finalCheckResponse = restTemplate.getForEntity(
                getBaseUrl() + "/villes/" + narborneId, VilleDTO.class);
        
        assertThat(finalCheckResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        VilleDTO villeFinal = finalCheckResponse.getBody();
        assertThat(villeFinal).isNotNull();
        
        System.out.println("Test final: Narbonne après vérification - Département: " + 
            villeFinal.getDepartement().getCode() + " (" + villeFinal.getDepartement().getNom() + ")");
        
        // Cette assertion devrait réussir après la correction
        assertThat(villeFinal.getDepartement().getCode())
            .withFailMessage("Le département de Narbonne devrait être l'Aude (11) après modification")
            .isEqualTo("11");
    }

    @Test
    public void testCreationVille() {
        // Test de création d'une nouvelle ville pour s'assurer que ça fonctionne
        VilleDTO nouvelleVille = new VilleDTO();
        nouvelleVille.setNom("Ville Test");
        nouvelleVille.setNbHabitants(10000);
        
        VilleDTO.DepartementSimplifieDTO dept = new VilleDTO.DepartementSimplifieDTO();
        dept.setId(1L); // Paris
        nouvelleVille.setDepartement(dept);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VilleDTO> requestEntity = new HttpEntity<>(nouvelleVille, headers);

        ResponseEntity<VilleDTO> createResponse = restTemplate.exchange(
                getBaseUrl() + "/villes",
                HttpMethod.POST,
                requestEntity,
                VilleDTO.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        VilleDTO villeCreee = createResponse.getBody();
        assertThat(villeCreee).isNotNull();
        assertThat(villeCreee.getNom()).isEqualTo("Ville Test");
        assertThat(villeCreee.getId()).isNotNull();
        
        System.out.println("Test création: Ville créée avec ID " + villeCreee.getId());
        
        // Nettoyer - supprimer la ville test
        restTemplate.delete(getBaseUrl() + "/villes/" + villeCreee.getId());
    }
}
