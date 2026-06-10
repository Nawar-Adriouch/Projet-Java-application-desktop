package ui;
import  Exceptions.*;
import model.*;
import repository.ExcelReaderImpl;
import repository.PdfPlanningWriter;
import repository.PdfAffectationWriter;
import repository.PdfFicheWriter;
import service.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import config.OutputConfig;


public class AccueilPanel extends JPanel {

    //Palette identique a AffectationPanel
    private static final Color BG          = new Color(0xF5F7FA);
    private static final Color VERT_FONCE  = new Color(0x1B5E20);
    private static final Color VERT_MOY    = new Color(0x2E7D32);
    private static final Color VERT_CLAIR  = new Color(0xE8F5E9);
    private static final Color JAUNE       = new Color(0xF9A825);
    private static final Color GRIS        = new Color(0x546E7A);
    private static final Color ROUGE       = new Color(0xC62828);
    private static final Color BLEU        = new Color(0x1565C0);
    private static final Color BLANC       = Color.WHITE;
    private static final Color BORD        = new Color(0xC8E6C9);

    private JTextField    champFichier;
    private JTextField    champDate;
    private JSpinner      spinnerJours;
    private JButton       btnGenerer;
    private JButton       btnDlPlanning;
    private JButton       btnDlAffectation;
    private JPanel        panelDl;
    private JLabel        labelStatut;
    private JProgressBar  progress;

    private Planning                       planning;
    private Map<Encadrant, List<Etudiant>> affectation;
    private final Runnable onGenere;

