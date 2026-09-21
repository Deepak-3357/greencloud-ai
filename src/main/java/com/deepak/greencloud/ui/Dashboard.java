package com.deepak.greencloud.ui;

import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.constants.Constants;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Displays only the CSV artifacts written by the simulation. */
public final class Dashboard {
    private static final Color BACKGROUND = new Color(0xF3, 0xF7, 0xF5);
    private static final Color SURFACE = Color.WHITE;
    private static final Color GREEN = new Color(0x16, 0x75, 0x4B);
    private static final Color GREEN_LIGHT = new Color(0xE7, 0xF4, 0xEC);
    private static final Color TEXT = new Color(0x1D, 0x2B, 0x25);
    private static final Color MUTED = new Color(0x67, 0x75, 0x6E);
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");
    private static final DecimalFormat FOUR_DECIMALS = new DecimalFormat("0.0000");

    private Dashboard() { }

    public static void showDashboard(JFrame parent) {
        Path reports = Path.of(SimulationConfig.REPORTS_DIRECTORY);
        if (!reports.toFile().isDirectory()) {
            JOptionPane.showMessageDialog(parent, "Reports directory not found: " + reports.toAbsolutePath(), "No Reports", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DashboardData data = loadData(reports);
        JFrame frame = new JFrame("Green Cloud Framework | Energy Monitoring Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 680));
        frame.setSize(1280, 900);
        frame.setLocationRelativeTo(parent);

        DashboardPage page = new DashboardPage();
        page.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel sections = new JPanel();
        sections.setOpaque(false);
        sections.setLayout(new javax.swing.BoxLayout(sections, javax.swing.BoxLayout.Y_AXIS));

        JPanel header = createHeader();
        JPanel cards = createCards(data.summary);
        JPanel chartGrid = createChartGrid(data);
        JPanel hostSection = createHostSection(data, reports, frame, parent);
        header.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        cards.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        chartGrid.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        hostSection.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        sections.add(header);
        sections.add(javax.swing.Box.createVerticalStrut(16));
        sections.add(cards);
        sections.add(javax.swing.Box.createVerticalStrut(16));
        sections.add(chartGrid);
        sections.add(javax.swing.Box.createVerticalStrut(16));
        sections.add(hostSection);
        page.add(sections, BorderLayout.CENTER);

        JScrollPane dashboardScroll = new JScrollPane(page,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardScroll.setBorder(null);
        dashboardScroll.getViewport().setBackground(BACKGROUND);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(24);
        dashboardScroll.getHorizontalScrollBar().setUnitIncrement(24);
        frame.setContentPane(dashboardScroll);
        frame.setVisible(true);
    }

    private static JPanel createChartGrid(DashboardData data) {
        JPanel stage = new JPanel(new GridBagLayout());
        stage.setOpaque(false);
        stage.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setMinimumSize(new Dimension(720, 500));
        grid.setPreferredSize(new Dimension(1240, 600));
        grid.setMaximumSize(new Dimension(1500, 1000));
        grid.add(chartCard(buildEnergyChart(data.energy), data.energy.totalByTimestamp.isEmpty()));
        grid.add(chartCard(buildHostUtilizationChart(data.hosts), data.hosts.byTimestamp.isEmpty()));
        grid.add(chartCard(buildVmUtilizationChart(data.vms), data.vms.byTimestamp.isEmpty()));
        grid.add(chartCard(buildMigrationChart(data.migrations), data.migrations.isEmpty()));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        stage.add(grid, constraints);
        return stage;
    }

    private static DashboardData loadData(Path reports) {
        try {
            return new DashboardData(
                    readSimulationSummary(reports.resolve(Constants.SIMULATION_SUMMARY_FILE).toFile()),
                    readHostMetrics(reports.resolve(Constants.HOST_METRICS_FILE).toFile()),
                    readVmMetrics(reports.resolve(Constants.VM_METRICS_FILE).toFile()),
                    readEnergyHistory(reports.resolve("energy_history.csv").toFile()),
                    readMigrations(reports.resolve("migration_history.csv").toFile(), reports.resolve(Constants.EVENT_LOG_FILE).toFile()));
        } catch (Exception ex) {
            return new DashboardData(new HashMap<>(), new HostTimeSeries(), new VmTimeSeries(), new EnergyTimeSeries(), new ArrayList<>());
        }
    }

    private static JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Green Cloud Energy Monitoring");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Simulation analytics from exported report data");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 2));
        labels.setOpaque(false);
        labels.add(title);
        labels.add(subtitle);
        header.add(labels, BorderLayout.WEST);
        return header;
    }

