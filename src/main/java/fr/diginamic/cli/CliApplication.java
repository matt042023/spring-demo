package fr.diginamic.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.repositories.DepartementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@SpringBootApplication(scanBasePackages = {"com.example.cli", "fr.diginamic.hello"})
@EnableJpaRepositories("fr.diginamic.hello")
@EntityScan("fr.diginamic.hello")
public class CliApplication implements CommandLineRunner {

    private final DepartementRepository departementRepository;

    public CliApplication(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(CliApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Démarrage de la synchronisation des départements depuis l'API externe...");

        final String url = "https://geo.api.gouv.fr/departements";

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        String json;
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                System.err.println("Appel API non réussi: " + resp.getStatusCode());
                return;
            }
            json = resp.getBody();
            if (json == null || json.isBlank()) {
                System.err.println("Réponse API vide (body null/blank).");
                return;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel API: " + e.getMessage());
            return;
        }

        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (Exception e) {
            System.err.println("JSON invalide: " + e.getMessage());
            return;
        }
        if (!root.isArray()) {
            System.err.println("Réponse inattendue: la racine n'est pas un tableau JSON.");
            return;
        }

        // --- 1) Normaliser les données entrantes (dé-dup par code) ---
        Map<String, String> incomingByCode = new LinkedHashMap<>();
        for (JsonNode node : root) {
            String code = node.path("code").asText(null);
            String name = node.hasNonNull("nom") ? node.get("nom").asText()
                    : node.path("name").asText(null);
            if (code == null) continue;

            code = code.trim();                 // ex: "01", "2A", "974"
            String normName = name == null ? null : name.trim();

            if (incomingByCode.containsKey(code)) {
                System.out.println("[WARN] Doublon API pour code=" + code + " (on garde la dernière valeur).");
            }
            incomingByCode.put(code, normName);
        }

        // --- 2) Charger l'existant en BDD ---
        List<Departement> existing = departementRepository.findAll();
        Map<String, Departement> existingByCode = new HashMap<>();
        for (Departement d : existing) {
            String code = safeGetString(d, "getCode", "code");
            if (code != null) existingByCode.put(code.trim(), d);
        }

        // --- 3) Calculer insert / update / ignore ---
        List<Departement> toInsert = new ArrayList<>();
        List<Departement> toUpdate = new ArrayList<>();
        int ignored = 0;

        for (Map.Entry<String, String> e : incomingByCode.entrySet()) {
            String code = e.getKey();
            String incomingName = e.getValue();

            Departement current = existingByCode.get(code);
            if (current == null) {
                // CREATE
                Departement n = Departement.class.getDeclaredConstructor().newInstance();
                safeSet(n, "setCode", "code", code);
                if (incomingName != null) {
                    if (!safeSet(n, "setNom", "nom", incomingName)) {
                        safeSet(n, "setName", "name", incomingName);
                    }
                }
                toInsert.add(n);
            } else {
                // UPDATE si différent
                String dbName = safeGetString(current, "getNom", "nom");
                if (dbName == null) dbName = safeGetString(current, "getName", "name");

                String dbNorm = dbName == null ? null : dbName.trim();
                String inNorm = incomingName == null ? null : incomingName.trim();

                boolean different = !Objects.equals(dbNorm, inNorm);
                if (different) {
                    if (incomingName != null) {
                        if (!safeSet(current, "setNom", "nom", incomingName)) {
                            safeSet(current, "setName", "name", incomingName);
                        }
                    } else {
                        // Si l'API ne donne pas de nom, on ne touche pas au nom existant
                    }
                    toUpdate.add(current);
                } else {
                    ignored++;
                }
            }
        }

        // --- 4) Persister ---
        if (!toInsert.isEmpty()) {
            departementRepository.saveAll(toInsert);
        }
        if (!toUpdate.isEmpty()) {
            departementRepository.saveAll(toUpdate);
        }

        // --- 5) Bilan ---
        System.out.println("Synchronisation terminée.");
        System.out.println("Créés   : " + toInsert.size());
        System.out.println("Mises à jour : " + toUpdate.size());
        System.out.println("Ignorés : " + ignored);
        System.out.println("Total API (unique code) : " + incomingByCode.size());
        System.out.println("Total en BDD après synchro (approx.) : " +
                (existingByCode.size() + toInsert.size())); // approximation (updates ne changent pas le count)
    }

    // --- Utils reflection "souples" ---

    private String safeGetString(Object target, String getterName, String fieldNameFallback) {
        try {
            Method m = target.getClass().getMethod(getterName);
            Object val = m.invoke(target);
            return (val == null) ? null : String.valueOf(val);
        } catch (NoSuchMethodException e) {
            try {
                Field f = target.getClass().getDeclaredField(fieldNameFallback);
                f.setAccessible(true);
                Object val = f.get(target);
                return (val == null) ? null : String.valueOf(val);
            } catch (Exception ignore) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean safeSet(Object target, String setterName, String fieldName, Object value) {
        try {
            Class<?> paramType = (value instanceof String) ? String.class : value.getClass();
            try {
                Method m = target.getClass().getMethod(setterName, paramType);
                m.invoke(target, value);
                return true;
            } catch (NoSuchMethodException tryField) {
                Field f = target.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