    public AccueilPanel(Runnable onGenere) {
        this.onGenere = onGenere;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContenu(), BorderLayout.CENTER);
    }

    public Planning                       getPlanning()    { return planning; }
    public Map<Encadrant, List<Etudiant>> getAffectation() { return affectation; }


    //  HEADER

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(VERT_FONCE);
        p.setBorder(new EmptyBorder(20, 28, 20, 28));

        JLabel titre = new JLabel("Planificateur de Soutenances");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titre.setForeground(BLANC);

        JLabel sub = new JLabel("Importez vos données Excel, configurez et générez le planning");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(0xA5D6A7));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(titre);
        texts.add(Box.createVerticalStrut(4));
        texts.add(sub);

        p.add(texts, BorderLayout.WEST);
        return p;
    }


    //  CONTENU PRINCIPAL

    private JScrollPane buildContenu() {
        JPanel contenu = new JPanel();
        contenu.setBackground(BG);
        contenu.setLayout(new BoxLayout(contenu, BoxLayout.Y_AXIS));
        contenu.setBorder(new EmptyBorder(24, 36, 24, 36));

        contenu.add(buildCarteInputs());
        contenu.add(Box.createVerticalStrut(18));
        contenu.add(buildCarteBoutonGenerer());
        contenu.add(Box.createVerticalStrut(18));
        panelDl = buildCarteDl();
        panelDl.setVisible(false);
        contenu.add(panelDl);

        JScrollPane scroll = new JScrollPane(contenu);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        return scroll;
    }

    //  CARTE 1  Inputs (fichier + date + nb jours)
    private JPanel buildCarteInputs() {
        JPanel carte = carte();
        carte.setLayout(new BorderLayout(0, 16));
        carte.add(titreSection("Paramètres d'entrée"), BorderLayout.NORTH);

        JPanel grille = new JPanel(new GridBagLayout());
        grille.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.anchor = GridBagConstraints.WEST;

        // Ligne 1 : fichier Excel
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        grille.add(label("Fichier Excel :"), g);

        champFichier = new JTextField("Aucun fichier sélectionné...");
        champFichier.setEditable(false);
        champFichier.setForeground(new Color(0x9E9E9E));
        champFichier.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        champFichier.setBackground(BLANC);
        champFichier.setBorder(new CompoundBorder(
                new LineBorder(BORD, 1, true), new EmptyBorder(6, 10, 6, 10)));
        champFichier.setPreferredSize(new Dimension(420, 36));

        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        grille.add(champFichier, g);

        JButton btnBrowse = bouton("Parcourir", VERT_MOY, BLANC, 130, 36);
        btnBrowse.addActionListener(e -> parcourir());
        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        grille.add(btnBrowse, g);

        //  Ligne 2  date de début
        g.gridx = 0; g.gridy = 1;
        grille.add(label("Date 1ère soutenance :"), g);

        champDate = new JTextField(
                String.format("%02d/%02d/%04d",
                        LocalDate.now().plusDays(7).getDayOfMonth(),
                        LocalDate.now().plusDays(7).getMonthValue(),
                        LocalDate.now().plusDays(7).getYear()));
        champDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        champDate.setHorizontalAlignment(JTextField.CENTER);
        champDate.setBorder(new CompoundBorder(
                new LineBorder(BORD, 1, true), new EmptyBorder(6, 10, 6, 10)));
        champDate.setPreferredSize(new Dimension(140, 36));

        JLabel hintDate = new JLabel("  format JJ/MM/AAAA");
        hintDate.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintDate.setForeground(GRIS);

        JPanel panDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panDate.setOpaque(false);
        panDate.add(champDate);
        panDate.add(hintDate);

        g.gridx = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        grille.add(panDate, g);
        g.gridwidth = 1;

        // Ligne 3 nombre de jours
        g.gridx = 0; g.gridy = 2; g.fill = GridBagConstraints.NONE;
        grille.add(label("Nombre de jours :"), g);

        spinnerJours = new JSpinner(new SpinnerNumberModel(3, 1, 60, 1));
        spinnerJours.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spinnerJours.setPreferredSize(new Dimension(80, 36));
        ((JSpinner.DefaultEditor) spinnerJours.getEditor())
                .getTextField().setHorizontalAlignment(JTextField.CENTER);

        JLabel hintJours = new JLabel("  jours de soutenances");
        hintJours.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintJours.setForeground(GRIS);

        JPanel panJours = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panJours.setOpaque(false);
        panJours.add(spinnerJours);
        panJours.add(hintJours);

        g.gridx = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        grille.add(panJours, g);

        carte.add(grille, BorderLayout.CENTER);
        return carte;
    }

    //  CARTE 2  Bouton Générer + statut + progress

    private JPanel buildCarteBoutonGenerer() {
        JPanel carte = carte();
        carte.setLayout(new BorderLayout(0, 12));
        carte.add(titreSection("Génération"), BorderLayout.NORTH);

        // Barre de progression
        progress = new JProgressBar();
        progress.setForeground(VERT_MOY);
        progress.setBackground(new Color(0xE0E0E0));
        progress.setBorder(null);
        progress.setPreferredSize(new Dimension(0, 8));
        progress.setVisible(false);

        // Label statut
        labelStatut = new JLabel(" ");
        labelStatut.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        labelStatut.setForeground(GRIS);

        // Bouton
        btnGenerer = bouton("Générer le Planning", JAUNE, VERT_FONCE, 230, 48);
        btnGenerer.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnGenerer.addActionListener(e -> generer());

        JPanel centreBouton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centreBouton.setOpaque(false);
        centreBouton.add(btnGenerer);

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.add(progress);
        centre.add(Box.createVerticalStrut(6));
        centre.add(labelStatut);
        centre.add(Box.createVerticalStrut(10));
        centre.add(centreBouton);

        carte.add(centre, BorderLayout.CENTER);
        return carte;
    }


    //  CARTE 3  Téléchargement

    private JPanel buildCarteDl() {
        JPanel carte = carte();
        carte.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x90CAF9), 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        carte.setLayout(new BorderLayout(0, 14));
        carte.add(titreSection(" Télécharger les résultats", BLEU), BorderLayout.NORTH);

        // Bouton 1 : Planning PDF
        btnDlPlanning = bouton("Planning .pdf",
                new Color(0x1565C0), BLANC, 200, 46);
        btnDlPlanning.addActionListener(e -> telechargerPlanning());

        // Bouton 2 : Affectation PDF
        btnDlAffectation = bouton("Affectation .pdf",
                new Color(0x2E7D32), BLANC, 200, 46);
        btnDlAffectation.addActionListener(e -> telechargerAffectation());

        // Bouton 3 : Fiches PDF (dossier)
        JButton btnFiches = bouton("Fiches évaluation (dossier)",
                new Color(0x6A1B9A), BLANC, 230, 46);
        btnFiches.addActionListener(e -> telechargerFiches());

        // Bouton 4 : Tout télécharger
        JButton btnTout = bouton("Tout télécharger", new Color(0xE65100), BLANC, 190, 46);
        btnTout.addActionListener(e -> {
            telechargerPlanning();
            telechargerAffectation();
            telechargerFiches();
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        row.setOpaque(false);
        row.add(btnDlPlanning);
        row.add(btnDlAffectation);
        row.add(btnFiches);
        row.add(btnTout);

        carte.add(row, BorderLayout.CENTER);
        return carte;
    }

    //  ACTIONS

    private void parcourir() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Sélectionner le fichier Excel");
        fc.setFileFilter(new FileNameExtensionFilter("Fichiers Excel *.xlsx", "xlsx"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            champFichier.setText(fc.getSelectedFile().getAbsolutePath());
            champFichier.setForeground(Color.BLACK);
            panelDl.setVisible(false);
            planning = null;
            statut("Fichier sélectionné : " + fc.getSelectedFile().getName(), VERT_MOY);
        }
    }

    private void generer() {

        // Validation champs
        String chemin = champFichier.getText().trim();
        if (chemin.isBlank() || chemin.startsWith("Aucun")) {
            statut("Veuillez sélectionner un fichier Excel.", ROUGE);
            return;
        }

        LocalDate dateDebut;
        try {
            String[] p = champDate.getText().trim().split("/");
            dateDebut = LocalDate.of(
                    Integer.parseInt(p[2]),
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[0]));
        } catch (Exception ex) {
            statut("Date invalide — format JJ/MM/AAAA", ROUGE);
            return;
        }

        int nbJours = (int) spinnerJours.getValue();
        final LocalDate date = dateDebut;

        planning    = null;
        affectation = null;
        panelDl.setVisible(false);
        statut("Lecture du fichier et vérification...", GRIS);


        int nbEtudiants;
        int nbSalles;
        try {
            ExcelReaderImpl readerCheck = new ExcelReaderImpl(chemin);
            nbEtudiants = readerCheck.lireEtudiants().size();
            nbSalles    = readerCheck.lireSalles().size();
        } catch (Exception ex) {
            statut("Impossible de lire le fichier : " + ex.getMessage(), ROUGE);
            return;
        }

        try {
            new GenerateurCreneaux().verifierCpacite(nbJours, nbEtudiants, nbSalles);
        } catch (nombreJoursInvalideException e) {

            String msg = e.getMessage();

            statut(" Insuffisant", ROUGE);

            JOptionPane.showMessageDialog(this, msg,
                    "Capacité insuffisante", JOptionPane.WARNING_MESSAGE);

            return;
        }


        btnGenerer.setEnabled(false);
        progress.setIndeterminate(true);
        progress.setVisible(true);

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    publish("Lecture du fichier Excel");
                    ExcelReaderImpl reader = new ExcelReaderImpl(chemin);
                    List<Etudiant>   etudiants = reader.lireEtudiants();
                    List<Professeur> profs     = reader.lireProfesseurs();
                    List<Salle>      salles    = reader.lireSalles();

                    publish("Affectation des étudiants...");
                    affectation = new Affectation().affectation(etudiants, profs);

                    publish("Génération des créneaux (" + nbJours + " jour(s))...");
                    List<Creneau> creneaux = new GenerateurCreneaux().generer(date, nbJours);

                    publish("Planification en cours ");
                    planning = new Planificateur(creneaux, salles, profs)
                            .plannifier(affectation);

                } catch (Exception ex) {
                    planning = null;
                    publish("ERR:" + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                String msg = chunks.get(chunks.size() - 1);
                if (msg.startsWith("ERR:")) statut(" " + msg.substring(4), ROUGE);
                else                        statut(msg, GRIS);
            }

            @Override
            protected void done() {
                btnGenerer.setEnabled(true);
                progress.setIndeterminate(false);
                progress.setVisible(false);

                if (planning != null && !planning.getSoutenances().isEmpty()) {
                    int nb = planning.getSoutenances().size();
                    statut( nb + " soutenances planifiées  •  "
                            + affectation.size() + " encadrants", VERT_MOY);
                    panelDl.setVisible(true);
                    if (onGenere != null) onGenere.run();
                } else {
                    statut("Aucune soutenance planifiée, Vérifiez les données.", ROUGE);
                }
                revalidate();
                repaint();
            }
        }.execute();
    }

    private void telechargerPlanning() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Enregistrer le planning PDF");
        fc.setFileFilter(new FileNameExtensionFilter("PDF", "pdf"));
        fc.setSelectedFile(new File(OutputConfig.planningPdf()));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String dest = fc.getSelectedFile().getAbsolutePath();
        if (!dest.endsWith(".pdf")) dest += ".pdf";
        final String chemin = dest;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                new PdfPlanningWriter().exporter(planning, chemin);
                return null;
            }
            @Override protected void done() {
                try { get();
                    JOptionPane.showMessageDialog(AccueilPanel.this,
                            "Planning PDF enregistré :\n" + chemin,
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AccueilPanel.this,
                            "Erreur :\n" + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void telechargerAffectation() {
        // verification que l'affectation existe
        if (affectation == null || affectation.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    " Aucune affectation disponible.\nGénérez d'abord le planning.",
                    "Affectation manquante", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Enregistrer l'affectation PDF...");
        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        fc.setSelectedFile(new File(OutputConfig.affectationPdf()));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String dest = fc.getSelectedFile().getAbsolutePath();
        if (!dest.endsWith(".pdf")) dest += ".pdf";
        final String chemin = dest;

        final Map<Encadrant, List<Etudiant>> affectationCopie = new LinkedHashMap<>(affectation);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {

                new PdfAffectationWriter().exporter(affectationCopie, chemin);
                return null;
            }
            @Override protected void done() {
                try { get();
                    JOptionPane.showMessageDialog(AccueilPanel.this,
                            "Affectation PDF enregistrée :\n" + chemin,
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AccueilPanel.this,
                            "Erreur :\n" + (ex.getCause() != null
                                    ? ex.getCause().getMessage() : ex.getMessage()),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void telechargerFiches() {
        //  Verification planning avant tout
        if (planning == null || planning.getSoutenances().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucun planning généré.\nCliquez d'abord sur 'Générer le Planning'.",
                    "Planning manquant", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Soutenances disponibles : " + planning.getSoutenances().size());

        // Sélectionner un DOSSIER
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choisir le dossier de destination pour les fiches");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        final String dossier = OutputConfig.dossierFiches();


        final List<Soutenance> soutenances = new ArrayList<>(planning.getSoutenances());

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return new PdfFicheWriter().exporterFiches(soutenances, dossier);
            }
            @Override
            protected void done() {
                try {
                    List<String> fichiers = get();
                    JOptionPane.showMessageDialog(AccueilPanel.this,
                             fichiers.size() + " fiches PDF créées dans :\n" + dossier,
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(AccueilPanel.this,
                            "Erreur :\n" + cause.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }


    private void statut(String msg, Color c) {
        labelStatut.setText("  " + msg);
        labelStatut.setForeground(c);
    }

    private JPanel carte() {
        JPanel p = new JPanel();
        p.setBackground(BLANC);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORD, 1, true),
                new EmptyBorder(18, 22, 18, 22)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel titreSection(String txt) {
        return titreSection(txt, VERT_FONCE);
    }

    private JLabel titreSection(String txt, Color c) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(c);
        l.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
                new EmptyBorder(0, 0, 10, 0)));
        return l;
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(GRIS);
        l.setBorder(new EmptyBorder(0, 0, 0, 12));
        return l;
    }

    private JButton bouton(String txt, Color bg, Color fg, int w, int h) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color col = getModel().isPressed() ? bg.darker()
                        : getModel().isRollover()  ? bg.brighter() : bg;
                g2.setColor(col);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }
}