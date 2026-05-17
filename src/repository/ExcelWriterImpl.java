package repository;

import model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ExcelWriterImpl implements ExcelWriter {

    // ── Formatters ───────────────────────────────────────────────────────────
    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    // ── Couleurs (ARGB hex) ──────────────────────────────────────────────────
    private static final String BLEU_TITRE   = "FF1E3A5F"; // bleu marine foncé
    private static final String BLEU_ENTETE  = "FF2D6A9F"; // bleu moyen
    private static final String BLEU_CLAIR   = "FFD6E4F0"; // bleu très clair (lignes paires)
    private static final String BLANC        = "FFFFFFFF";
    private static final String GRIS_BORD    = "FFB0BEC5";
    private static final String VERT_VALIDE  = "FF27AE60"; // vert (label)
    private static final String OR_ACCENT    = "FFFFC107"; // doré (accent fiches)

    // ════════════════════════════════════════════════════════════════════════
    //  1. EXPORT PLANNING GLOBAL
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void exporterPlanning(Planning planning, String cheminSortie) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFSheet sheet = wb.createSheet("Planning Soutenances");
            sheet.setDefaultColumnWidth(22);

            // ── Styles ────────────────────────────────────────────────────
            CellStyle styleTitre  = creerStyleTitre(wb);
            CellStyle styleEntete = creerStyleEntete(wb);
            CellStyle styleData   = creerStyleData(wb, false);
            CellStyle styleDataAlt= creerStyleData(wb, true);

            // ── Ligne titre principal ─────────────────────────────────────
            Row rowTitre = sheet.createRow(0);
            rowTitre.setHeightInPoints(36);
            Cell cellTitre = rowTitre.createCell(0);
            cellTitre.setCellValue("PLANNING DES SOUTENANCES");
            cellTitre.setCellStyle(styleTitre);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // ── Ligne sous-titre (date de génération) ─────────────────────
            Row rowSub = sheet.createRow(1);
            rowSub.setHeightInPoints(20);
            Cell cellSub = rowSub.createCell(0);
            cellSub.setCellValue("Généré le : " +
                    java.time.LocalDate.now().format(FMT_DATE));
            CellStyle styleSub = wb.createCellStyle();
            styleSub.cloneStyleFrom(styleData);
            styleSub.setAlignment(HorizontalAlignment.RIGHT);
            XSSFFont fontSub = wb.createFont();
            fontSub.setItalic(true);
            fontSub.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            styleSub.setFont(fontSub);
            cellSub.setCellStyle(styleSub);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

            // ── Ligne vide de séparation ──────────────────────────────────
            sheet.createRow(2);

            // ── En-têtes de colonnes ──────────────────────────────────────
            String[] entetes = {
                    "N°", "Date", "Début", "Fin",
                    "Étudiant", "Sujet", "Encadrant", "Jury 1", "Jury 2", "Salle"
            };
            Row rowEntete = sheet.createRow(3);
            rowEntete.setHeightInPoints(28);
            for (int c = 0; c < entetes.length; c++) {
                Cell cell = rowEntete.createCell(c);
                cell.setCellValue(entetes[c]);
                cell.setCellStyle(styleEntete);
            }

            // Largeurs spécifiques
            sheet.setColumnWidth(0, 6 * 256);   // N°
            sheet.setColumnWidth(1, 14 * 256);  // Date
            sheet.setColumnWidth(2, 10 * 256);  // Début
            sheet.setColumnWidth(3, 10 * 256);  // Fin
            sheet.setColumnWidth(4, 24 * 256);  // Étudiant
            sheet.setColumnWidth(5, 40 * 256);  // Sujet
            sheet.setColumnWidth(6, 22 * 256);  // Encadrant
            sheet.setColumnWidth(7, 22 * 256);  // Jury 1
            sheet.setColumnWidth(8, 22 * 256);  // Jury 2
            sheet.setColumnWidth(9, 14 * 256);  // Salle

            // ── Données ───────────────────────────────────────────────────
            List<Soutenance> liste = planning.getSoutenances();

            // Tri par date puis heure de début
            liste = liste.stream()
                    .sorted(java.util.Comparator
                            .comparing(s -> s.getCreneau().getDate())
                    )
                    .sorted(java.util.Comparator
                            .comparing((Soutenance s) -> s.getCreneau().getDate())
                            .thenComparing(s -> s.getCreneau().getHeureDebut())
                    )
                    .toList();

            int ligneNum = 4;
            for (int i = 0; i < liste.size(); i++) {
                Soutenance s = liste.get(i);
                Row row = sheet.createRow(ligneNum++);
                row.setHeightInPoints(22);

                CellStyle style = (i % 2 == 0) ? styleData : styleDataAlt;

                ecrireCell(row, 0, String.valueOf(i + 1), style);
                ecrireCell(row, 1, s.getCreneau().getDate().format(FMT_DATE), style);
                ecrireCell(row, 2, s.getCreneau().getHeureDebut().format(FMT_HEURE), style);
                ecrireCell(row, 3, s.getCreneau().getHeureFin().format(FMT_HEURE), style);
                ecrireCell(row, 4, s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom(), style);
                ecrireCell(row, 5, s.getEtudiant().getSujet(), style);
                ecrireCell(row, 6, s.getEncadrant().getNom() + " " + s.getEncadrant().getPrenom(), style);
                ecrireCell(row, 7, s.getJury1().getNom() + " " + s.getJury1().getPrenom(), style);
                ecrireCell(row, 8, s.getJury2().getNom() + " " + s.getJury2().getPrenom(), style);
                ecrireCell(row, 9, s.getSalle().getNom(), style);
            }

            // ── Ligne total ───────────────────────────────────────────────
            Row rowTotal = sheet.createRow(ligneNum + 1);
            CellStyle styleTotal = wb.createCellStyle();
            styleTotal.cloneStyleFrom(styleEntete);
            styleTotal.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            styleTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Cell cellTotal = rowTotal.createCell(0);
            cellTotal.setCellValue("Total : " + liste.size() + " soutenance(s)");
            cellTotal.setCellStyle(styleTotal);
            sheet.addMergedRegion(new CellRangeAddress(ligneNum + 1, ligneNum + 1, 0, 9));

            // ── Figer la ligne d'en-tête ──────────────────────────────────
            sheet.createFreezePane(0, 4);

            // ── Écriture du fichier ───────────────────────────────────────
            ecrireFichier(wb, cheminSortie);
            System.out.println("✅ Planning exporté → " + cheminSortie
                    + " (" + liste.size() + " soutenances)");

        } catch (IOException e) {
            throw new RuntimeException("Erreur export planning : " + e.getMessage(), e);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  2. EXPORT FICHES INDIVIDUELLES
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void exporterFiches(List<Soutenance> soutenances, String cheminSortie) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            for (Soutenance s : soutenances) {
                String nomFeuille = sanitiserNomFeuille(
                        s.getEtudiant().getNom() + "_" + s.getEtudiant().getCne());
                XSSFSheet sheet = wb.createSheet(nomFeuille);
                sheet.setDefaultColumnWidth(30);
                sheet.setColumnWidth(0, 28 * 256);
                sheet.setColumnWidth(1, 45 * 256);

                ecrireFicheEtudiant(wb, sheet, s);
            }

            ecrireFichier(wb, cheminSortie);
            System.out.println("✅ Fiches exportées → " + cheminSortie
                    + " (" + soutenances.size() + " fiche(s))");

        } catch (IOException e) {
            throw new RuntimeException("Erreur export fiches : " + e.getMessage(), e);
        }
    }

    /**
     * Écrit la fiche d'une soutenance sur une feuille donnée.
     */
    private void ecrireFicheEtudiant(XSSFWorkbook wb, XSSFSheet sheet, Soutenance s) {

        CellStyle styleHeader = creerStyleFicheHeader(wb);
        CellStyle styleLabel  = creerStyleFicheLabel(wb);
        CellStyle styleValeur = creerStyleFicheValeur(wb);
        CellStyle styleSep    = creerStyleSeparateur(wb);

        int ligne = 0;

        // ── Titre ────────────────────────────────────────────────────────
        ligne = ajouterTitreFiche(sheet, wb, ligne,
                "FICHE DE SOUTENANCE", BLEU_TITRE);
        ligne++;

        // ── Infos étudiant ────────────────────────────────────────────────
        ligne = ajouterSectionHeader(sheet, styleHeader, ligne, "INFORMATIONS ÉTUDIANT");
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "CNE", s.getEtudiant().getCne());
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Nom complet", s.getEtudiant().getNom() + " " + s.getEtudiant().getPrenom());
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Filière", s.getEtudiant().getFiliere());
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Langue de soutenance", s.getEtudiant().getLangue());
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Sujet du projet", s.getEtudiant().getSujet());

        ligne = ajouterSeparateur(sheet, styleSep, ligne);

        // ── Infos créneau ─────────────────────────────────────────────────
        ligne = ajouterSectionHeader(sheet, styleHeader, ligne, "CRÉNEAU & SALLE");
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Date", s.getCreneau().getDate().format(FMT_DATE));
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Heure début", s.getCreneau().getHeureDebut().format(FMT_HEURE));
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Heure fin", s.getCreneau().getHeureFin().format(FMT_HEURE));
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Salle", s.getSalle().getNom() + " (ID: " + s.getSalle().getId() + ")");

        ligne = ajouterSeparateur(sheet, styleSep, ligne);

        // ── Jury ──────────────────────────────────────────────────────────
        ligne = ajouterSectionHeader(sheet, styleHeader, ligne, "COMPOSITION DU JURY");
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Encadrant", s.getEncadrant().getNom() + " " + s.getEncadrant().getPrenom()
                        + " (" + s.getEncadrant().getDepartement() + ")");
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Jury 1 (Président)", s.getJury1().getNom() + " " + s.getJury1().getPrenom()
                        + " (" + s.getJury1().getDepartement() + ")");
        ligne = ajouterLigne(sheet, styleLabel, styleValeur, ligne,
                "Jury 2 (Examinateur)", s.getJury2().getNom() + " " + s.getJury2().getPrenom()
                        + " (" + s.getJury2().getDepartement() + ")");

        ligne = ajouterSeparateur(sheet, styleSep, ligne);

        // ── Zone de notation ──────────────────────────────────────────────
        ligne = ajouterSectionHeader(sheet, styleHeader, ligne, "ÉVALUATION");

        CellStyle styleNoteLbl = creerStyleFicheLabel(wb);
        CellStyle styleNoteVal = wb.createCellStyle();
        styleNoteVal.cloneStyleFrom(styleValeur);
        styleNoteVal.setBorderBottom(BorderStyle.MEDIUM);

        String[] criteres = {
                "Note Rapport (/20)",
                "Note Présentation (/20)",
                "Note Soutenance (/20)",
                "Appréciation générale"
        };
        for (String c : criteres) {
            Row row = sheet.createRow(ligne++);
            row.setHeightInPoints(24);
            Cell lbl = row.createCell(0);
            lbl.setCellValue(c);
            lbl.setCellStyle(styleNoteLbl);
            Cell val = row.createCell(1);
            val.setCellValue(""); // à remplir manuellement
            val.setCellStyle(styleNoteVal);
        }

        ligne = ajouterSeparateur(sheet, styleSep, ligne);

        // ── Signature ─────────────────────────────────────────────────────
        Row rowSig = sheet.createRow(ligne + 1);
        rowSig.setHeightInPoints(20);
        CellStyle styleSigLbl = creerStyleFicheLabel(wb);
        Cell sig1 = rowSig.createCell(0);
        sig1.setCellValue("Signature de l'encadrant");
        sig1.setCellStyle(styleSigLbl);
        Cell sig2 = rowSig.createCell(1);
        sig2.setCellValue("Signature du Président du Jury");
        sig2.setCellStyle(styleSigLbl);

        // Espace signature
        sheet.createRow(ligne + 2).setHeightInPoints(48);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS — lignes de la fiche
    // ════════════════════════════════════════════════════════════════════════

    private int ajouterTitreFiche(XSSFSheet sheet, XSSFWorkbook wb, int ligne, String texte, String couleurARGB) {
        Row row = sheet.createRow(ligne);
        row.setHeightInPoints(40);
        Cell cell = row.createCell(0);
        cell.setCellValue(texte);
        sheet.addMergedRegion(new CellRangeAddress(ligne, ligne, 0, 1));

        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRGB(couleurARGB), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(new XSSFColor(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF}, null));
        style.setFont(font);
        cell.setCellStyle(style);
        return ligne + 1;
    }

    private int ajouterSectionHeader(XSSFSheet sheet, CellStyle style, int ligne, String titre) {
        Row row = sheet.createRow(ligne);
        row.setHeightInPoints(24);
        Cell cell = row.createCell(0);
        cell.setCellValue(titre);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(ligne, ligne, 0, 1));
        return ligne + 1;
    }

    private int ajouterLigne(XSSFSheet sheet, CellStyle styleLabel, CellStyle styleValeur,
                             int ligne, String label, String valeur) {
        Row row = sheet.createRow(ligne);
        row.setHeightInPoints(20);
        Cell cellLabel = row.createCell(0);
        cellLabel.setCellValue(label);
        cellLabel.setCellStyle(styleLabel);
        Cell cellVal = row.createCell(1);
        cellVal.setCellValue(valeur != null ? valeur : "");
        cellVal.setCellStyle(styleValeur);
        return ligne + 1;
    }

    private int ajouterSeparateur(XSSFSheet sheet, CellStyle style, int ligne) {
        Row row = sheet.createRow(ligne);
        row.setHeightInPoints(6);
        Cell cell = row.createCell(0);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(ligne, ligne, 0, 1));
        return ligne + 1;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS — styles
    // ════════════════════════════════════════════════════════════════════════

    private CellStyle creerStyleTitre(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRGB(BLEU_TITRE), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null));
        style.setFont(font);
        return style;
    }

    private CellStyle creerStyleEntete(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRGB(BLEU_ENTETE), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        bordures(style, BorderStyle.THIN, GRIS_BORD);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null));
        style.setFont(font);
        return style;
    }

    private CellStyle creerStyleData(XSSFWorkbook wb, boolean altRow) {
        XSSFCellStyle style = wb.createCellStyle();
        if (altRow) {
            style.setFillForegroundColor(new XSSFColor(hexToRGB(BLEU_CLAIR), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        bordures(style, BorderStyle.THIN, GRIS_BORD);
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle creerStyleFicheHeader(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRGB(BLEU_ENTETE), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null));
        style.setFont(font);
        bordures(style, BorderStyle.THIN, GRIS_BORD);
        return style;
    }

    private CellStyle creerStyleFicheLabel(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRGB(BLEU_CLAIR), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        bordures(style, BorderStyle.THIN, GRIS_BORD);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle creerStyleFicheValeur(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        bordures(style, BorderStyle.THIN, GRIS_BORD);
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle creerStyleSeparateur(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToRGB(BLEU_TITRE), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS — utilitaires
    // ════════════════════════════════════════════════════════════════════════

    private void ecrireCell(Row row, int col, String valeur, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(valeur != null ? valeur : "");
        cell.setCellStyle(style);
    }

    private void bordures(CellStyle style, BorderStyle bs, String couleurARGB) {
        style.setBorderTop(bs);
        style.setBorderBottom(bs);
        style.setBorderLeft(bs);
        style.setBorderRight(bs);
        if (style instanceof XSSFCellStyle xs) {
            XSSFColor c = new XSSFColor(hexToRGB(couleurARGB), null);
            xs.setTopBorderColor(c);
            xs.setBottomBorderColor(c);
            xs.setLeftBorderColor(c);
            xs.setRightBorderColor(c);
        }
    }

    /**
     * Convertit un code couleur ARGB hex (ex: "FF1E3A5F") en tableau de bytes RGB.
     */
    private byte[] hexToRGB(String argb) {
        // argb = "FFrrggbb"
        int r = Integer.parseInt(argb.substring(2, 4), 16);
        int g = Integer.parseInt(argb.substring(4, 6), 16);
        int b = Integer.parseInt(argb.substring(6, 8), 16);
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }

    /**
     * Nettoie le nom d'une feuille Excel (max 31 chars, pas de caractères spéciaux).
     */
    private String sanitiserNomFeuille(String nom) {
        String clean = nom.replaceAll("[\\\\/*?\\[\\]:]", "_");
        return clean.length() > 31 ? clean.substring(0, 31) : clean;
    }

    private void ecrireFichier(XSSFWorkbook wb, String chemin) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(chemin)) {
            wb.write(fos);
        }
    }
}
