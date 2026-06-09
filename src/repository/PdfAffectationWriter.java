package repository;

import model.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Génère le tableau d'affectation en PDF.
 *
 * Format :
 * ┌──────────────────┬──────────────┬──────────────┬──────────────┐
 * │    Encadrant     │  Étudiant 1  │  Étudiant 2  │  Étudiant 3  │
 * ├──────────────────┼──────────────┼──────────────┼──────────────┤
 * │  Benali Omar     │ Alami Mohamed│ Karimi Sara  │ Tazi Youssef │
 * │  Tazi Fatima     │ Benali Ahmed │              │              │
 * └──────────────────┴──────────────┴──────────────┴──────────────┘
 */
public class PdfAffectationWriter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Dimensions A4 ─────────────────────────────────────────────────────
    private static final float PAGE_W = PDRectangle.A4.getWidth();
    private static final float PAGE_H = PDRectangle.A4.getHeight();
    private static final float MARGIN  = 30f;
    private static final float TABLE_W = PAGE_W - 2 * MARGIN;

    // ── Couleurs ──────────────────────────────────────────────────────────
    private static final float[] VERT_TITRE  = {0.11f, 0.37f, 0.13f};
    private static final float[] VERT_ENTETE = {0.18f, 0.49f, 0.20f};
    private static final float[] VERT_CLAIR  = {0.91f, 0.96f, 0.91f};
    private static final float[] BLANC       = {1f,    1f,    1f};
    private static final float[] GRIS        = {0.33f, 0.43f, 0.48f};
    private static final float[] NOIR        = {0.10f, 0.10f, 0.10f};
    private static final float[] BORDURE     = {0.78f, 0.90f, 0.79f};

    // ── Hauteurs ──────────────────────────────────────────────────────────
    private static final float H_TITRE  = 44f;
    private static final float H_SOUS   = 22f;
    private static final float H_ENTETE = 26f;
    private static final float H_LIGNE  = 22f;
    private static final float H_PIED   = 20f;

    // ════════════════════════════════════════════════════════════════════
    public void exporter(Map<Encadrant, List<Etudiant>> affectation,
                         String cheminSortie) throws IOException {

        if (affectation == null || affectation.isEmpty())
            throw new IllegalArgumentException("Affectation vide.");

        // ── Trouver le nombre max d'étudiants par encadrant ───────────────
        int maxEtu = affectation.values().stream()
                .mapToInt(List::size).max().orElse(1);

        // ── Largeurs des colonnes ─────────────────────────────────────────
        // Col 0 : Encadrant (25%)  |  Col 1..N : étudiants (reste / maxEtu)
        float colEncadrant = TABLE_W * 0.25f;
        float colEtu       = (TABLE_W - colEncadrant) / maxEtu;

        int totalEtudiants = affectation.values().stream()
                .mapToInt(List::size).sum();

        try (PDDocument doc = new PDDocument()) {

            // Calcul lignes par page
            float yDebut         = PAGE_H - MARGIN - H_TITRE - H_SOUS - 8 - H_ENTETE;
            int   lignesParPage  = (int) ((yDebut - MARGIN - H_PIED) / H_LIGNE);

            List<Map.Entry<Encadrant, List<Etudiant>>> entries =
                    new ArrayList<>(affectation.entrySet());

            int totalPages = (int) Math.ceil((double) entries.size() / lignesParPage);

            for (int p = 0; p < totalPages; p++) {
                int debut = p * lignesParPage;
                int fin   = Math.min(debut + lignesParPage, entries.size());

                PDPage pdPage = new PDPage(PDRectangle.A4);
                doc.addPage(pdPage);

                try (PDPageContentStream cs = new PDPageContentStream(
                        doc, pdPage, PDPageContentStream.AppendMode.APPEND, true)) {

                    float y = PAGE_H - MARGIN;

                    // ── Titre ─────────────────────────────────────────────
                    y = dessinerTitre(cs, y, affectation.size(),
                            totalEtudiants, p + 1, totalPages);
                    y -= 6;

                    // ── En-tête colonnes ──────────────────────────────────
                    y = dessinerEntete(cs, y, colEncadrant, colEtu, maxEtu);

                    // ── Lignes encadrants ─────────────────────────────────
                    for (int i = debut; i < fin; i++) {
                        Map.Entry<Encadrant, List<Etudiant>> entry = entries.get(i);
                        boolean alt = i % 2 != 0;
                        y = dessinerLigne(cs, y, entry.getKey(),
                                entry.getValue(), colEncadrant, colEtu, maxEtu, alt);
                    }

                    // ── Pied de page ──────────────────────────────────────
                    dessinerPied(cs, p + 1, totalPages);
                }
            }

            doc.save(cheminSortie);
            System.out.println("✅ Affectation PDF → " + cheminSortie);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  SECTIONS
    // ════════════════════════════════════════════════════════════════════

    private float dessinerTitre(PDPageContentStream cs, float y,
                                int nbEnc, int nbEtu, int page, int total) throws IOException {

        PDFont bold   = PDType1Font.HELVETICA_BOLD;
        PDFont normal = PDType1Font.HELVETICA;

        // Bande titre
        rempli(cs, MARGIN, y - H_TITRE, TABLE_W, H_TITRE, VERT_TITRE);
        texte(cs, bold, 18, BLANC,
                "TABLEAU D'AFFECTATION",
                centrerX("TABLEAU D'AFFECTATION", bold, 18), y - H_TITRE + 13);
        y -= H_TITRE;

        // Sous-titre
        rempli(cs, MARGIN, y - H_SOUS, TABLE_W, H_SOUS, VERT_ENTETE);
        String info = nbEnc + " encadrant(s)   •   " + nbEtu
                + " étudiant(s)   •   Généré le " + LocalDate.now().format(FMT)
                + (total > 1 ? "   •   Page " + page + "/" + total : "");
        texte(cs, normal, 9, BLANC, info, MARGIN + 8, y - H_SOUS + 7);
        return y - H_SOUS;
    }

    private float dessinerEntete(PDPageContentStream cs, float y,
                                 float colEnc, float colEtu, int maxEtu) throws IOException {

        PDFont bold = PDType1Font.HELVETICA_BOLD;
        rempli(cs, MARGIN, y - H_ENTETE, TABLE_W, H_ENTETE, VERT_ENTETE);

        // Colonne Encadrant
        texte(cs, bold, 10, BLANC, "Encadrant", MARGIN + 5, y - H_ENTETE + 8);
        traitV(cs, MARGIN, y, y - H_ENTETE, BLANC, 0.5f);

        // Colonnes Étudiants
        for (int i = 0; i < maxEtu; i++) {
            float x = MARGIN + colEnc + i * colEtu;
            texte(cs, bold, 9, BLANC,
                    "Étudiant " + (i + 1), x + 4, y - H_ENTETE + 8);
            traitV(cs, x, y, y - H_ENTETE, BLANC, 0.5f);
        }
        // Bordure droite
        traitV(cs, MARGIN + TABLE_W, y, y - H_ENTETE, BLANC, 0.5f);
        // Ligne basse
        traitH(cs, MARGIN, MARGIN + TABLE_W, y - H_ENTETE, BLANC, 0.5f);

        return y - H_ENTETE;
    }

    private float dessinerLigne(PDPageContentStream cs, float y,
                                Encadrant enc, List<Etudiant> etudiants,
                                float colEnc, float colEtu, int maxEtu, boolean alt) throws IOException {

        PDFont bold   = PDType1Font.HELVETICA_BOLD;
        PDFont normal = PDType1Font.HELVETICA;

        float[] bg = alt ? VERT_CLAIR : BLANC;
        rempli(cs, MARGIN, y - H_LIGNE, TABLE_W, H_LIGNE, bg);

        // ── Colonne encadrant ─────────────────────────────────────────────
        String nomEnc = tronquer(v(enc.getNom()) + " " + v(enc.getPrenom()),
                bold, 10, colEnc - 8);
        texte(cs, bold, 10, VERT_TITRE, nomEnc, MARGIN + 5, y - H_LIGNE + 7);
        traitV(cs, MARGIN, y, y - H_LIGNE, BORDURE, 0.4f);

        // ── Colonnes étudiants ────────────────────────────────────────────
        for (int i = 0; i < maxEtu; i++) {
            float x = MARGIN + colEnc + i * colEtu;
            traitV(cs, x, y, y - H_LIGNE, BORDURE, 0.4f);

            if (i < etudiants.size()) {
                Etudiant e = etudiants.get(i);
                String nomEtu = tronquer(
                        v(e.getNom()) + " " + v(e.getPrenom()),
                        normal, 9, colEtu - 8);
                texte(cs, normal, 9, NOIR, nomEtu, x + 4, y - H_LIGNE + 7);
            }
        }
        // Bordure droite + ligne basse
        traitV(cs, MARGIN + TABLE_W, y, y - H_LIGNE, BORDURE, 0.4f);
        traitH(cs, MARGIN, MARGIN + TABLE_W, y - H_LIGNE, BORDURE, 0.4f);

        return y - H_LIGNE;
    }

    private void dessinerPied(PDPageContentStream cs,
                              int page, int total) throws IOException {
        PDFont font = PDType1Font.HELVETICA;
        traitH(cs, MARGIN, MARGIN + TABLE_W, MARGIN + H_PIED, VERT_ENTETE, 0.5f);
        texte(cs, font, 8, GRIS,
                "Tableau d'affectation  •  Généré le " + LocalDate.now().format(FMT),
                MARGIN, MARGIN + 6);
        String pg = "Page " + page + " / " + total;
        texte(cs, font, 8, GRIS, pg,
                MARGIN + TABLE_W - longueur(pg, font, 8), MARGIN + 6);
    }

    // ════════════════════════════════════════════════════════════════════
    //  HELPERS GRAPHIQUES
    // ════════════════════════════════════════════════════════════════════

    private void rempli(PDPageContentStream cs, float x, float y,
                        float w, float h, float[] c) throws IOException {
        cs.setNonStrokingColor(c[0], c[1], c[2]);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void texte(PDPageContentStream cs, PDFont font, float size,
                       float[] color, String txt, float x, float y) throws IOException {
        if (txt == null || txt.isBlank()) return;
        cs.setNonStrokingColor(color[0], color[1], color[2]);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(txt);
        cs.endText();
    }

    private void traitV(PDPageContentStream cs, float x,
                        float yHaut, float yBas, float[] c, float w) throws IOException {
        cs.setStrokingColor(c[0], c[1], c[2]);
        cs.setLineWidth(w);
        cs.moveTo(x, yHaut);
        cs.lineTo(x, yBas);
        cs.stroke();
    }

    private void traitH(PDPageContentStream cs, float x1, float x2,
                        float y, float[] c, float w) throws IOException {
        cs.setStrokingColor(c[0], c[1], c[2]);
        cs.setLineWidth(w);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    private float centrerX(String txt, PDFont font, float size) throws IOException {
        return MARGIN + (TABLE_W - font.getStringWidth(txt) / 1000 * size) / 2;
    }

    private float longueur(String txt, PDFont font, float size) throws IOException {
        return font.getStringWidth(txt) / 1000 * size;
    }

    private String tronquer(String txt, PDFont font, float size, float maxW) {
        if (txt == null) return "";
        try {
            while (txt.length() > 1 &&
                    font.getStringWidth(txt) / 1000 * size > maxW)
                txt = txt.substring(0, txt.length() - 1);
            return txt;
        } catch (IOException e) { return txt; }
    }

    private String v(String s) { return s != null ? s : ""; }
}