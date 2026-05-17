package ui;

import model.Encadrant;
import model.Etudiant;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Map;

public class AffectationPanel extends JPanel {

    // ─────────────────────────────────────────────
    // Couleurs
    // ─────────────────────────────────────────────

    private static final Color BG = new Color(0xF5F7FA);

    private static final Color HEADER_BG = new Color(0x1B5E20);
    private static final Color HEADER_FG = Color.WHITE;

    private static final Color ENC_BG = new Color(0x2E7D32);
    private static final Color ENC_FG = Color.WHITE;

    private static final Color ETU_EVEN = new Color(0xFFF9C4);
    private static final Color ETU_ODD = Color.WHITE;

    private static final Color HOVER = new Color(0xFFE082);

    private static final Color GRID = new Color(0xD6D6D6);

    private JTable table;
    private DefaultTableModel model;
    private JLabel infoLabel;

    private int hoveredRow = -1;

    // ─────────────────────────────────────────────
    // Constructeur
    // ─────────────────────────────────────────────

    public AffectationPanel() {

        setLayout(new BorderLayout(12, 12));

        setBackground(BG);

        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);

        add(buildCenterCard(), BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────

    private JPanel buildHeader() {

        JPanel panel = new RoundedPanel(25);

        panel.setLayout(new BorderLayout());

        panel.setBackground(HEADER_BG);

        panel.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("📋 Tableau d'Affectation");

        title.setForeground(HEADER_FG);

        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        infoLabel = new JLabel("—");

        infoLabel.setForeground(new Color(0xFFF59D));

        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(title, BorderLayout.WEST);

        panel.add(infoLabel, BorderLayout.EAST);

        return panel;
    }

    // ─────────────────────────────────────────────
    // CARD CENTRALE
    // ─────────────────────────────────────────────

    private JPanel buildCenterCard() {

        JPanel card = new RoundedPanel(25);

        card.setLayout(new BorderLayout());

        card.setBackground(Color.WHITE);

        card.setBorder(new CompoundBorder(
                new ShadowBorder(),
                new EmptyBorder(16, 16, 16, 16)
        ));

        card.add(buildTable(), BorderLayout.CENTER);

        return card;
    }

    // ─────────────────────────────────────────────
    // TABLE
    // ─────────────────────────────────────────────

    private JScrollPane buildTable() {

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model) {

            @Override
            public Component prepareRenderer(
                    TableCellRenderer renderer,
                    int row,
                    int column
            ) {

                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {

                    if (row == hoveredRow) {

                        c.setBackground(HOVER);

                        c.setForeground(Color.BLACK);

                    } else {

                        if (column == 0) {

                            c.setBackground(ENC_BG);

                            c.setForeground(ENC_FG);

                            c.setFont(c.getFont().deriveFont(Font.BOLD));

                        } else {

                            String val = (String) getValueAt(row, column);

                            if (val == null || val.isBlank()) {

                                c.setBackground(new Color(0xF5F5F5));

                                c.setForeground(new Color(0xBDBDBD));

                            } else {

                                c.setBackground(
                                        row % 2 == 0
                                                ? ETU_EVEN
                                                : ETU_ODD
                                );

                                c.setForeground(Color.BLACK);
                            }
                        }
                    }
                }

                return c;
            }
        };

        // Hover animation
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredRow = table.rowAtPoint(e.getPoint());
                table.repaint();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.setRowHeight(42);

        table.setGridColor(GRID);

        table.setShowGrid(true);

        table.setIntercellSpacing(new Dimension(1, 1));

        table.setSelectionBackground(new Color(0xAED581));

        table.setSelectionForeground(Color.BLACK);

        table.setBackground(Color.WHITE);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        applyHeaderStyle();

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(null);

        scroll.getViewport().setBackground(Color.WHITE);

        return scroll;
    }

    // ─────────────────────────────────────────────
    // HEADER STYLE
    // ─────────────────────────────────────────────

    private void applyHeaderStyle() {

        JTableHeader header = table.getTableHeader();

        header.setPreferredSize(new Dimension(0, 45));

        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {

                super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                );

                setBackground(HEADER_BG);

                setForeground(Color.WHITE);

                setHorizontalAlignment(SwingConstants.CENTER);

                setFont(new Font("Segoe UI", Font.BOLD, 14));

                setBorder(new EmptyBorder(10, 10, 10, 10));

                return this;
            }
        });
    }

    // ─────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────

    private JPanel buildFooter() {

        JPanel footer = new RoundedPanel(20);

        footer.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 10));

        footer.setBackground(new Color(0xE8F5E9));

        footer.add(legendItem(ENC_BG, "Encadrant"));

        footer.add(legendItem(ETU_EVEN, "Étudiant"));

        footer.add(legendItem(HOVER, "Hover animation"));

        return footer;
    }

    private JPanel legendItem(Color color, String text) {

        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));

        item.setOpaque(false);

        JPanel square = new RoundedPanel(8);

        square.setBackground(color);

        square.setPreferredSize(new Dimension(16, 16));

        JLabel label = new JLabel(text);

        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        item.add(square);

        item.add(label);

        return item;
    }

    // ─────────────────────────────────────────────
    // API
    // ─────────────────────────────────────────────

    public void setAffectation(
            Map<Encadrant, List<Etudiant>> affectation
    ) {

        model.setRowCount(0);

        model.setColumnCount(0);

        if (affectation == null || affectation.isEmpty()) {

            infoLabel.setText("Aucune affectation");

            return;
        }

        int maxEtu = affectation.values()
                .stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        model.addColumn("Encadrant");

        for (int i = 1; i <= maxEtu; i++) {
            model.addColumn("Étudiant " + i);
        }

        int total = 0;

        for (Map.Entry<Encadrant, List<Etudiant>> entry
                : affectation.entrySet()) {

            Encadrant enc = entry.getKey();

            List<Etudiant> etudiants = entry.getValue();

            Object[] row = new Object[maxEtu + 1];

            row[0] = enc.getNom() + " " + enc.getPrenom();

            for (int i = 0; i < maxEtu; i++) {

                if (i < etudiants.size()) {

                    Etudiant e = etudiants.get(i);

                    row[i + 1] = e.getNom() + " " + e.getPrenom();

                } else {

                    row[i + 1] = "";
                }
            }

            model.addRow(row);

            total += etudiants.size();
        }

        table.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(220);

        for (int i = 1; i < model.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(180);
        }

        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();

        center.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < model.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(center);
        }

        table.setDefaultRenderer(Object.class, null);

        infoLabel.setText(
                affectation.size()
                        + " encadrants   •   "
                        + total
                        + " étudiants"
        );
    }

    // ─────────────────────────────────────────────
    // PANEL ARRONDI
    // ─────────────────────────────────────────────

    class RoundedPanel extends JPanel {

        private int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(getBackground());

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ─────────────────────────────────────────────
    // OMBRE
    // ─────────────────────────────────────────────

    class ShadowBorder extends AbstractBorder {

        @Override
        public void paintBorder(
                Component c,
                Graphics g,
                int x,
                int y,
                int width,
                int height
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(0, 0, 0, 30));

            g2.fillRoundRect(
                    x + 3,
                    y + 3,
                    width - 6,
                    height - 6,
                    25,
                    25
            );

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 6, 6, 6);
        }
    }
}