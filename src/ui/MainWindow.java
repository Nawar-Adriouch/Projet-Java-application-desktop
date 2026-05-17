package ui;

import model.Encadrant;
import model.Etudiant;
import model.Planning;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {

    // ── Déclaration des panels (niveau classe, pas dans le constructeur)
    private StatsPanel statsPanel;
    private AffectationPanel affectationPanel;

    public MainWindow() {
        super("Gestion de soutenances - Menu principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        // ── Initialisation des panels
        statsPanel       = new StatsPanel();
        affectationPanel = new AffectationPanel();

        // ── Onglets
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.add("Affectation",  affectationPanel);
        tabs.add("Statistiques", statsPanel);
        // PlanningPanel sera ajouté plus tard

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    // ── Méthodes publiques appelées depuis Main.java ──────────

    public void setPlanning(Planning planning) {
        statsPanel.setPlanning(planning);
    }

    public void setAffectation(Map<Encadrant, List<Etudiant>> affectation) {
        affectationPanel.setAffectation(affectation);
    }

    // ── Point d'entrée (optionnel, le vrai main est dans Main.java)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}