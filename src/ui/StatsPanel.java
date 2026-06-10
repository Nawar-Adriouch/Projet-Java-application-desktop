package ui;

import model.Planning;
import service.StatsPlanning;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class StatsPanel extends JPanel {

    // Palette
    private static final Color BG          = new Color(0xF8F9FA);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BORDER_CLR  = new Color(0xE2E8F0);
    private static final Color TEXT_PRI    = new Color(0x1E293B);
    private static final Color TEXT_SEC    = new Color(0x64748B);
    private static final Color ACCENT      = new Color(0x3B82F6);

    private static final Color[] PALETTE = {
            new Color(0x3B82F6), new Color(0x10B981), new Color(0xF59E0B),
            new Color(0xEF4444), new Color(0x8B5CF6), new Color(0x06B6D4),
            new Color(0xF97316), new Color(0x84CC16)
    };

    private Planning planning;
    private JPanel cardsContainer;


    public StatsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);

        // En-tête
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        cardsContainer = new JPanel();
        cardsContainer.setBackground(BG);
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel placeholder = new JLabel("Aucun planning chargé — générez d'abord un planning.");
        placeholder.setForeground(TEXT_SEC);
        placeholder.setFont(new Font("SansSerif", Font.ITALIC, 14));
        placeholder.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardsContainer.add(Box.createVerticalStrut(60));
        cardsContainer.add(placeholder);

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    // ── En-tête ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel title = new JLabel("Statistiques du Planning");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT_PRI);

        JButton refresh = new JButton("Actualiser");
        refresh.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refresh.setForeground(ACCENT);
        refresh.setBackground(new Color(0xEFF6FF));
        refresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBFDBFE), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> refreshGraphs());

        h.add(title, BorderLayout.WEST);
        h.add(refresh, BorderLayout.EAST);
        return h;
    }


    public void setPlanning(Planning planning) {
        this.planning = planning;
        refreshGraphs();
    }

    public void refreshGraphs() {
        cardsContainer.removeAll();
        if (planning == null || planning.getSoutenances().isEmpty()) {
            JLabel lbl = new JLabel("Aucune donnée disponible.");
            lbl.setForeground(TEXT_SEC);
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            cardsContainer.add(Box.createVerticalStrut(60));
            cardsContainer.add(lbl);
        } else {
            buildKpiRow();
            cardsContainer.add(Box.createVerticalStrut(16));
            buildRow2Graphs();
            cardsContainer.add(Box.createVerticalStrut(16));
            buildRow3Graphs();
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    // KPI cards
    private void buildKpiRow() {
        int total   = planning.getSoutenances().size();
        long salles = planning.getSoutenances().stream()
                .map(s -> s.getSalle().getId()).distinct().count();
        long profs  = planning.getSoutenances().stream()
                .flatMap(s -> List.of(s.getEncadrant(), s.getJury1(), s.getJury2()).stream())
                .map(p -> p.getId()).distinct().count();
        Map<String, Integer> ecart = StatsPlanning.ecartJurys(planning);

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.add(kpiCard("Soutenances",  String.valueOf(total),          "planifiées",   new Color(0x3B82F6)));
        row.add(kpiCard("Salles",       String.valueOf(salles),         "mobilisées",   new Color(0x10B981)));
        row.add(kpiCard("Professeurs",  String.valueOf(profs),          "impliqués",    new Color(0x8B5CF6)));
        row.add(kpiCard("Écart jurys",  String.valueOf(ecart.getOrDefault("ecart",0)), "max−min", new Color(0xF59E0B)));
        cardsContainer.add(row);
    }

    private JPanel kpiCard(String label, String value, String sub, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.setColor(accent);
                g2.fillRoundRect(0, 16, 4, getHeight()-32, 2, 2);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SEC);

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 26));
        val.setForeground(TEXT_PRI);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("SansSerif", Font.PLAIN, 11));
        s.setForeground(TEXT_SEC);

        card.add(lbl); card.add(val); card.add(s);
        return card;
    }

    //  Ligne 2  barres charge profs + camembert filières ───
    private void buildRow2Graphs() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        Map<String, Integer> charge = StatsPlanning.chargeParProf(planning);
        row.add(wrapCard("Charge par professeur", new BarChartPanel(charge, PALETTE, false)));

        Map<String, Integer> filieres = StatsPlanning.repartitionParFiliere(planning);
        row.add(wrapCard("Répartition par filière", new PieChartPanel(filieres, PALETTE)));

        cardsContainer.add(row);
    }

    //Ligne 3  barres jour + barres salles
    private void buildRow3Graphs() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // Soutenances par jour
        Map<LocalDate, Integer> parJour = StatsPlanning.soutenancesParJour(planning);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        Map<String, Integer> parJourStr = new LinkedHashMap<>();
        parJour.forEach((d, v) -> parJourStr.put(d.format(fmt), v));
        row.add(wrapCard("Soutenances par jour", new BarChartPanel(parJourStr, new Color[]{new Color(0x10B981)}, true)));

        // Taux occupation salles
        Map<String, Integer> salles = StatsPlanning.occupationSalles(planning);
        row.add(wrapCard("Occupation des salles", new BarChartPanel(salles, PALETTE, false)));

        cardsContainer.add(row);
    }

    // ── Wrapper carte ─────────────────────────────────────────
    private JPanel wrapCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 14, 14));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRI);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));

        card.add(lbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }


    private static class BarChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private final Color[] colors;
        private final boolean singleColor;

        BarChartPanel(Map<String, Integer> data, Color[] colors, boolean singleColor) {
            this.data = data;
            this.colors = colors;
            this.singleColor = singleColor;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padL = 36, padR = 12, padT = 10, padB = 48;
            int chartW = W - padL - padR;
            int chartH = H - padT - padB;

            int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            int n = data.size();
            int barW = Math.max(8, (chartW - (n + 1) * 6) / n);

            // Lignes de grille
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{3, 3}, 0));
            int steps = Math.min(maxVal, 5);
            for (int i = 0; i <= steps; i++) {
                int y = padT + chartH - (int)((double) i / steps * chartH);
                g2.setColor(new Color(0xE2E8F0));
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(TEXT_SEC);
                String lbl = String.valueOf((int)((double) i / steps * maxVal));
                g2.drawString(lbl, padL - g2.getFontMetrics().stringWidth(lbl) - 3, y + 4);
            }
            g2.setStroke(new BasicStroke(1f));

            // Barres
            int idx = 0;
            List<String> keys = new ArrayList<>(data.keySet());
            for (String key : keys) {
                int val = data.get(key);
                int x = padL + idx * (barW + 6) + 6;
                int barH = (int)((double) val / maxVal * chartH);
                int y = padT + chartH - barH;

                Color c = singleColor ? colors[0] : colors[idx % colors.length];
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 4, 4));

                // Valeur au-dessus
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(TEXT_PRI);
                String v = String.valueOf(val);
                int vx = x + barW/2 - g2.getFontMetrics().stringWidth(v)/2;
                g2.drawString(v, vx, y - 3);

                // Label axe X (tronqué)
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(TEXT_SEC);
                String shortKey = key.length() > 8 ? key.substring(0, 7) + "." : key;
                int lx = x + barW/2 - g2.getFontMetrics().stringWidth(shortKey)/2;
                g2.drawString(shortKey, lx, H - padB + 14);

                idx++;
            }

            // Axe Y
            g2.setColor(BORDER_CLR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padL, padT, padL, padT + chartH);
        }
    }


    private static class PieChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private final Color[] colors;

        PieChartPanel(Map<String, Integer> data, Color[] colors) {
            this.data = data;
            this.colors = colors;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int legendH = data.size() * 18 + 8;
            int pieZone  = H - legendH;
            int diameter = Math.min(W - 20, pieZone - 10);
            int cx = W / 2 - diameter / 2;
            int cy = (pieZone - diameter) / 2;

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            double startAngle = -90;
            int idx = 0;

            List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());

            for (Map.Entry<String, Integer> e : entries) {
                double sweep = (double) e.getValue() / total * 360.0;
                Color c = colors[idx % colors.length];

                g2.setColor(c);
                g2.fill(new Arc2D.Double(cx, cy, diameter, diameter, startAngle, sweep, Arc2D.PIE));

                // Séparateur blanc
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Arc2D.Double(cx, cy, diameter, diameter, startAngle, sweep, Arc2D.PIE));

                // Pourcentage au centre du secteur
                double midAngle = Math.toRadians(startAngle + sweep / 2);
                int r = diameter / 4;
                int px = (int)(cx + diameter/2 + r * Math.cos(midAngle));
                int py = (int)(cy + diameter/2 + r * Math.sin(midAngle));
                String pct = String.format("%.0f%%", (double) e.getValue() / total * 100);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pct, px - fm.stringWidth(pct)/2, py + 4);

                startAngle += sweep;
                idx++;
            }

            // Légende sous le camembert
            int ly = pieZone + 4;
            idx = 0;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (Map.Entry<String, Integer> e : entries) {
                Color c = colors[idx % colors.length];
                g2.setColor(c);
                g2.fillRoundRect(8, ly + idx * 18 + 2, 12, 12, 3, 3);
                g2.setColor(TEXT_PRI);
                String lbl = e.getKey() + " (" + e.getValue() + ")";
                g2.drawString(lbl, 26, ly + idx * 18 + 13);
                idx++;
            }
        }
    }
}

