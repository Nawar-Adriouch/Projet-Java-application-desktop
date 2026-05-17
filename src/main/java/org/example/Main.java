package org.example;

import repository.ExcelReaderImpl;
import service.*;
import java.time.LocalDate;
import java.util.*;
import ui.*;
import repository.ExcelReaderImpl;
import model.Creneau;
import java.time.LocalDate;
import java.util.*;
import model.*;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // ── 1. Lecture du fichier Excel ───────────────────────────────────────
        String chemin = "C:\\Users\\nawar\\IdeaProjects\\JAVA\\src\\ui\\SHETTS (1).xlsx";
        ExcelReaderImpl reader = new ExcelReaderImpl(chemin);

        List<Etudiant>   etudiants = reader.lireEtudiants();
        List<Professeur> profs     = reader.lireProfesseurs();
        List<Salle>      salles    = reader.lireSalles();

        System.out.println("===== ETUDIANTS =====");
        for (Etudiant e : etudiants) System.out.println(e);

        System.out.println("\n===== PROFESSEURS =====");
        for (Professeur p : profs) System.out.println(p);

        System.out.println("\n===== SALLES =====");
        for (Salle s : salles) System.out.println(s);

        // ── 2. Affectation ───────────────────────────────────────────────────
        Affectation service = new Affectation();
        Map<Encadrant, List<Etudiant>> resultat = service.affectation(etudiants, profs);

        System.out.println("\n===== AFFECTATION =====");
        for (Map.Entry<Encadrant, List<Etudiant>> entry : resultat.entrySet()) {
            Encadrant enc = entry.getKey();
            int nbEtudiants = entry.getValue().size(); // ← charge RÉELLE de cet encadrant
            System.out.println("\nEncadrant : " + enc.getNom() + " " + enc.getPrenom()
                    + " (" + nbEtudiants + " étudiant(s))");
            for (Etudiant e : entry.getValue()) {
                System.out.println("   -> " + e.getNom() + " " + e.getPrenom());
            }
        }

        // ── 3. Génération des créneaux ────────────────────────────────────────
        GenerateurCreneaux generateur = new GenerateurCreneaux();
        List<Creneau> creneaux = generateur.generer(LocalDate.of(2026, 6, 4), 3);
        System.out.println("\nNombre de créneaux générés : " + creneaux.size());

        // ── 4. Planification ─────────────────────────────────────────────────
        Planificateur plan = new Planificateur(creneaux, salles, profs);
        Planning planning = plan.plannifier(resultat);

        // ── 5. Test jury (avec un encadrant tiré directement de la map) ───────
        Etudiant etudiantTest = etudiants.get(0);

        // On prend le premier encadrant directement depuis la map d'affectation
        // sa charge réelle = resultat.get(encadrantTest).size()
        Encadrant encadrantTest = resultat.keySet().iterator().next();

        List<Professeur> jurys = plan.chercherJuryEquitable(
                etudiantTest, creneaux.get(0), encadrantTest);

        System.out.println("\n===== TEST JURY =====");
        System.out.println("Etudiant : " + etudiantTest.getNom());
        System.out.println("Encadrant : " + encadrantTest.getNom()
                + " | étudiants encadrés : " + resultat.get(encadrantTest).size());
        for (Professeur j : jurys) {
            System.out.println("Jury : " + j.getNom());
        }

        // ── 6. Affichage planning trié ────────────────────────────────────────
        System.out.println("\n===== PLANNING FINAL =====");
        List<Soutenance> soutenances = new ArrayList<>(planning.getSoutenances());
        soutenances.sort(Comparator
                .comparing((Soutenance s) -> s.getCreneau().getDate())
                .thenComparing(s -> s.getCreneau().getHeureDebut())
        );
        for (Soutenance s : soutenances) System.out.println(s);

        System.out.println("\nTotal soutenances : " + planning.getSoutenances().size());

        // ── 7. Stats ──────────────────────────────────────────────────────────
        StatsPlanning.analyser(planning);

        // Étudiants non planifiés
        List<Etudiant> planifies = new ArrayList<>();
        for (Soutenance s : planning.getSoutenances()) planifies.add(s.getEtudiant());
        System.out.println("\n===== ÉTUDIANTS NON PLANIFIÉS =====");
        for (Etudiant e : etudiants) {
            boolean trouve = planifies.stream()
                    .anyMatch(ep -> ep.getCne().equals(e.getCne()));
            if (!trouve) System.out.println(e.getNom() + " | " + e.getLangue());
        }

        // ── 8. Validation ─────────────────────────────────────────────────────
        System.out.println("\n===== VALIDATION =====");
        List<Soutenance> listeSoutenances = planning.getSoutenances();
        if (ValidationPlanning.verifierTout(listeSoutenances)) {
            System.out.println("Planning OK");
        } else {
            System.out.println("Planning NON valide");
        }

        // ── 9. Fiches d'évaluation ────────────────────────────────────────────
        GenerateurFiche gen = new GenerateurFiche();
        List<FicheEvaluation> fiches = gen.generer(planning);
        for (FicheEvaluation f : fiches) System.out.println(f);

        // ── 10. Interface graphique ────────────────────────────────────────────
        final Planning planningFinal = planning;
        final Map<Encadrant, List<Etudiant>> affectationFinale = resultat;

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setAffectation(affectationFinale);
            window.setPlanning(planningFinal);
        });
    }
}
