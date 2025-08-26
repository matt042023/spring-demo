package fr.diginamic.hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.diginamic.hello.controlers.VilleController;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.repositories.VilleRepositoryHelper;
import fr.diginamic.hello.services.VilleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VilleController.class)
class VilleControllerRoutesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VilleService villeService;

    @MockBean
    private VilleMapper villeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Ville sampleVille;
    private VilleDTO sampleVilleDTO;
    private Departement sampleDept;
    private VilleDTO.DepartementSimplifieDTO sampleDeptDTO;

    @BeforeEach
    void setup() {
        sampleDept = new Departement();
        sampleDept.setId(1L);
        sampleDept.setCode("34");
        sampleDept.setNom("Hérault");

        sampleVille = new Ville();
        sampleVille.setId(10L);
        sampleVille.setNom("Montpellier");
        sampleVille.setNbHabitants(295542);
        sampleVille.setDepartement(sampleDept);

        sampleDeptDTO = new VilleDTO.DepartementSimplifieDTO(1L, "34", "Hérault");
        sampleVilleDTO = new VilleDTO(10L, "Montpellier", 295542, sampleDeptDTO);
    }

    @Test
    void getAllVilles_ok() throws Exception {
        when(villeService.findAll()).thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Montpellier"));
    }

    @Test
    void getAllVillesPaginated_ok() throws Exception {
        Page<Ville> page = new PageImpl<>(List.of(sampleVille));
        when(villeService.findAllPaginated(eq(0), eq(2))).thenReturn(page);
        when(villeMapper.toDTO(any(Ville.class))).thenReturn(sampleVilleDTO);

        mockMvc.perform(get("/villes/paginated").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nom").value("Montpellier"));
    }

    @Test
    void getVilleById_found() throws Exception {
        when(villeService.findById(10L)).thenReturn(Optional.of(sampleVille));
        when(villeMapper.toDTO(sampleVille)).thenReturn(sampleVilleDTO);

        mockMvc.perform(get("/villes/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getVilleById_notFound() throws Exception {
        when(villeService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/villes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVille_ok() throws Exception {
        when(villeMapper.toEntity(any(VilleDTO.class))).thenAnswer(inv -> {
            VilleDTO dto = inv.getArgument(0);
            Ville v = new Ville();
            v.setNom(dto.getNom());
            v.setNbHabitants(dto.getNbHabitants());
            v.setDepartement(sampleDept);
            return v;
        });
        when(villeService.save(any(Ville.class))).thenReturn(sampleVille);
        when(villeMapper.toDTO(any(Ville.class))).thenReturn(sampleVilleDTO);

        String body = objectMapper.writeValueAsString(sampleVilleDTO);

        mockMvc.perform(post("/villes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Montpellier"));
    }

    @Test
    void updateVille_ok() throws Exception {
        when(villeService.findById(10L)).thenReturn(Optional.of(sampleVille));
        when(villeMapper.toEntity(any(VilleDTO.class))).thenReturn(sampleVille);
        when(villeService.save(any(Ville.class))).thenReturn(sampleVille);
        when(villeMapper.toDTO(any(Ville.class))).thenReturn(sampleVilleDTO);

        String body = objectMapper.writeValueAsString(sampleVilleDTO);

        mockMvc.perform(put("/villes/10").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deleteVille_noContent() throws Exception {
        mockMvc.perform(delete("/villes/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findByNom_exact() throws Exception {
        when(villeService.findByNom("Montpellier")).thenReturn(Optional.of(sampleVille));
        when(villeMapper.toDTO(sampleVille)).thenReturn(sampleVilleDTO);

        mockMvc.perform(get("/villes/search/nom").param("nom", "Montpellier"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Montpellier"));
    }

    @Test
    void findByNomContaining_ok() throws Exception {
        when(villeService.findByNomContaining("Mont"))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/search/nom-contient").param("nom", "Mont"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Montpellier"));
    }

    @Test
    void findByNomStartingWith_ok() throws Exception {
        when(villeService.findByNomStartingWith("Mon"))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/search/nom-commence").param("prefix", "Mon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Montpellier"));
    }

    @Test
    void populationMin_ok() throws Exception {
        when(villeService.findByPopulationGreaterThan(100000))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/search/population-min").param("min", "100000"))
                .andExpect(status().isOk());
    }

    @Test
    void populationPlage_ok() throws Exception {
        when(villeService.findByPopulationBetween(50000, 300000))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/search/population-plage")
                        .param("min", "50000")
                        .param("max", "300000"))
                .andExpect(status().isOk());
    }

    @Test
    void departementMin_ok() throws Exception {
        when(villeService.findByDepartementAndMinPopulation("34", 10000))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/departement/34").param("min", "10000"))
                .andExpect(status().isOk());
    }

    @Test
    void departementSansMin_ok() throws Exception {
        when(villeService.exportVillesByDepartement("34"))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/departement/34"))
                .andExpect(status().isOk());
    }

    @Test
    void departementPlage_ok() throws Exception {
        when(villeService.findByDepartementAndPopulationRange("34", 10000, 300000))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/departement/34/plage")
                        .param("min", "10000")
                        .param("max", "300000"))
                .andExpect(status().isOk());
    }

    @Test
    void departementTop_ok() throws Exception {
        when(villeService.findTopNVillesByDepartement("34", 5))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/departement/34/top").param("n", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void count_ok() throws Exception {
        when(villeService.count()).thenReturn(100L);

        mockMvc.perform(get("/villes/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void departementStats_ok_or_404() throws Exception {
        VilleRepositoryHelper.DepartementStats stats = new VilleRepositoryHelper.DepartementStats(sampleDept, 50L, 1000000L, sampleVille);
        when(villeService.getDepartementStats("34")).thenReturn(stats);

        mockMvc.perform(get("/villes/departement/34/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departement.code").value("34"));

        when(villeService.getDepartementStats("99")).thenReturn(null);
        mockMvc.perform(get("/villes/departement/99/stats"))
                .andExpect(status().isNotFound());
    }

    @Test
    void mostPopulated_ok_or_404() throws Exception {
        when(villeService.findMostPopulatedVilleInDepartement("34")).thenReturn(Optional.of(sampleVille));
        when(villeMapper.toDTO(sampleVille)).thenReturn(sampleVilleDTO);

        mockMvc.perform(get("/villes/departement/34/plus-peuplee"))
                .andExpect(status().isOk());

        when(villeService.findMostPopulatedVilleInDepartement("99")).thenReturn(Optional.empty());
        mockMvc.perform(get("/villes/departement/99/plus-peuplee"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePopulation_ok() throws Exception {
        when(villeService.updatePopulation(10L, 300000)).thenReturn(sampleVille);
        when(villeMapper.toDTO(sampleVille)).thenReturn(sampleVilleDTO);

        mockMvc.perform(put("/villes/10/population").param("nouveauNb", "300000"))
                .andExpect(status().isOk());
    }

    @Test
    void creationRapide_ok() throws Exception {
        when(villeService.createVille("Test", 1000, "34")).thenReturn(sampleVille);
        when(villeMapper.toDTO(sampleVille)).thenReturn(sampleVilleDTO);

        mockMvc.perform(post("/villes/creation-rapide")
                        .param("nom", "Test")
                        .param("nbHabitants", "1000")
                        .param("codeDepartement", "34"))
                .andExpect(status().isOk());
    }

    @Test
    void importVilles_ok() throws Exception {
        when(villeMapper.toEntityList(anyList())).thenReturn(List.of(sampleVille));
        when(villeService.importVilles(anyList())).thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        String body = objectMapper.writeValueAsString(Collections.singletonList(sampleVilleDTO));

        mockMvc.perform(post("/villes/import").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Montpellier"));
    }

    @Test
    void exportByDepartement_ok() throws Exception {
        when(villeService.exportVillesByDepartement("34")).thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/export/departement/34"))
                .andExpect(status().isOk());
    }

    @Test
    void rechercheAvancee_ok() throws Exception {
        when(villeService.findByDepartementAndPopulationRange("34", 1000, 300000))
                .thenReturn(List.of(sampleVille));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(sampleVilleDTO));

        mockMvc.perform(get("/villes/search/avancee")
                        .param("dept", "34")
                        .param("minPop", "1000")
                        .param("maxPop", "300000"))
                .andExpect(status().isOk());
    }
}
