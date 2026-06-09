package ui;
import model.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class PlanningPanel extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────
    private static final Color BG         = new Color(0xF5F7FA);
    private static final Color HEADER_BG  = new Color(0x1B5E20);
    private static final Color HEADER_FG  = Color.WHITE;
    private static final Color ROW_EVEN   = new Color(0xE8F5E9);
    private static final Color ROW_ODD    = Color.WHITE;
    private static final Color HOVER      = new Color(0xFFE082);
    private static final Color GRID       = new Color(0xD6D6D6);
    private static final Color COL_HEADER = new Color(0x2E7D32);

    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    // ── Colonnes ──────────────────────────────────────────────────────────
    private static final String[] COLS = {
            "N°", "Date", "Début", "Fin",
            "Étudiant", "Filière", "Sujet",
            "Encadrant", "Jury 1", "Jury 2", "Salle"
    };
    private static final int[] WIDTHS = {
            40, 105, 65, 65,
            155, 95, 210,
            155, 155, 155, 75
    };

    // ── Composants ────────────────────────────────────────────────────────
    private DefaultTableModel           tableModel;
    private JTable                      table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel                      infoLabel;
    private JTextField                  champRecherche;
    private int                         hoveredRow = -1;

    // ═════════════════════════════════════════════════════════════════════
    public PlanningPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildCard(),       BorderLayout.CENTER);
        add(buildFooter(),     BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new RoundedPanel(25);
        p.setLayout(new BorderLayout(16, 0));
        p.setBackground(HEADER_BG);
        p.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel titre = new JLabel("📅  Planning des Soutenances");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titre.setForeground(HEADER_FG);

        infoLabel = new JLabel("—");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(0xFFF59D));

        // Barre de recherche
        champRecherche = new JTextField(18);
        champRecherche.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        champRecherche.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xA5D6A7), 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        champRecherche.setPreferredSize(new Dimension(200, 32));

        // placeholder manuel
        champRecherche.setForeground(Color.GRAY);
        champRecherche.setText(" Rechercher...");
        champRecherche.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (champRecherche.getText().equals(" Rechercher...")) {
                    champRecherche.setText("");
                    champRecherche.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (champRecherche.getText().isBlank()) {
                    champRecherche.setForeground(Color.GRAY);
                    champRecherche.setText(" Rechercher...");
                    if (sorter != null) sorter.setRowFilter(null);
                }
            }
        });
        champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filtrer(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filtrer(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        droite.setOpaque(false);
        droite.add(champRecherche);
        droite.add(infoLabel);

        p.add(titre,  BorderLayout.WEST);
        p.add(droite, BorderLayout.EAST);
        return p;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  CARD + TABLE
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildCard() {
        JPanel card = new RoundedPanel(25);
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new ShadowBorder(),
                new EmptyBorder(16, 16, 16, 16)));

        // ── Modèle ────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };

        sorter = new TableRowSorter<>(tableModel);

        // ── Table ─────────────────────────────────────────────────────────
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row == hoveredRow ? HOVER
                            : row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        table.setRowSorter(sorter);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setGridColor(GRID);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(0xAED581));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Hover
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r != hoveredRow) { hoveredRow = r; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoveredRow = -1; table.repaint();
            }
        });

        // ── Header ────────────────────────────────────────────────────────
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int row, int col) {
                super.getTableCellRendererComponent(t, v, s, f, row, col);
                setBackground(COL_HEADER);
                setForeground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(new EmptyBorder(10, 8, 10, 8));
                return this;
            }
        });

        // Largeurs
        for (int i = 0; i < WIDTHS.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(WIDTHS[i]);

        // Centrage
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centre);

        // Sujet aligné à gauche (col 6)
        DefaultTableCellRenderer gauche = new DefaultTableCellRenderer();
        gauche.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(6).setCellRenderer(gauche);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  FOOTER
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel footer = new RoundedPanel(20);
        footer.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 10));
        footer.setBackground(new Color(0xE8F5E9));
        footer.add(legendItem(ROW_EVEN, "Ligne paire"));
        footer.add(legendItem(ROW_ODD,  "Ligne impaire"));
        footer.add(legendItem(HOVER,    "Survol"));
        JLabel hint = new JLabel("  💡 Clic en-tête = tri   •   🔍 Recherche par étudiant, salle, encadrant...");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(0x546E7A));
        footer.add(hint);
        return footer;
    }

    private JPanel legendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
        JPanel sq = new RoundedPanel(8);
        sq.setBackground(color);
        sq.setBorder(new LineBorder(GRID, 1));
        sq.setPreferredSize(new Dimension(16, 16));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.add(sq); item.add(lbl);
        return item;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  API PUBLIQUE — appelée depuis MainWindow
    // ═════════════════════════════════════════════════════════════════════
    public void setPlanning(Planning planning) {
        tableModel.setRowCount(0);

        if (planning == null || planning.getSoutenances().isEmpty()) {
            infoLabel.setText("Aucune soutenance");
            return;
        }

        // Tri par date puis heure
        List<Soutenance> liste = new ArrayList<>(planning.getSoutenances());
        liste.sort(Comparator
                .comparing((Soutenance s) -> s.getCreneau().getDate())
                .thenComparing(s -> s.getCreneau().getHeureDebut()));

        int num = 1;
        for (Soutenance s : liste) {
            tableModel.addRow(new Object[]{
                    num++,
                    s.getCreneau().getDate().format(FMT_DATE),
                    s.getCreneau().getHeureDebut().format(FMT_HEURE),
                    s.getCreneau().getHeureFin().format(FMT_HEURE),
                    s.getEtudiant().getNom()    + " " + s.getEtudiant().getPrenom(),
                    s.getEtudiant().getFiliere(),
                    s.getEtudiant().getSujet(),
                    s.getEncadrant().getNom()   + " " + s.getEncadrant().getPrenom(),
                    s.getJury1().getNom()       + " " + s.getJury1().getPrenom(),
                    s.getJury2().getNom()       + " " + s.getJury2().getPrenom(),
                    s.getSalle().getNom()
            });
        }

        infoLabel.setText(liste.size() + " soutenance(s)");
    }

    // ── Filtre texte ──────────────────────────────────────────────────────
    private void filtrer() {
        String txt = champRecherche.getText().trim();
        if (txt.isBlank() || txt.equals("🔍 Rechercher...")) {
            sorter.setRowFilter(null);
            infoLabel.setText(tableModel.getRowCount() + " soutenance(s)");
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txt));
            infoLabel.setText(table.getRowCount() + " résultat(s)");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  CLASSES INTERNES
    // ═════════════════════════════════════════════════════════════════════
    class RoundedPanel extends JPanel {
        private final int r;
        RoundedPanel(int r) { this.r = r; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ShadowBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(x+3, y+3, w-6, h-6, 25, 25);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(6,6,6,6); }
    }
}