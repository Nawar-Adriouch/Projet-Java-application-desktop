package ui;

import model.Encadrant;
import model.Etudiant;
import model.Planning;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;


public class MainWindow extends JFrame {

    private AccueilPanel     accueilPanel;
    private AffectationPanel affectationPanel;
    private PlanningPanel    planningPanel;
    private StatsPanel       statsPanel;
    private JTabbedPane      tabs;

    public MainWindow() {
        super("Gestion de soutenances");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        accueilPanel     = new AccueilPanel(this::onPlanningGenere);
        affectationPanel = new AffectationPanel();
        planningPanel    = new PlanningPanel();
        statsPanel       = new StatsPanel();

        tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("Accueil",       accueilPanel);
        tabs.addTab("Affectation",   affectationPanel);
        tabs.addTab("Planning",      planningPanel);
        tabs.addTab("Statistiques",  statsPanel);

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    private void onPlanningGenere() {
        Planning planning                      = accueilPanel.getPlanning();
        Map<Encadrant, List<Etudiant>> affect  = accueilPanel.getAffectation();

        if (affect   != null) affectationPanel.setAffectation(affect);
        if (planning != null) planningPanel.setPlanning(planning);
        if (planning != null) statsPanel.setPlanning(planning);

        tabs.setSelectedIndex(2);
    }

    public void setPlanning(Planning planning) {
        planningPanel.setPlanning(planning);
        statsPanel.setPlanning(planning);
    }

    public void setAffectation(Map<Encadrant, List<Etudiant>> affectation) {
        affectationPanel.setAffectation(affectation);
    }
}