    private static JPanel createCards(Map<String, String> summary) {
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(makeCard("TOTAL ENERGY", formatEnergy(summary), "Simulation consumption"));
        cards.add(makeCard("AVG HOST UTILIZATION", formatPercent(summary.get("average_host_utilization")), "Across recorded hosts"));
        cards.add(makeCard("SLA VIOLATIONS", formatWhole(summary, "total_sla_violations", "sla_violations"), "Reported by simulation"));
        cards.add(makeCard("VM MIGRATIONS", formatWhole(summary, "total_migrations"), "Recorded migration events"));
        return cards;
    }

    private static JPanel makeCard(String title, String value, String caption) {
        RoundedPanel card = new RoundedPanel(18, SURFACE);
        card.setLayout(new BorderLayout(0, 7));
        card.setBorder(BorderFactory.createEmptyBorder(15, 17, 15, 17));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        heading.setForeground(GREEN);
        JLabel metric = new JLabel(value);
        metric.setFont(new Font("Segoe UI", Font.BOLD, 24));
        metric.setForeground(TEXT);
        JLabel detail = new JLabel(caption);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detail.setForeground(MUTED);
        JPanel middle = new JPanel(new BorderLayout());
        middle.setOpaque(false);
        middle.add(metric, BorderLayout.CENTER);
        card.add(heading, BorderLayout.NORTH);
        card.add(middle, BorderLayout.CENTER);
        card.add(detail, BorderLayout.SOUTH);
        return card;
    }

