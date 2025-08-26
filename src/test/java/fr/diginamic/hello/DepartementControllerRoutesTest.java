package fr.diginamic.hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.diginamic.hello.controlers.DepartementController;
import fr.diginamic.hello.dto.DepartementDTO;
import fr.diginamic.hello.dto.VilleDTO;
import fr.diginamic.hello.mappers.DepartementMapper;
import fr.diginamic.hello.mappers.VilleMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.services.DepartementService;
import fr.diginamic.hello.services.VilleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartementController.class)
class DepartementControllerRoutesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartementService departementService;

    @MockBean
    private VilleService villeService;

    @MockBean
    private DepartementMapper departementMapper;

    @MockBean
    private VilleMapper villeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Departement dep;
    private DepartementDTO depDTO;
    private Ville ville;
    private VilleDTO villeDTO;

    @BeforeEach
    void setup() {
        dep = new Departement();
        dep.setId(1L);
        dep.setCode("34");
        dep.setNom("Hérault");

        depDTO = new DepartementDTO();
        depDTO.setId(1L);
        depDTO.setCode("34");
        depDTO.setNom("Hérault");

        ville = new Ville();
        ville.setId(10L);
        ville.setNom("Montpellier");
        ville.setNbHabitants(295542);
        ville.setDepartement(dep);

        VilleDTO.DepartementSimplifieDTO d = new VilleDTO.DepartementSimplifieDTO(1L, "34", "Hérault");
        villeDTO = new VilleDTO(10L, "Montpellier", 295542, d);
    }

    @Test
    void getAll_ok() throws Exception {
        when(departementService.findAll()).thenReturn(List.of(dep));
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));
        mockMvc.perform(get("/departements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("34"));
    }

    @Test
    void getById_found_and_notFound() throws Exception {
        when(departementService.findById(1L)).thenReturn(Optional.of(dep));
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);
        mockMvc.perform(get("/departements/1")).andExpect(status().isOk());

        when(departementService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/departements/99")).andExpect(status().isNotFound());
    }

    @Test
    void getByCode_found_and_notFound() throws Exception {
        when(departementService.findByCode("34")).thenReturn(Optional.of(dep));
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);
        mockMvc.perform(get("/departements/code/34")).andExpect(status().isOk());

        when(departementService.findByCode("99")).thenReturn(Optional.empty());
        mockMvc.perform(get("/departements/code/99")).andExpect(status().isNotFound());
    }

    @Test
    void create_ok() throws Exception {
        when(departementMapper.toEntity(any(DepartementDTO.class))).thenReturn(dep);
        when(departementService.save(dep)).thenReturn(dep);
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);

        String body = objectMapper.writeValueAsString(depDTO);
        mockMvc.perform(post("/departements").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("34"));
    }

    @Test
    void update_ok_and_notFound() throws Exception {
        when(departementService.findById(1L)).thenReturn(Optional.of(dep));
        when(departementMapper.toEntity(any(DepartementDTO.class))).thenReturn(dep);
        when(departementService.save(dep)).thenReturn(dep);
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);
        String body = objectMapper.writeValueAsString(depDTO);
        mockMvc.perform(put("/departements/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        when(departementService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(put("/departements/99").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_noContent_or_badRequest() throws Exception {
        mockMvc.perform(delete("/departements/1")).andExpect(status().isNoContent());
    }

    @Test
    void search_nom_ok() throws Exception {
        when(departementService.findByNom("Hérault")).thenReturn(Optional.of(dep));
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);
        mockMvc.perform(get("/departements/search/nom").param("nom", "Hérault"))
                .andExpect(status().isOk());
    }

    @Test
    void with_nom_ok() throws Exception {
        when(departementService.findDepartementsWithNom()).thenReturn(List.of(dep));
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));
        mockMvc.perform(get("/departements/avec-nom")).andExpect(status().isOk());
    }

    @Test
    void without_nom_ok() throws Exception {
        when(departementService.findDepartementsWithoutNom()).thenReturn(List.of());
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of());
        mockMvc.perform(get("/departements/sans-nom")).andExpect(status().isOk());
    }

    @Test
    void with_villes_ok() throws Exception {
        when(departementService.findDepartementsWithVilles()).thenReturn(List.of(dep));
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));
        mockMvc.perform(get("/departements/avec-villes")).andExpect(status().isOk());
    }

    @Test
    void min_villes_ok() throws Exception {
        when(departementService.findDepartementsWithMinVilles(5)).thenReturn(List.of(dep));
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));
        mockMvc.perform(get("/departements/min-villes").param("min", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void min_population_ok() throws Exception {
        when(departementService.findDepartementsWithMinPopulation(1000000L)).thenReturn(List.of(dep));
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));
        mockMvc.perform(get("/departements/min-population").param("min", "1000000"))
                .andExpect(status().isOk());
    }

    @Test
    void categories_ok() throws Exception {
        when(departementService.findDepartementsMetropolitains()).thenReturn(List.of(dep));
        when(departementService.findDepartementsOutreMer()).thenReturn(List.of());
        when(departementService.findDepartementsCorse()).thenReturn(List.of());
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));

        mockMvc.perform(get("/departements/metropolitains")).andExpect(status().isOk());
        mockMvc.perform(get("/departements/outre-mer")).andExpect(status().isOk());
        mockMvc.perform(get("/departements/corse")).andExpect(status().isOk());
    }

    @Test
    void code_commence_ok() throws Exception {
        when(departementService.findByCodeStartingWith("3")).thenReturn(List.of(dep));
        when(departementMapper.toDTOList(anyList())).thenReturn(List.of(depDTO));
        mockMvc.perform(get("/departements/code-commence").param("prefix", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void villes_by_departement() throws Exception {
        when(departementService.findById(1L)).thenReturn(Optional.of(dep));
        when(villeService.exportVillesByDepartement("34")).thenReturn(List.of(ville));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(villeDTO));
        mockMvc.perform(get("/departements/1/villes")).andExpect(status().isOk());
    }

    @Test
    void villes_by_departement_code() throws Exception {
        when(villeService.exportVillesByDepartement("34")).thenReturn(List.of(ville));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(villeDTO));
        mockMvc.perform(get("/departements/code/34/villes")).andExpect(status().isOk());
    }

    @Test
    void villes_top_population_filters() throws Exception {
        when(villeService.findTopNVillesByDepartement("34", 3)).thenReturn(List.of(ville));
        when(villeService.findByDepartementAndMinPopulation("34", 1000)).thenReturn(List.of(ville));
        when(villeService.findByDepartementAndPopulationRange("34", 1000, 300000)).thenReturn(List.of(ville));
        when(villeMapper.toDTOList(anyList())).thenReturn(List.of(villeDTO));

        mockMvc.perform(get("/departements/code/34/villes/top").param("n", "3")).andExpect(status().isOk());
        mockMvc.perform(get("/departements/code/34/villes/population").param("min", "1000")).andExpect(status().isOk());
        mockMvc.perform(get("/departements/code/34/villes/population-plage").param("min", "1000").param("max", "300000"))
                .andExpect(status().isOk());
    }

    @Test
    void stats_counts_ok() throws Exception {
        when(departementService.count()).thenReturn(101L);
        mockMvc.perform(get("/departements/count")).andExpect(status().isOk()).andExpect(content().string("101"));

        when(departementService.findById(1L)).thenReturn(Optional.of(dep));
        when(departementService.countVillesById(1L)).thenReturn(10L);
        when(departementService.getTotalPopulationById(1L)).thenReturn(1_000_000L);
        mockMvc.perform(get("/departements/1/stats")).andExpect(status().isOk());

        when(villeService.getDepartementStats("34")).thenReturn(
                new fr.diginamic.hello.repositories.VilleRepositoryHelper.DepartementStats(
                        dep, 10L, 1_000_000L, ville
                )
        );
        mockMvc.perform(get("/departements/code/34/stats")).andExpect(status().isOk());

        when(departementService.findByCode("34")).thenReturn(Optional.of(dep));
        when(departementService.getTotalPopulationById(1L)).thenReturn(1_000_000L);
        mockMvc.perform(get("/departements/code/34/population-totale")).andExpect(status().isOk());

        when(departementService.countVillesById(1L)).thenReturn(10L);
        mockMvc.perform(get("/departements/code/34/nombre-villes")).andExpect(status().isOk());
    }

    @Test
    void gestion_avancee_ok() throws Exception {
        when(departementService.createDepartement("99", null)).thenReturn(dep);
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);
        mockMvc.perform(post("/departements/creation-rapide").param("code", "99"))
                .andExpect(status().isOk());

        when(departementService.updateNom("34", "Herault")).thenReturn(dep);
        when(departementMapper.toDTO(dep)).thenReturn(depDTO);
        mockMvc.perform(put("/departements/code/34/nom").param("nom", "Herault"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/departements/update-noms-manquants")).andExpect(status().isOk());

        when(departementService.existsByCode("34")).thenReturn(true);
        mockMvc.perform(get("/departements/exists/code/34")).andExpect(status().isOk()).andExpect(content().string("true"));
    }
}
