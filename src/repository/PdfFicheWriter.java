package repository;

import model.Soutenance;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class PdfFicheWriter {

    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    private static final float PAGE_W = PDRectangle.A4.getWidth();   // 595
    private static final float PAGE_H = PDRectangle.A4.getHeight();  // 842
    private static final float MARGIN  = 40f;
    private static final float COL_W   = PAGE_W - 2 * MARGIN;

    private static final float[] VERT_FONCE  = {0.11f, 0.37f, 0.13f};  // #1B5E20
    private static final float[] VERT_MOY    = {0.18f, 0.49f, 0.20f};  // #2E7D32
    private static final float[] VERT_CLAIR  = {0.91f, 0.96f, 0.91f};  // #E8F5E9
    private static final float[] GRIS        = {0.33f, 0.43f, 0.48f};  // #546E7A
    private static final float[] BLANC       = {1f, 1f, 1f};
    private static final float[] BORDURE     = {0.78f, 0.90f, 0.79f};  // #C8E6C9
    private static final float[] BORDURE2    = {0.56f, 0.64f, 0.68f};  // #90A4AE

    public List<String> exporterFiches(List<Soutenance> soutenances, String cheminDossier) {

        List<String> creees = new ArrayList<>();
        if (soutenances == null || soutenances.isEmpty()) {
            System.err.println("exporterFiches PDF : liste vide.");
            return creees;
        }

        File dossier = new File(cheminDossier);
        if (!dossier.exists()) dossier.mkdirs();

        System.out.println("Export PDF " + cheminDossier
                + " (" + soutenances.size() + " fiche(s))");

        for (Soutenance s : soutenances) {
            String cne    = val(s.getEtudiant().getCne());
            String nom    = val(s.getEtudiant().getNom());
            String prenom = val(s.getEtudiant().getPrenom());

            String nomFichier = "Fiche_" + sanitiser(nom)
                    + "_" + sanitiser(prenom)
                    + "_" + sanitiser(cne) + ".pdf";
            String chemin = cheminDossier + File.separator + nomFichier;

            try {
                genererPdf(s, chemin);
                creees.add(chemin);
            } catch (Exception e) {
                System.err.println(" probleme lors de la creation de fichier " + nomFichier + " : " + e.getMessage());
                e.printStackTrace();
            }
        }

        return creees;
    }

    private void genererPdf(Soutenance s, String chemin) throws IOException {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDFont fontBold   = PDType1Font.HELVETICA_BOLD;
            PDFont fontNormal = PDType1Font.HELVETICA;

            try (PDPageContentStream cs = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.APPEND, true)) {

                float y = PAGE_H - MARGIN;

                // Titre principal
                y = dessinerBandeTitre(cs, y,
                        "FICHE DE SOUTENANCE", fontBold, 18, VERT_FONCE, BLANC);

                // Sous-titre  nom Etudiant
                y = dessinerBandeTitre(cs, y,
                        val(s.getEtudiant().getNom()) + " " + val(s.getEtudiant().getPrenom()),
                        fontBold, 12, VERT_MOY, BLANC);

                y -= 10;

                // Section 1 Informations Etudiant
                y = dessinerSectionHeader(cs, y, "INFORMATIONS ÉTUDIANT", fontBold);
                y = dessinerLigne(cs, y, "CNE",              val(s.getEtudiant().getCne()), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Nom complet",      val(s.getEtudiant().getNom()) + " " + val(s.getEtudiant().getPrenom()), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Filière",          val(s.getEtudiant().getFiliere()), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Langue",           val(s.getEtudiant().getLangue()), fontBold, fontNormal);
                y = dessinerLigneMulti(cs, y, "Sujet",       val(s.getEtudiant().getSujet()), fontBold, fontNormal);

                y -= 8;

                //Section 2 Créneau ET Salle
                y = dessinerSectionHeader(cs, y, "CRÉNEAU & SALLE", fontBold);
                y = dessinerLigne(cs, y, "Date",        s.getCreneau().getDate().format(FMT_DATE), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Heure début", s.getCreneau().getHeureDebut().format(FMT_HEURE), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Heure fin",   s.getCreneau().getHeureFin().format(FMT_HEURE), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Salle",       val(s.getSalle().getNom()), fontBold, fontNormal);

                y -= 8;

                // Section 3 Jury
                y = dessinerSectionHeader(cs, y, "COMPOSITION DU JURY", fontBold);
                y = dessinerLigne(cs, y, "Encadrant",
                        val(s.getEncadrant().getNom()) + " " + val(s.getEncadrant().getPrenom())
                                + "  |  " + val(s.getEncadrant().getDepartement()), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Jury 1 ",
                        val(s.getJury1().getNom()) + " " + val(s.getJury1().getPrenom())
                                + "  |  " + val(s.getJury1().getDepartement()), fontBold, fontNormal);
                y = dessinerLigne(cs, y, "Jury 2 ",
                        val(s.getJury2().getNom()) + " " + val(s.getJury2().getPrenom())
                                + "  |  " + val(s.getJury2().getDepartement()), fontBold, fontNormal);

                y -= 8;

                //Section : Évaluation
                y = dessinerSectionHeader(cs, y, "ÉVALUATION", fontBold);
                y = dessinerLigneVide(cs, y, "Note Rapport (/20)",       fontBold);
                y = dessinerLigneVide(cs, y, "Note Présentation (/20)",  fontBold);
                y = dessinerLigneVide(cs, y, "Note Soutenance (/20)",    fontBold);
                y = dessinerLigneVide(cs, y, "Note Finale (/20)",        fontBold);
                y = dessinerLigneVideHaute(cs, y, "Appréciation générale", fontBold);

                y -= 12;

                //  Signatures
                dessinerSignatures(cs, y, fontBold);
            }

            doc.save(chemin);
        }
    }


    //  COMPOSANTS GRAPHIQUES

    private float dessinerBandeTitre(PDPageContentStream cs, float y,
                                     String texte, PDFont font, float fontSize,
                                     float[] bgColor, float[] fgColor) throws IOException {
        float h = fontSize + 16;
        rect(cs, MARGIN, y - h, COL_W, h, bgColor, true);
        cs.beginText();
        cs.setFont(font, fontSize);
        setColor(cs, fgColor, false);
        float tw = font.getStringWidth(texte) / 1000 * fontSize;
        cs.newLineAtOffset(MARGIN + (COL_W - tw) / 2, y - h + 6);
        cs.showText(texte);
        cs.endText();
        return y - h - 1;
    }


    private float dessinerSectionHeader(PDPageContentStream cs, float y,
                                        String titre, PDFont font) throws IOException {
        float h = 20f;
        rect(cs, MARGIN, y - h, COL_W, h, VERT_MOY, true);
        cs.beginText();
        cs.setFont(font, 10);
        setColor(cs, BLANC, false);
        cs.newLineAtOffset(MARGIN + 6, y - h + 5);
        cs.showText(titre);
        cs.endText();
        return y - h - 1;
    }

    private float dessinerLigne(PDPageContentStream cs, float y,
                                String label, String valeur, PDFont fontBold, PDFont fontNormal) throws IOException {
        float h = 20f;
        float col1 = COL_W * 0.32f;

        // Fond label
        rect(cs, MARGIN, y - h, col1, h, VERT_CLAIR, true);
        border(cs, MARGIN, y - h, col1, h, BORDURE);

        // Fond valeur
        rect(cs, MARGIN + col1, y - h, COL_W - col1, h, BLANC, true);
        border(cs, MARGIN + col1, y - h, COL_W - col1, h, BORDURE);

        // Texte label
        cs.beginText();
        cs.setFont(fontBold, 9);
        setColor(cs, GRIS, false);
        cs.newLineAtOffset(MARGIN + 5, y - h + 6);
        cs.showText(label);
        cs.endText();

        // Texte valeur
        cs.beginText();
        cs.setFont(fontNormal, 9);
        setColor(cs, new float[]{0,0,0}, false);
        String v = valeur != null ? valeur : "";
        if (v.length() > 70) v = v.substring(0, 67) + "...";
        cs.newLineAtOffset(MARGIN + col1 + 5, y - h + 6);
        cs.showText(v);
        cs.endText();

        return y - h - 1;
    }

    private float dessinerLigneMulti(PDPageContentStream cs, float y,
                                     String label, String valeur, PDFont fontBold, PDFont fontNormal) throws IOException {
        float h = 30f;
        float col1 = COL_W * 0.32f;

        rect(cs, MARGIN, y - h, col1, h, VERT_CLAIR, true);
        border(cs, MARGIN, y - h, col1, h, BORDURE);
        rect(cs, MARGIN + col1, y - h, COL_W - col1, h, BLANC, true);
        border(cs, MARGIN + col1, y - h, COL_W - col1, h, BORDURE);

        cs.beginText();
        cs.setFont(fontBold, 9);
        setColor(cs, GRIS, false);
        cs.newLineAtOffset(MARGIN + 5, y - h + 16);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.setFont(fontNormal, 9);
        setColor(cs, new float[]{0,0,0}, false);
        String v = valeur != null ? valeur : "";
        // Deux lignes max
        if (v.length() > 75) {
            cs.newLineAtOffset(MARGIN + col1 + 5, y - h + 18);
            cs.showText(v.substring(0, 75));
            cs.newLineAtOffset(0, -12);
            String rest = v.substring(75);
            if (rest.length() > 75) rest = rest.substring(0, 72) + "...";
            cs.showText(rest);
        } else {
            cs.newLineAtOffset(MARGIN + col1 + 5, y - h + 16);
            cs.showText(v);
        }
        cs.endText();

        return y - h - 1;
    }

    private float dessinerLigneVide(PDPageContentStream cs, float y,
                                    String label, PDFont fontBold) throws IOException {
        float h = 22f;
        float col1 = COL_W * 0.55f;

        rect(cs, MARGIN, y - h, col1, h, VERT_CLAIR, true);
        border(cs, MARGIN, y - h, col1, h, BORDURE);

        // Zone à remplir
        rect(cs, MARGIN + col1, y - h, COL_W - col1, h, BLANC, true);
        border(cs, MARGIN + col1, y - h, COL_W - col1, h, BORDURE2);

        cs.beginText();
        cs.setFont(fontBold, 9);
        setColor(cs, GRIS, false);
        cs.newLineAtOffset(MARGIN + 5, y - h + 7);
        cs.showText(label);
        cs.endText();

        return y - h - 1;
    }

    private float dessinerLigneVideHaute(PDPageContentStream cs, float y,
                                         String label, PDFont fontBold) throws IOException {
        float h = 40f;
        float col1 = COL_W * 0.32f;

        rect(cs, MARGIN, y - h, col1, h, VERT_CLAIR, true);
        border(cs, MARGIN, y - h, col1, h, BORDURE);
        rect(cs, MARGIN + col1, y - h, COL_W - col1, h, BLANC, true);
        border(cs, MARGIN + col1, y - h, COL_W - col1, h, BORDURE2);

        cs.beginText();
        cs.setFont(fontBold, 9);
        setColor(cs, GRIS, false);
        cs.newLineAtOffset(MARGIN + 5, y - h + 26);
        cs.showText(label);
        cs.endText();

        return y - h - 1;
    }

    private void dessinerSignatures(PDPageContentStream cs, float y, PDFont fontBold) throws IOException {
        float h = 55f;
        float half = COL_W / 2;

        for (int i = 0; i < 2; i++) {
            float x = MARGIN + i * half;
            border(cs, x + 4, y - h, half - 8, h, BORDURE2);
            String sig = i == 0 ? "Signature de l'encadrant" : "Signature du jury";
            cs.beginText();
            cs.setFont(fontBold, 8);
            setColor(cs, GRIS, false);
            cs.newLineAtOffset(x + 8, y - 14);
            cs.showText(sig);
            cs.endText();
        }
    }

    //  HELPERS GRAPHIQUES PDFBox


    private void rect(PDPageContentStream cs, float x, float y,
                      float w, float h, float[] color, boolean fill) throws IOException {
        setColor(cs, color, fill);
        cs.addRect(x, y, w, h);
        if (fill) cs.fill(); else cs.stroke();
    }


    private void border(PDPageContentStream cs, float x, float y,
                        float w, float h, float[] color) throws IOException {
        cs.setStrokingColor(color[0], color[1], color[2]);
        cs.setLineWidth(0.5f);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private void setColor(PDPageContentStream cs, float[] c, boolean fill) throws IOException {
        if (fill) cs.setNonStrokingColor(c[0], c[1], c[2]);
        else      cs.setNonStrokingColor(c[0], c[1], c[2]);
    }

    private String val(String s)       { return s != null ? s : ""; }
    private String sanitiser(String s) { return s == null ? "" : s.replaceAll("[^a-zA-ZÀ-ÿ0-9_-]", "_"); }
}