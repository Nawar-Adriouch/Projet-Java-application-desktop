package repository;

import model.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PdfPlanningWriter {

    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    // Dimensions
    private static final float PAGE_W  = PDRectangle.A4.getWidth();   // 595
    private static final float PAGE_H  = PDRectangle.A4.getHeight();  // 842
    private static final float MARGIN  = 30f;
    private static final float TABLE_W = PAGE_W - 2 * MARGIN;

    //  Couleurs RGB (0..1)
    private static final float[] VERT_TITRE   = {0.11f, 0.37f, 0.13f};
    private static final float[] VERT_ENTETE  = {0.18f, 0.49f, 0.20f};
    private static final float[] VERT_CLAIR   = {0.91f, 0.96f, 0.91f};
    private static final float[] BLANC        = {1f,    1f,    1f};
    private static final float[] GRIS_TEXTE   = {0.33f, 0.43f, 0.48f};
    private static final float[] NOIR         = {0.13f, 0.13f, 0.13f};
    private static final float[] BORDURE      = {0.78f, 0.90f, 0.79f};

    // Colonnes : label + largeur (% de TABLE_W)
    private static final String[] COL_LABELS = {
            "N°", "Date", "Début", "Fin", "Étudiant", "Encadrant", "Jury 1", "Jury 2", "Salle"
    };
    private static final float[] COL_PCTS = {
            0.04f, 0.10f, 0.07f, 0.07f, 0.18f, 0.16f, 0.16f, 0.16f, 0.06f
    };

    //  Hauteurs
    private static final float H_TITRE   = 44f;
    private static final float H_SOUS    = 22f;
    private static final float H_ENTETE  = 26f;
    private static final float H_LIGNE   = 20f;
    private static final float H_PIED    = 20f;


    public void exporter(Planning planning, String cheminSortie) throws IOException {

        if (planning == null || planning.getSoutenances().isEmpty())
            throw new IllegalArgumentException("Planning vide.");

        // Tri par date puis heure
        List<Soutenance> liste = new ArrayList<>(planning.getSoutenances());
        liste.sort(Comparator
                .comparing((Soutenance s) -> s.getCreneau().getDate())
                .thenComparing(s -> s.getCreneau().getHeureDebut()));

        try (PDDocument doc = new PDDocument()) {

            // Précalcul des largeurs de colonnes en pixels
            float[] colW = new float[COL_PCTS.length];
            for (int i = 0; i < COL_PCTS.length; i++)
                colW[i] = TABLE_W * COL_PCTS[i];

            // Calcul du nombre de lignes par page
            float yDebutDonnees = PAGE_H - MARGIN - H_TITRE - H_SOUS - 8 - H_ENTETE;
            int lignesParPage   = (int) ((yDebutDonnees - MARGIN - H_PIED) / H_LIGNE);

            // Découpage en pages
            int totalPages = (int) Math.ceil((double) liste.size() / lignesParPage);

            for (int page = 0; page < totalPages; page++) {
                int debut = page * lignesParPage;
                int fin   = Math.min(debut + lignesParPage, liste.size());
                List<Soutenance> portion = liste.subList(debut, fin);

                PDPage pdPage = new PDPage(PDRectangle.A4);
                doc.addPage(pdPage);

                try (PDPageContentStream cs = new PDPageContentStream(
                        doc, pdPage, PDPageContentStream.AppendMode.APPEND, true)) {

                    float y = PAGE_H - MARGIN;

                    // Titre (seulement page 1)
                    if (page == 0) {
                        y = dessinerTitre(cs, y, liste.size());
                    } else {
                        y = dessinerTitreContinuation(cs, y, page + 1, totalPages);
                    }

                    y -= 4;

                    //  En-tête du tableau
                    y = dessinerEnteteTableau(cs, y, colW);

                    // Lignes de données
                    for (int i = 0; i < portion.size(); i++) {
                        Soutenance s = portion.get(i);
                        boolean alt  = (debut + i) % 2 != 0;
                        y = dessinerLigneSoutenance(cs, y, debut + i + 1, s, colW, alt);
                    }

                    // Pied de page
                    dessinerPiedDePage(cs, page + 1, totalPages,
                            "Planning des Soutenances  •  Généré le " +
                                    java.time.LocalDate.now().format(FMT_DATE));
                }
            }

            doc.save(cheminSortie);
            System.out.println("Planning PDF " + cheminSortie
                    + " (" + liste.size() + " soutenances, " + totalPages + " page(s))");
        }
    }


    //  SECTIONS

    private float dessinerTitre(PDPageContentStream cs, float y, int total) throws IOException {
        PDFont fontBold   = PDType1Font.HELVETICA_BOLD;
        PDFont fontNormal = PDType1Font.HELVETICA;

        // Bande titre
        rempli(cs, MARGIN, y - H_TITRE, TABLE_W, H_TITRE, VERT_TITRE);
        texte(cs, fontBold, 18, BLANC,
                "PLANNING DES SOUTENANCES",
                centrerX("PLANNING DES SOUTENANCES", fontBold, 18), y - H_TITRE + 13);

        y -= H_TITRE;

        // Sous-titre
        rempli(cs, MARGIN, y - H_SOUS, TABLE_W, H_SOUS, VERT_ENTETE);
        texte(cs, fontNormal, 10, BLANC,
                "Total : " + total + " soutenance(s)   •   Généré le "
                        + java.time.LocalDate.now().format(FMT_DATE),
                MARGIN + 10, y - H_SOUS + 7);

        return y - H_SOUS;
    }

    private float dessinerTitreContinuation(PDPageContentStream cs, float y,
                                            int page, int total) throws IOException {
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        float h = 28f;
        rempli(cs, MARGIN, y - h, TABLE_W, h, VERT_TITRE);
        texte(cs, fontBold, 13, BLANC,
                "PLANNING DES SOUTENANCES  (suite)  —  Page " + page + "/" + total,
                MARGIN + 10, y - h + 9);
        return y - h;
    }

    private float dessinerEnteteTableau(PDPageContentStream cs, float y,
                                        float[] colW) throws IOException {
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        float x = MARGIN;
        rempli(cs, MARGIN, y - H_ENTETE, TABLE_W, H_ENTETE, VERT_ENTETE);

        for (int i = 0; i < COL_LABELS.length; i++) {
            texte(cs, fontBold, 9, BLANC,
                    tronquer(COL_LABELS[i], fontBold, 9, colW[i] - 4),
                    x + 3, y - H_ENTETE + 9);
            // Séparateur vertical
            trait(cs, x, y, x, y - H_ENTETE, BLANC, 0.4f);
            x += colW[i];
        }
        trait(cs, x, y, x, y - H_ENTETE, BLANC, 0.4f);
        return y - H_ENTETE;
    }

    private float dessinerLigneSoutenance(PDPageContentStream cs, float y,
                                          int num, Soutenance s, float[] colW, boolean alt) throws IOException {
        PDFont fontBold   = PDType1Font.HELVETICA_BOLD;
        PDFont fontNormal = PDType1Font.HELVETICA;

        float[] bg = alt ? VERT_CLAIR : BLANC;
        rempli(cs, MARGIN, y - H_LIGNE, TABLE_W, H_LIGNE, bg);

        String[] vals = {
                String.valueOf(num),
                s.getCreneau().getDate().format(FMT_DATE),
                s.getCreneau().getHeureDebut().format(FMT_HEURE),
                s.getCreneau().getHeureFin().format(FMT_HEURE),
                v(s.getEtudiant().getNom()) + " " + v(s.getEtudiant().getPrenom()),
                v(s.getEncadrant().getNom()) + " " + v(s.getEncadrant().getPrenom()),
                v(s.getJury1().getNom()) + " " + v(s.getJury1().getPrenom()),
                v(s.getJury2().getNom()) + " " + v(s.getJury2().getPrenom()),
                v(s.getSalle().getNom())
        };

        float x = MARGIN;
        for (int i = 0; i < vals.length; i++) {
            PDFont f = (i == 4) ? fontBold : fontNormal;
            texte(cs, f, 8, NOIR,
                    tronquer(vals[i], f, 8, colW[i] - 4),
                    x + 3, y - H_LIGNE + 6);
            trait(cs, x, y, x, y - H_LIGNE, BORDURE, 0.4f);
            x += colW[i];
        }
        trait(cs, x, y, x, y - H_LIGNE, BORDURE, 0.4f);
        // Ligne horizontale basse
        trait(cs, MARGIN, y - H_LIGNE, MARGIN + TABLE_W, y - H_LIGNE, BORDURE, 0.4f);

        return y - H_LIGNE;
    }

    private void dessinerPiedDePage(PDPageContentStream cs,
                                    int page, int total, String texte) throws IOException {
        PDFont font = PDType1Font.HELVETICA;
        trait(cs, MARGIN, MARGIN + H_PIED, MARGIN + TABLE_W,
                MARGIN + H_PIED, VERT_ENTETE, 0.5f);
        texte(cs, font, 8, GRIS_TEXTE, texte, MARGIN, MARGIN + 6);
        String pg = "Page " + page + " / " + total;
        texte(cs, font, 8, GRIS_TEXTE, pg,
                MARGIN + TABLE_W - longueur(pg, font, 8), MARGIN + 6);
    }


    //  HELPERS GRAPHIQUES

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

    private void trait(PDPageContentStream cs, float x1, float y1,
                       float x2, float y2, float[] c, float w) throws IOException {
        cs.setStrokingColor(c[0], c[1], c[2]);
        cs.setLineWidth(w);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private float centrerX(String txt, PDFont font, float size) throws IOException {
        float tw = font.getStringWidth(txt) / 1000 * size;
        return MARGIN + (TABLE_W - tw) / 2;
    }

    private float longueur(String txt, PDFont font, float size) throws IOException {
        return font.getStringWidth(txt) / 1000 * size;
    }

    private String tronquer(String txt, PDFont font, float size, float maxW) {
        if (txt == null) return "";
        try {
            while (txt.length() > 1 &&
                    font.getStringWidth(txt) / 1000 * size > maxW) {
                txt = txt.substring(0, txt.length() - 1);
            }
            return txt;
        } catch (IOException e) { return txt; }
    }

    private String v(String s) { return s != null ? s : ""; }
}