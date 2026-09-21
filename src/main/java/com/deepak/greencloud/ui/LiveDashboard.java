package com.deepak.greencloud.ui;

import com.deepak.greencloud.ai.EnergySnapshot;
import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.constants.Constants;
import com.deepak.greencloud.monitoring.HostMetrics;
import com.deepak.greencloud.monitoring.LiveMonitoringData;
import com.deepak.greencloud.monitoring.MonitoringListener;
import com.deepak.greencloud.monitoring.ResourceSnapshot;
import com.deepak.greencloud.monitoring.VmMetrics;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

/** Live Swing view backed directly by MonitoringManager snapshots. */
public final class LiveDashboard implements MonitoringListener {
    private static final Color BG = new Color(0xF3, 0xF7, 0xF5);
    private static final Color GREEN = new Color(0x16, 0x75, 0x4B);
    private static final Color LIGHT = new Color(0xE7, 0xF4, 0xEC);
    private static final Color TEXT = new Color(0x1D, 0x2B, 0x25);
    private static final DecimalFormat TWO = new DecimalFormat("0.00");
    private static final DecimalFormat FOUR = new DecimalFormat("0.0000");

    private final JFrame frame;
    private final JLabel status = label("🟢 SIMULATION RUNNING", 16, GREEN);
    private final JLabel time = label("Time: 0.00 s", 13, TEXT);
    private final JLabel[] values = new JLabel[10];
    private final DefaultTableModel hostModel;
    private final XYChart energyChart = chart("Energy vs Time", "Simulation time (s)", "Accumulated energy (Wh)");
    private final XYChart hostChart = chart("Host Utilization", "Simulation time (s)", "Average CPU (%)");
    private final XYChart vmChart = chart("VM Utilization", "Simulation time (s)", "Average CPU (%)");
    private final XYChart migrationChart = chart("Migration Timeline", "Simulation time (s)", "Cumulative migrations");
    private XChartPanel<XYChart> energyPanel;
    private XChartPanel<XYChart> hostPanel;
    private XChartPanel<XYChart> vmPanel;
    private XChartPanel<XYChart> migrationPanel;
    private final Map<String, List<Double>> series = new HashMap<>();
    private final List<Double> times = new ArrayList<>();
    private int lastMigrationCount;
    private ResourceSnapshot lastMeaningfulSnapshot;
    private int lastCompletedCloudlets;
    private volatile LiveMonitoringData latestData;