    private static JPanel chartCard(XYChart chart, boolean noData) {
        RoundedPanel card = new RoundedPanel(18, SURFACE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.setMinimumSize(new Dimension(340, 235));
        card.setPreferredSize(new Dimension(580, 285));
        if (noData) {
            JLabel message = new JLabel("No data available", SwingConstants.CENTER);
            message.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            message.setForeground(MUTED);
            card.add(message, BorderLayout.CENTER);
        } else {
            card.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        }
        return card;
    }

    private static JPanel createHostSection(DashboardData data, Path reports, JFrame frame, JFrame parent) {
        RoundedPanel section = new RoundedPanel(18, SURFACE);
        section.setLayout(new BorderLayout(10, 10));
        section.setBorder(BorderFactory.createEmptyBorder(13, 15, 13, 15));
        JLabel title = new JLabel("Host Summary  •  Latest active workload sample");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        section.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Host ID", "CPU", "RAM", "Power (W)", "Energy (Wh)", "VM Count", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        populateHostTable(model, data.hosts, data.energy);
        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDF, 0xE8, 0xE2)));
        scroll.setMinimumSize(new Dimension(0, 120));
        scroll.setPreferredSize(new Dimension(0, 175));
        section.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0));
        buttons.setOpaque(false);
        JButton export = modernButton("Open Reports", GREEN_LIGHT, GREEN);
        JButton refresh = modernButton("Refresh Dashboard", GREEN, Color.WHITE);
        JButton close = modernButton("Close", new Color(0xF1, 0xF3, 0xF2), TEXT);
        buttons.add(export); buttons.add(refresh); buttons.add(close);
        actions.add(buttons, BorderLayout.EAST);
        section.add(actions, BorderLayout.SOUTH);
        section.setMinimumSize(new Dimension(0, 170));
        section.setPreferredSize(new Dimension(0, 220));

        refresh.addActionListener(e -> { frame.dispose(); SwingUtilities.invokeLater(() -> showDashboard(parent)); });
        close.addActionListener(e -> frame.dispose());
        export.addActionListener(e -> {
            try { java.awt.Desktop.getDesktop().open(reports.toFile()); }
            catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Unable to open reports folder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        return section;
    }

    private static void populateHostTable(DefaultTableModel model, HostTimeSeries hosts, EnergyTimeSeries energy) {
        Map<Long, HostMetric> latestHosts = hosts.latestActiveByHost.isEmpty() ? hosts.latestByHost : hosts.latestActiveByHost;
        Map<Long, EnergyEntry> latestEnergy = energy.latestByHost;
        TreeMap<Long, Boolean> ids = new TreeMap<>();
        latestHosts.keySet().forEach(id -> ids.put(id, Boolean.TRUE));
        latestEnergy.keySet().forEach(id -> ids.put(id, Boolean.TRUE));
        for (long id : ids.keySet()) {
            HostMetric host = latestHosts.get(id);
            EnergyEntry e = host == null ? latestEnergy.get(id) : energy.atOrBefore(id, host.timestamp);
            model.addRow(new Object[]{id,
                    host == null ? "N/A" : formatPercent(host.cpuUtilization),
                    host == null ? "N/A" : formatPercent(host.ramUtilization),
                    e == null ? "N/A" : TWO_DECIMALS.format(e.currentPowerWatts),
                    e == null ? "N/A" : FOUR_DECIMALS.format(e.accumulatedEnergyWh),
                    host == null ? "N/A" : host.runningVmCount,
                    host != null ? host.status : e == null ? "N/A" : e.hostState});
        }
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setGridColor(new Color(0xE4, 0xEB, 0xE7));
        table.setShowVerticalLines(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        table.getTableHeader().setBackground(GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    private static JButton modernButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(background); button.setForeground(foreground);
        button.setFocusPainted(false); button.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private static Map<String, String> readSimulationSummary(File file) throws Exception {
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> row : readCsv(file)) {
            String metric = row.get("metric"); String value = row.get("value");
            if (metric != null && value != null) result.put(metric.trim(), value.trim());
        }
        return result;
    }

    private static HostTimeSeries readHostMetrics(File file) throws Exception {
        HostTimeSeries series = new HostTimeSeries();
        for (Map<String, String> row : readCsv(file)) {
            double time = number(row.get("timestamp"));
            long hostId = whole(row.get("host_id"));
            HostMetric metric = new HostMetric(time, hostId, number(row.get("cpu_utilization")), number(row.get("ram_utilization")),
                    whole(row.get("running_vm_count")), row.getOrDefault("status", "N/A"));
            series.byTimestamp.computeIfAbsent(time, ignored -> new ArrayList<>()).add(metric);
            series.latestByHost.merge(hostId, metric, Dashboard::laterHostMetric);
            if (metric.cpuUtilization > 0) series.latestActiveByHost.merge(hostId, metric, Dashboard::laterHostMetric);
        }
        return series;
    }

    private static HostMetric laterHostMetric(HostMetric first, HostMetric second) { return second.timestamp >= first.timestamp ? second : first; }

    private static VmTimeSeries readVmMetrics(File file) throws Exception {
        VmTimeSeries series = new VmTimeSeries();
        for (Map<String, String> row : readCsv(file)) {
            double time = number(row.get("timestamp"));
            VmMetric metric = new VmMetric(time, whole(row.get("vm_id")), number(row.get("cpu_usage")));
            series.byTimestamp.computeIfAbsent(time, ignored -> new ArrayList<>()).add(metric);
        }
        return series;
    }

    private static EnergyTimeSeries readEnergyHistory(File file) throws Exception {
        EnergyTimeSeries series = new EnergyTimeSeries();
        Map<Double, Map<Long, EnergyEntry>> uniqueEntriesByTimestamp = new TreeMap<>();
        for (Map<String, String> row : readCsv(file)) {
            double time = number(row.get("timestamp")); long hostId = whole(row.get("host_id"));
            EnergyEntry entry = new EnergyEntry(time, hostId, number(row.get("current_power_watts")), number(row.get("accumulated_energy_wh")), row.getOrDefault("host_state", "N/A"));
            series.byTimestamp.computeIfAbsent(time, ignored -> new ArrayList<>()).add(entry);
            series.byHost.computeIfAbsent(hostId, ignored -> new ArrayList<>()).add(entry);
            series.latestByHost.merge(hostId, entry, Dashboard::laterEnergyEntry);
            uniqueEntriesByTimestamp.computeIfAbsent(time, ignored -> new HashMap<>())
                    .merge(hostId, entry, Dashboard::higherAccumulatedEnergy);
        }
        for (Map.Entry<Double, Map<Long, EnergyEntry>> entry : uniqueEntriesByTimestamp.entrySet()) {
            // energy_history.csv may contain repeated snapshots for one host at the same simulation time.
            // Each host's accumulated value is cumulative, so count the highest reading once per host.
            series.totalByTimestamp.put(entry.getKey(), entry.getValue().values().stream()
                    .mapToDouble(e -> e.accumulatedEnergyWh).sum());
        }
        return series;
    }

    private static EnergyEntry laterEnergyEntry(EnergyEntry first, EnergyEntry second) { return second.timestamp >= first.timestamp ? second : first; }

    private static EnergyEntry higherAccumulatedEnergy(EnergyEntry first, EnergyEntry second) {
        return second.accumulatedEnergyWh >= first.accumulatedEnergyWh ? second : first;
    }

    private static List<Migration> readMigrations(File migrationFile, File eventFile) throws Exception {
        List<Migration> result = new ArrayList<>();
        for (Map<String, String> row : readCsv(migrationFile)) result.add(new Migration(number(row.get("migration_time"))));
        if (!result.isEmpty()) return result;
        for (Map<String, String> row : readCsv(eventFile)) {
            if (row.getOrDefault("event_type", "").toLowerCase(Locale.ROOT).contains("migrat")) result.add(new Migration(number(row.get("simulation_time"))));
        }
        return result;
    }

    private static List<Map<String, String>> readCsv(File file) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        if (!file.isFile()) return rows;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            if (header == null) return rows;
            String[] columns = splitCsv(header);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] values = splitCsv(line); Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.length; i++) row.put(columns[i].trim(), i < values.length ? values[i].trim() : "");
                rows.add(row);
            }
        }
        return rows;
    }

    private static XYChart baseChart(String title, String xTitle, String yTitle) {
        XYChart chart = new XYChartBuilder().width(420).height(250).title(title).xAxisTitle(xTitle).yAxisTitle(yTitle).build();
        chart.getStyler().setChartBackgroundColor(SURFACE); chart.getStyler().setPlotBackgroundColor(SURFACE);
        chart.getStyler().setPlotGridLinesColor(new Color(0xD9, 0xE5, 0xDE)); chart.getStyler().setAxisTickLabelsColor(MUTED);
        chart.getStyler().setChartTitleFont(new Font("Segoe UI", Font.BOLD, 15)); chart.getStyler().setAxisTitleFont(new Font("Segoe UI", Font.PLAIN, 12));
        chart.getStyler().setLegendFont(new Font("Segoe UI", Font.PLAIN, 11)); chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        chart.getStyler().setPlotContentSize(0.88);
        chart.getStyler().setChartPadding(8);
        chart.getStyler().setPlotMargin(6);
        chart.getStyler().setXAxisTickMarkSpacingHint(70);
        chart.getStyler().setYAxisTickMarkSpacingHint(45);
        return chart;
    }

    private static XYChart buildEnergyChart(EnergyTimeSeries series) {
        XYChart chart = baseChart("Energy vs Time", "Simulation time (s)", "Accumulated energy (Wh)");
        addSeries(chart, "Total energy", series.totalByTimestamp);
        setYAxisBounds(chart, series.totalByTimestamp, true);
        return chart;
    }

    private static XYChart buildHostUtilizationChart(HostTimeSeries series) {
        Map<Double, Double> averages = new TreeMap<>();
        series.byTimestamp.forEach((time, rows) -> averages.put(time, rows.stream().mapToDouble(row -> row.cpuUtilization).average().orElse(0) * 100));
        XYChart chart = baseChart("Host Utilization", "Simulation time (s)", "Average CPU (%)");
        addSeries(chart, "Average host CPU", averages);
        setYAxisBounds(chart, averages, true);
        return chart;
    }

    private static XYChart buildVmUtilizationChart(VmTimeSeries series) {
        Map<Double, Double> averages = new TreeMap<>();
        series.byTimestamp.forEach((time, rows) -> averages.put(time, rows.stream().mapToDouble(row -> row.cpuUsage).average().orElse(0) * 100));
        XYChart chart = baseChart("VM Utilization", "Simulation time (s)", "Average CPU (%)");
        addSeries(chart, "Average VM CPU", averages);
        setYAxisBounds(chart, averages, true);
        return chart;
    }

    private static XYChart buildMigrationChart(List<Migration> migrations) {
        Map<Double, Double> cumulative = new TreeMap<>(); double total = 0;
        for (Map.Entry<Double, Long> group : migrations.stream().collect(java.util.stream.Collectors.groupingBy(Migration::time, TreeMap::new, java.util.stream.Collectors.counting())).entrySet()) {
            total += group.getValue(); cumulative.put(group.getKey(), total);
        }
        XYChart chart = baseChart("Migration Timeline", "Simulation time (s)", "Cumulative migrations");
        addSeries(chart, "Migrations", cumulative);
        setYAxisBounds(chart, cumulative, true);
        return chart;
    }

    private static void setYAxisBounds(XYChart chart, Map<Double, Double> values, boolean nonNegative) {
        if (values.isEmpty()) return;
        double min = values.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double range = max - min;
        double padding = range > 0 ? range * 0.12 : Math.max(1.0, Math.abs(max) * 0.10);
        double lower = min - padding;
        double upper = max + padding;
        if (nonNegative) lower = Math.max(0.0, lower);
        if (upper <= lower) upper = lower + 1.0;
        chart.getStyler().setYAxisMin(lower);
        chart.getStyler().setYAxisMax(upper);
    }

    private static void addSeries(XYChart chart, String name, Map<Double, Double> values) {
        if (values.isEmpty()) return;
        XYSeries line = chart.addSeries(name, new ArrayList<>(values.keySet()), new ArrayList<>(values.values()));
        line.setLineColor(GREEN); line.setLineWidth(2.6f); line.setMarker(values.size() == 1 ? SeriesMarkers.CIRCLE : SeriesMarkers.NONE);
        line.setMarkerColor(GREEN);
    }

    private static String formatEnergy(Map<String, String> summary) {
        String value = summary.getOrDefault("energy_consumed_wh", summary.get("total_energy"));
        return value == null ? "N/A" : FOUR_DECIMALS.format(number(value)) + " Wh";
    }
    private static String formatPercent(String value) { return value == null || value.isBlank() ? "N/A" : formatPercent(number(value)); }
    private static String formatPercent(double value) { return TWO_DECIMALS.format(value * 100) + "%"; }
    private static String formatWhole(Map<String, String> summary, String... keys) {
        for (String key : keys) if (summary.containsKey(key)) return String.valueOf(whole(summary.get(key)));
        return "N/A";
    }
    private static double number(String value) { try { double n = Double.parseDouble(value); return Double.isFinite(n) ? n : 0; } catch (Exception ex) { return 0; } }
    private static long whole(String value) { try { return Long.parseLong(value); } catch (Exception ex) { return Math.round(number(value)); } }

    private static String[] splitCsv(String line) {
        List<String> values = new ArrayList<>(); boolean quoted = false; StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) { char c = line.charAt(i); if (c == '"') quoted = !quoted; else if (c == ',' && !quoted) { values.add(current.toString()); current.setLength(0); } else current.append(c); }
        values.add(current.toString()); return values.toArray(new String[0]);
    }

    private record DashboardData(Map<String, String> summary, HostTimeSeries hosts, VmTimeSeries vms, EnergyTimeSeries energy, List<Migration> migrations) { }
    private static final class HostTimeSeries { final Map<Double, List<HostMetric>> byTimestamp = new TreeMap<>(); final Map<Long, HostMetric> latestByHost = new HashMap<>(); final Map<Long, HostMetric> latestActiveByHost = new HashMap<>(); }
    private static final class VmTimeSeries { final Map<Double, List<VmMetric>> byTimestamp = new TreeMap<>(); }
    private static final class EnergyTimeSeries {
        final Map<Double, List<EnergyEntry>> byTimestamp = new TreeMap<>(); final Map<Double, Double> totalByTimestamp = new TreeMap<>();
        final Map<Long, EnergyEntry> latestByHost = new HashMap<>(); final Map<Long, List<EnergyEntry>> byHost = new HashMap<>();
        EnergyEntry atOrBefore(long hostId, double time) {
            return byHost.getOrDefault(hostId, List.of()).stream().filter(entry -> entry.timestamp <= time)
                    .max(Comparator.comparingDouble(EnergyEntry::timestamp)).orElse(latestByHost.get(hostId));
        }
    }
    private record HostMetric(double timestamp, long hostId, double cpuUtilization, double ramUtilization, long runningVmCount, String status) { }
    private record VmMetric(double timestamp, long vmId, double cpuUsage) { }
    private record EnergyEntry(double timestamp, long hostId, double currentPowerWatts, double accumulatedEnergyWh, String hostState) { }
    private record Migration(double time) { }

    /** Scrollable page that follows the viewport width but keeps its full vertical content. */
    private static final class DashboardPage extends JPanel implements Scrollable {
        DashboardPage() {
            super(new BorderLayout(16, 16));
            setBackground(BACKGROUND);
        }

        @Override public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(1200, 800);
        }

        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == javax.swing.SwingConstants.VERTICAL ? 24 : 16;
        }

        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == javax.swing.SwingConstants.VERTICAL
                    ? Math.max(visibleRect.height - 24, 24)
                    : Math.max(visibleRect.width - 16, 16);
        }

        @Override public boolean getScrollableTracksViewportWidth() { return true; }

        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class RoundedPanel extends JPanel {
        private final int radius; private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill); g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius); g.dispose(); super.paintComponent(graphics);
        }
    }
}
