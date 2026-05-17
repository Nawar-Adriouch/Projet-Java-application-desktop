package repository;
import java.util.*;

import model.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelReaderImpl implements ExcelReader {

    private final String cheminFichier;


    private List<Etudiant>   etudiants;
    private List<Professeur> professeurs;
    private List<Salle>      salles;
    private boolean          deja_charge = false;

    public ExcelReaderImpl(String cheminFichier) {
        if (cheminFichier == null || cheminFichier.isBlank())
            throw new IllegalArgumentException("Chemin du fichier invalide");
        this.cheminFichier = cheminFichier;
    }

    private void chargerSiNecessaire() {
        if (deja_charge) return;

        try (FileInputStream fis = new FileInputStream(cheminFichier);
             Workbook wb = new XSSFWorkbook(fis)) {

            this.etudiants   = chargerEtudiants(wb);
            this.professeurs = chargerProfesseurs(wb);
            this.salles      = chargerSalles(wb);
            this.deja_charge = true;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier Excel : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Etudiant> lireEtudiants() {
        chargerSiNecessaire();
        return etudiants;
    }

    @Override
    public List<Professeur> lireProfesseurs() {
        chargerSiNecessaire();
        return professeurs;
    }

    @Override
    public List<Salle> lireSalles() {
        chargerSiNecessaire();
        return salles;
    }



    private List<Etudiant> chargerEtudiants(Workbook wb) {
        List<Etudiant> liste = new ArrayList<>();
        Sheet sheet = wb.getSheetAt(2);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String cne     = cellString(row, 0);
            String nom     = cellString(row, 1);
            String prenom  = cellString(row, 2);
            String filiere = cellString(row, 3);
            String sujet   = cellString(row, 4);
            String langue  = cellString(row, 5);
            if (cne.isBlank()) continue;
            liste.add(new Etudiant(cne, nom, prenom, filiere, sujet, langue));
        }
        return liste;
    }

    private List<Professeur> chargerProfesseurs(Workbook wb) {
        List<Professeur> liste = new ArrayList<>();
        Sheet sheet = wb.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String id          = cellString(row, 0);
            String nom         = cellString(row, 1);
            String prenom      = cellString(row, 2);
            String departement = cellString(row, 3);
            String specialite  = cellString(row, 4);
            if (id.isBlank()) continue;
            liste.add(new Professeur(id, nom, prenom, departement, specialite));
        }
        return liste;
    }

    private List<Salle> chargerSalles(Workbook wb) {
        List<Salle> liste = new ArrayList<>();
        Sheet sheet = wb.getSheetAt(1);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String id  = cellString(row, 0);
            String nom = cellString(row, 1);
            if (id.isBlank()) continue;
            liste.add(new Salle(id, nom));
        }
        return liste;
    }

    private String cellString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield (v == Math.floor(v)) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default      -> "";
        };
    }
}