    private LiveDashboard(JFrame parent) {
        frame = new JFrame("Green Cloud Framework | Live Monitoring Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 680));
        frame.setSize(1280, 900);
        frame.setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        root.setBackground(BG);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel title = new JPanel(new GridLayout(3, 1)); title.setOpaque(false);
        JLabel heading = label("Green Cloud Energy Monitoring", 25, TEXT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        JLabel subtitle = label("Live monitoring data from CloudSim and MonitoringManager", 13, new Color(0x67, 0x75, 0x6E));
        title.add(heading); title.add(status); title.add(subtitle);
        header.add(title, BorderLayout.WEST); header.add(time, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(14, 14)); body.setOpaque(false);
        JPanel kpis = new JPanel(new GridLayout(2, 5, 12, 12)); kpis.setOpaque(false);
        String[] names = {"CURRENT ENERGY", "AVG HOST CPU", "AVG VM CPU", "ACTIVE HOSTS", "RUNNING VMs", "RUNNING CLOUDLETS", "COMPLETED CLOUDLETS", "SLA VIOLATIONS", "MIGRATIONS", "SIMULATION TIME"};
        for (int i = 0; i < values.length; i++) kpis.add(kpi(names[i], values[i] = label("N/A", 21, TEXT)));
        body.add(kpis, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(2, 2, 14, 14)); charts.setOpaque(false);
        charts.setMinimumSize(new Dimension(720, 520));
        charts.setPreferredSize(new Dimension(1100, 560));
        energyPanel = new XChartPanel<>(energyChart); hostPanel = new XChartPanel<>(hostChart);
        vmPanel = new XChartPanel<>(vmChart); migrationPanel = new XChartPanel<>(migrationChart);
        Dimension chartMinimum = new Dimension(340, 245);
        Dimension chartPreferred = new Dimension(520, 270);
        for (XChartPanel<XYChart> panel : List.of(energyPanel, hostPanel, vmPanel, migrationPanel)) {
            panel.setMinimumSize(chartMinimum);
            panel.setPreferredSize(chartPreferred);
        }
        charts.add(energyPanel); charts.add(hostPanel); charts.add(vmPanel); charts.add(migrationPanel);
        body.add(charts, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 10)); bottom.setOpaque(false);
        hostModel = new DefaultTableModel(new Object[]{"Host ID", "CPU %", "RAM %", "Power (W)", "Energy (Wh)", "VM Count", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(hostModel); table.setRowHeight(26); table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setBackground(GREEN); table.getTableHeader().setForeground(Color.WHITE);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(1000, 175));
        tableScroll.setMinimumSize(new Dimension(0, 140));
        bottom.add(tableScroll, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0)); buttons.setOpaque(false);
        JButton reports = button("Open Reports", LIGHT, GREEN); JButton refresh = button("Refresh Dashboard", GREEN, Color.WHITE); JButton close = button("Close", new Color(0xF1, 0xF3, 0xF2), TEXT);
        buttons.add(reports); buttons.add(refresh); buttons.add(close);
        buttons.setPreferredSize(new Dimension(0, 42));
        bottom.add(buttons, BorderLayout.SOUTH);
        bottom.setPreferredSize(new Dimension(1000, 225));
        bottom.setMinimumSize(new Dimension(0, 190));
        body.add(bottom, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);
        JScrollPane dashboardScroll = new JScrollPane(root,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardScroll.setBorder(null);
        dashboardScroll.getViewport().setBackground(BG);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(24);
        frame.setContentPane(dashboardScroll); frame.setVisible(true);
        reports.addActionListener(e -> { try { java.awt.Desktop.getDesktop().open(Path.of(SimulationConfig.REPORTS_DIRECTORY).toFile()); } catch (Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); } });
        refresh.addActionListener(e -> refreshCurrentView()); close.addActionListener(e -> frame.dispose());
    }

    public static LiveDashboard open(JFrame parent) { return new LiveDashboard(parent); }

    @Override public void onMonitoringUpdate(LiveMonitoringData data) {
        latestData = data;
        Runnable update = () -> render(data);
        if (SwingUtilities.isEventDispatchThread()) update.run(); else SwingUtilities.invokeLater(update);
    }

    public void complete() { SwingUtilities.invokeLater(() -> status.setText("🟢 SIMULATION COMPLETED")); }

    /** Re-renders the latest in-memory live state without opening the report dashboard or rerunning simulation. */
    private void refreshCurrentView() {
        LiveMonitoringData data = latestData;
        if (data != null) {
            render(data);
        }
    }

    private void render(LiveMonitoringData data) {
        ResourceSnapshot snapshot = data.getSnapshot();
        var currentDc = snapshot.getDatacenterMetrics();
        if (currentDc != null) {
            lastCompletedCloudlets = Math.max(lastCompletedCloudlets, currentDc.getCompletedCloudlets());
        }
        boolean meaningful = isMeaningfulSnapshot(snapshot);
        if (meaningful) {
            lastMeaningfulSnapshot = snapshot;
        }
        ResourceSnapshot displaySnapshot = lastMeaningfulSnapshot == null ? snapshot : lastMeaningfulSnapshot;
        var displayDc = displaySnapshot.getDatacenterMetrics();
        double hostCpu = displayDc == null ? averageHostCpu(displaySnapshot) : displayDc.getAverageCpuUtilization();
        double vmCpu = averageVmCpu(displaySnapshot);
        double t = snapshot.getSimulationTime();
        time.setText("Time: " + TWO.format(t) + " s");
        values[0].setText(FOUR.format(data.getTotalEnergy()) + " Wh"); values[1].setText(percent(hostCpu)); values[2].setText(percent(vmCpu));
        values[3].setText(String.valueOf(displaySnapshot.getHostMetrics().stream().filter(h -> "ACTIVE".equalsIgnoreCase(h.getStatus())).count()));
        values[4].setText(String.valueOf(displaySnapshot.getVmMetrics().stream().filter(v -> v.getHostId() >= 0).count()));
        values[5].setText(String.valueOf(displaySnapshot.getVmMetrics().stream().mapToInt(VmMetrics::getCurrentCloudlets).sum()));
        values[6].setText(String.valueOf(lastCompletedCloudlets)); values[7].setText(String.valueOf(data.getSlaViolations()));
        values[8].setText(String.valueOf(data.getMigrations().size())); values[9].setText(TWO.format(t) + " s");
        append(energyChart, "Energy", t, data.getTotalEnergy(), false);
        if (meaningful) {
            append(hostChart, "Host CPU", t, hostCpu * 100, false);
            append(vmChart, "VM CPU", t, vmCpu * 100, false);
        }
        int migrationCount = data.getMigrations().size(); if (migrationCount > 0 && migrationCount != lastMigrationCount) append(migrationChart, "Migrations", t, migrationCount, true); lastMigrationCount = migrationCount;
        hostModel.setRowCount(0);
        Map<Long, EnergySnapshot> energy = new HashMap<>(); for (EnergySnapshot e : data.getEnergy()) energy.put(e.getHostId(), e);
        for (HostMetrics h : displaySnapshot.getHostMetrics()) { EnergySnapshot e = energy.get(h.getHostId()); hostModel.addRow(new Object[]{h.getHostId(), percent(h.getCpuUtilization()), percent(h.getRamUtilization()), e == null ? "N/A" : TWO.format(e.getCurrentPowerWatts()), e == null ? "N/A" : FOUR.format(e.getAccumulatedEnergyWattHours()), h.getRunningVmCount(), h.getStatus()}); }
    }

    /** Cleanup snapshots can legitimately contain no workload; keep the last useful workload state for display. */
    private static boolean isMeaningfulSnapshot(ResourceSnapshot snapshot) {
        return snapshot.getHostMetrics().stream().anyMatch(host ->
                host.getCpuUtilization() > 0)
                || snapshot.getVmMetrics().stream().anyMatch(vm ->
                vm.getCurrentCloudlets() > 0);
    }

    private void append(XYChart chart, String name, double x, double y, boolean allowDuplicateTime) {
        List<Double> xs = series.computeIfAbsent(name + "x", k -> new ArrayList<>()); List<Double> ys = series.computeIfAbsent(name + "y", k -> new ArrayList<>());
        if (!allowDuplicateTime && !xs.isEmpty() && xs.get(xs.size() - 1).equals(x)) { ys.set(ys.size() - 1, y); } else { xs.add(x); ys.add(y); }
        XYSeries line = chart.getSeriesMap().containsKey(name) ? chart.getSeriesMap().get(name) : chart.addSeries(name, xs, ys);
        if (chart.getSeriesMap().containsKey(name)) chart.updateXYSeries(name, xs, ys, null);
        line.setLineColor(GREEN); line.setLineWidth(2.4f); line.setMarker(xs.size() == 1 ? SeriesMarkers.CIRCLE : SeriesMarkers.NONE); line.setMarkerColor(GREEN);
        if (chart == energyChart) energyPanel.repaint(); else if (chart == hostChart) hostPanel.repaint(); else if (chart == vmChart) vmPanel.repaint(); else migrationPanel.repaint();
    }

    private static XYChart chart(String title, String x, String y) { XYChart c = new XYChartBuilder().width(420).height(220).title(title).xAxisTitle(x).yAxisTitle(y).build(); c.getStyler().setChartBackgroundColor(Color.WHITE); c.getStyler().setPlotBackgroundColor(Color.WHITE); c.getStyler().setLegendPosition(LegendPosition.InsideNE); return c; }
    private static JPanel kpi(String name, JLabel value) { JPanel p = new JPanel(new BorderLayout(0, 4)); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12)); JLabel n = label(name, 10, GREEN); p.add(n, BorderLayout.NORTH); p.add(value, BorderLayout.CENTER); return p; }
    private static JLabel label(String text, int size, Color color) { JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.PLAIN, size)); l.setForeground(color); return l; }
    private static JButton button(String text, Color bg, Color fg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(fg); b.setFocusPainted(false); b.setMargin(new Insets(8, 14, 8, 14)); return b; }
    private static double averageHostCpu(ResourceSnapshot s) { return s.getHostMetrics().stream().mapToDouble(HostMetrics::getCpuUtilization).average().orElse(0); }
    private static double averageVmCpu(ResourceSnapshot s) { return s.getVmMetrics().stream().mapToDouble(VmMetrics::getCpuUsage).average().orElse(0); }
    private static String percent(double value) { return TWO.format(value * 100) + "%"; }
}
