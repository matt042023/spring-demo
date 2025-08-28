package fr.diginamic.hello.services;

import fr.diginamic.hello.models.Departement;
import fr.diginamic.hello.models.Ville;
import fr.diginamic.hello.repositories.DepartementRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PdfExportService {

    @Autowired
    private DepartementRepository departementRepository;

    /**
     * Génère un PDF détaillé pour un département
     *
     * @param codeDepartement code du département
     * @return ByteArrayOutputStream contenant le PDF
     * @throws DocumentException, IOException en cas d'erreur
     */
    public ByteArrayOutputStream exportDepartementToPdf(String codeDepartement)
            throws DocumentException, IOException {

        Optional<Departement> departementOpt = departementRepository.findByCode(codeDepartement);

        if (departementOpt.isEmpty()) {
            throw new IllegalArgumentException("Département non trouvé : " + codeDepartement);
        }

        Departement departement = departementOpt.get();

        // Création du document PDF
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Titre principal
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Rapport Département - " + departement.getNom(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date de génération
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        Paragraph dateGeneration = new Paragraph(
                "Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                dateFont
        );
        dateGeneration.setAlignment(Element.ALIGN_RIGHT);
        dateGeneration.setSpacingAfter(15);
        document.add(dateGeneration);

        // Informations du département
        addDepartementInfo(document, departement);

        // Liste des villes
        addVillesTable(document, departement);

        // Statistiques
        addStatistics(document, departement);

        document.close();

        return outputStream;
    }

    /**
     * Ajoute les informations générales du département
     */
    private void addDepartementInfo(Document document, Departement departement) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        Paragraph infoTitle = new Paragraph("Informations générales", sectionFont);
        infoTitle.setSpacingBefore(10);
        infoTitle.setSpacingAfter(10);
        document.add(infoTitle);

        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        document.add(new Paragraph("Code du département : " + departement.getCode(), normalFont));
        document.add(new Paragraph("Nom du département : " + departement.getNom(), normalFont));
        document.add(new Paragraph("Nombre de villes : " + departement.getVilles().size(), normalFont));
    }

    /**
     * Ajoute le tableau des villes
     */
    private void addVillesTable(Document document, Departement departement) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        Paragraph villesTitle = new Paragraph("Liste des villes", sectionFont);
        villesTitle.setSpacingBefore(20);
        villesTitle.setSpacingAfter(10);
        document.add(villesTitle);

        // Création du tableau
        PdfPTable table = new PdfPTable(2); // 2 colonnes : nom, population
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // En-têtes
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);

        PdfPCell headerNom = new PdfPCell(new Phrase("Nom de la ville", headerFont));
        headerNom.setBackgroundColor(BaseColor.DARK_GRAY);
        headerNom.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerNom.setPadding(8);
        table.addCell(headerNom);

        PdfPCell headerPop = new PdfPCell(new Phrase("Population", headerFont));
        headerPop.setBackgroundColor(BaseColor.DARK_GRAY);
        headerPop.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerPop.setPadding(8);
        table.addCell(headerPop);

        // Données des villes
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        for (Ville ville : departement.getVilles()) {
            PdfPCell cellNom = new PdfPCell(new Phrase(ville.getNom(), cellFont));
            cellNom.setPadding(5);
            table.addCell(cellNom);

            PdfPCell cellPop = new PdfPCell(new Phrase(String.format("%,d", ville.getNbHabitants()), cellFont));
            cellPop.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellPop.setPadding(5);
            table.addCell(cellPop);
        }

        document.add(table);
    }

    /**
     * Ajoute les statistiques du département
     */
    private void addStatistics(Document document, Departement departement) throws DocumentException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        Paragraph statsTitle = new Paragraph("Statistiques", sectionFont);
        statsTitle.setSpacingBefore(20);
        statsTitle.setSpacingAfter(10);
        document.add(statsTitle);

        // Calculs statistiques
        long totalPopulation = departement.getVilles().stream()
                .mapToLong(Ville::getNbHabitants)
                .sum();

        double moyennePopulation = departement.getVilles().stream()
                .mapToDouble(Ville::getNbHabitants)
                .average()
                .orElse(0.0);

        int maxPopulation = departement.getVilles().stream()
                .mapToInt(Ville::getNbHabitants)
                .max()
                .orElse(0);

        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        document.add(new Paragraph(String.format("Population totale : %,d habitants", totalPopulation), normalFont));
        document.add(new Paragraph(String.format("Population moyenne par ville : %.0f habitants", moyennePopulation), normalFont));
        document.add(new Paragraph(String.format("Population maximale : %,d habitants", maxPopulation), normalFont));
    }
}