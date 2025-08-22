package fr.diginamic.hello;

import fr.diginamic.hello.controlers.VilleController;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.services.VilleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le VilleController
 * Vérifie que les endpoints CRUD fonctionnent correctement
 */
@WebMvcTest(VilleController.class)
public class VilleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VilleService villeService;

    @MockBean
    private VilleMapper villeMapper;

    @Test
    public void testGetAllVilles() throws Exception {
        // Given
        VilleDTO.DepartementSimplifieDTO dept = new VilleDTO.DepartementSimplifieDTO();
        dept.setId(1L);
        dept.setCode("34");
        dept.setNom("Hérault");

        VilleDTO ville1 = new VilleDTO();
        ville1.setId(1L);
        ville1.setNom("Montpellier");
        ville1.setNbHabitants(295542);
        ville1.setDepartement(dept);

        VilleDTO ville2 = new VilleDTO();
        ville2.setId(2L);
        ville2.setNom("Narbonne");
        ville2.setNbHabitants(53399);
        ville2.setDepartement(dept);

        List<VilleDTO> villes = Arrays.asList(ville1, ville2);
        when(villeService.findAll()).thenReturn(villes);

        // When & Then
        mockMvc.perform(get("/villes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Montpellier"))
                .andExpect(jsonPath("$[0].nbHabitants").value(295542))
                .andExpect(jsonPath("$[0].departement.code").value("34"))
                .andExpect(jsonPath("$[1].nom").value("Narbonne"))
                .andExpect(jsonPath("$[1].nbHabitants").value(53399));

        verify(villeService, times(1)).findAll();
    }

    @Test
    public void testGetVilleById() throws Exception {
        // Given
        VilleDTO.DepartementSimplifieDTO dept = new VilleDTO.DepartementSimplifieDTO();
        dept.setId(1L);
        dept.setCode("34");
        dept.setNom("Hérault");

        VilleDTO ville = new VilleDTO();
        ville.setId(1L);
        ville.setNom("Narbonne");
        ville.setNbHabitants(53399);
        ville.setDepartement(dept);

        when(villeService.findById(1L)).thenReturn(ville);

        // When & Then
        mockMvc.perform(get("/villes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nom").value("Narbonne"))
                .andExpect(jsonPath("$.nbHabitants").value(53399))
                .andExpect(jsonPath("$.departement.code").value("34"));

        verify(villeService, times(1)).findById(1L);
    }

    @Test
    public void testCreateVille() throws Exception {
        // Given
        VilleDTO.DepartementSimplifieDTO dept = new VilleDTO.DepartementSimplifieDTO();
        dept.setId(2L);
        dept.setCode("11");
        dept.setNom("Aude");

        VilleDTO nouvelleVille = new VilleDTO();
        nouvelleVille.setId(3L);
        nouvelleVille.setNom("Carcassonne");
        nouvelleVille.setNbHabitants(47068);
        nouvelleVille.setDepartement(dept);

        when(villeService.save(any(VilleDTO.class))).thenReturn(nouvelleVille);

        String jsonRequest = """
            {
                "nom": "Carcassonne",
                "nbHabitants": 47068,
                "departement": {
                    "id": 2
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/villes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nom").value("Carcassonne"))
                .andExpect(jsonPath("$.nbHabitants").value(47068))
                .andExpect(jsonPath("$.departement.code").value("11"));

        verify(villeService, times(1)).save(any(VilleDTO.class));
    }

    @Test
    public void testUpdateVille() throws Exception {
        // Given - Test de modification de département de Narbonne
        VilleDTO.DepartementSimplifieDTO ancienDept = new VilleDTO.DepartementSimplifieDTO();
        ancienDept.setId(1L);
        ancienDept.setCode("34");
        ancienDept.setNom("Hérault");

        VilleDTO.DepartementSimplifieDTO nouveauDept = new VilleDTO.DepartementSimplifieDTO();
        nouveauDept.setId(2L);
        nouveauDept.setCode("11");
        nouveauDept.setNom("Aude");

        VilleDTO villeModifiee = new VilleDTO();
        villeModifiee.setId(1L);
        villeModifiee.setNom("Narbonne");
        villeModifiee.setNbHabitants(53399);
        villeModifiee.setDepartement(nouveauDept); // Changement de département

        when(villeService.findById(1L)).thenReturn(villeModifiee);
        when(villeService.update(eq(1L), any(VilleDTO.class))).thenReturn(villeModifiee);

        String jsonRequest = """
            {
                "nom": "Narbonne",
                "nbHabitants": 53399,
                "departement": {
                    "id": 2
                }
            }
            """;

        // When & Then
        mockMvc.perform(put("/villes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nom").value("Narbonne"))
                .andExpect(jsonPath("$.nbHabitants").value(53399))
                .andExpect(jsonPath("$.departement.code").value("11")) // Vérification du changement
                .andExpect(jsonPath("$.departement.nom").value("Aude"));

        verify(villeService, times(1)).update(eq(1L), any(VilleDTO.class));
    }

    @Test
    public void testDeleteVille() throws Exception {
        // Given
        doNothing().when(villeService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/villes/1"))
                .andExpect(status().isNoContent());

        verify(villeService, times(1)).delete(1L);
    }

    @Test
    public void testGetVilleNotFound() throws Exception {
        // Given
        when(villeService.findById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/villes/999"))
                .andExpect(status().isNotFound());

        verify(villeService, times(1)).findById(999L);
    }
}