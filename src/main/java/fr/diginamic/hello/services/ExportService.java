package fr.diginamic.hello.services;

import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.repositories.VilleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private VilleRepository villeRepository;

    /**
     * Exporte les villes au format CSV dont la population est supérieure au minimum donné
     * Format: nom de la ville, nombre d'habitants, code département, nom du département
     *
     * @param populationMinimum seuil minimum de population
     * @return ByteArrayOutputStream contenant le CSV
     * @throws IOException en cas d'erreur d'écriture
     */
    public ByteArrayOutputStream exportVillesToCsv(int populationMinimum) throws IOException {
        List<Ville> villes = villeRepository.findByNbHabitantsGreaterThanOrderByNbHabitantsDesc(populationMinimum);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        // En-tête CSV
        writer.write("Nom Ville,Population,Code Département,Nom Département\n");

        // Données des villes
        for (Ville ville : villes) {
            writer.write(String.format("%s,%d,%s,%s\n",
                    escapeCSV(ville.getNom()),
                    ville.getNbHabitants(),
                    ville.getDepartement().getCode(),
                    escapeCSV(ville.getDepartement().getNom())
            ));
        }

        writer.flush();
        writer.close();

        return outputStream;
    }

    /**
     * Échappe les caractères spéciaux pour CSV
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}