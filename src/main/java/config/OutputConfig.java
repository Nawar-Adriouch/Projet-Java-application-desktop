package config;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class OutputConfig {

    // Dossier racine
    private static final String RACINE = System.getProperty("user.home")
            + File.separator + "Documents"
            + File.separator + "Soutenances";

    private static final String SOUS_DOSSIER =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    // Dossier complet de la session
    private static final String DOSSIER_SESSION =
            RACINE + File.separator + SOUS_DOSSIER;



    // Chemin complet du planning PDF
    public static String planningPdf() {
        return dossierSession() + File.separator + "planning_soutenances.pdf";
    }

    // Chemin complet du tableau d'affectation PDF
    public static String affectationPdf() {
        return dossierSession() + File.separator + "affectation_encadrants.pdf";
    }

    // Dossier des fiches d'évaluation PDF (une par étudiant)
    public static String dossierFiches() {
        return dossierSession() + File.separator + "Fiches_Soutenances";
    }

    //Dossier de la session courante — créé automatiquement si inexistant
    public static String dossierSession() {
        File f = new File(DOSSIER_SESSION);
        if (!f.exists()) f.mkdirs();
        return DOSSIER_SESSION;
    }

    // Dossier racine — créé automatiquement si inexistant
    public static String dossierRacine() {
        File f = new File(RACINE);
        if (!f.exists()) f.mkdirs();
        return RACINE;
    }


    public static void afficherConfig() {

        System.out.println("  Dossier session : " + dossierSession());
        System.out.println("  Planning PDF    : " + planningPdf());
        System.out.println("  Affectation PDF : " + affectationPdf());
        System.out.println("  Fiches dossier  : " + dossierFiches());

    }
}