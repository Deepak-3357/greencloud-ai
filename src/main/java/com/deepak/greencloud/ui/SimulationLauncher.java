package com.deepak.greencloud.ui;

import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.simulation.CloudSimulation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Simple launcher UI for the simulation. Collects user inputs and runs the simulation in a background thread.
 */
public final class SimulationLauncher {

    private SimulationLauncher() {
    }

    public static void showLauncher() {
        JFrame frame = new JFrame("Green Cloud Framework - Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 360);
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel();
        content.setBackground(new Color(0xF4F7F9));
        content.setLayout(null);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        int left = 30;
        int top = 20;
        int labelW = 220;
        int inputW = 200;
        int h = 30;
        int gap = 12;

        JLabel title = new JLabel("Green Cloud Framework", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBounds(0, 0, 520, 40);
        content.add(title);

        JLabel hostsLabel = new JLabel("Number of Hosts");
        hostsLabel.setBounds(left, top + 40, labelW, h);
        hostsLabel.setFont(labelFont);
        content.add(hostsLabel);
        JTextField hostsField = new JTextField(String.valueOf(SimulationConfig.DEFAULT_HOST_COUNT));
        hostsField.setBounds(left + labelW, top + 40, inputW, h);
        hostsField.setFont(inputFont);
        content.add(hostsField);

        JLabel vmsLabel = new JLabel("Number of VMs");
        vmsLabel.setBounds(left, top + 40 + (h + gap), labelW, h);
        vmsLabel.setFont(labelFont);
        content.add(vmsLabel);
        JTextField vmsField = new JTextField(String.valueOf(SimulationConfig.DEFAULT_VM_COUNT));
        vmsField.setBounds(left + labelW, top + 40 + (h + gap), inputW, h);
        vmsField.setFont(inputFont);
        content.add(vmsField);

        JLabel durationLabel = new JLabel("Simulation Duration (cloudlet length)");
        durationLabel.setBounds(left, top + 40 + 2 * (h + gap), labelW, h);
        durationLabel.setFont(labelFont);
        content.add(durationLabel);
        JTextField durationField = new JTextField(String.valueOf(SimulationConfig.DEFAULT_CLOUDLET_LENGTH));
        durationField.setBounds(left + labelW, top + 40 + 2 * (h + gap), inputW, h);
        durationField.setFont(inputFont);
        content.add(durationField);

        JLabel hostCpuLabel = new JLabel("Host CPU Capacity (MIPS)");
        hostCpuLabel.setBounds(left, top + 40 + 3 * (h + gap), labelW, h);
        hostCpuLabel.setFont(labelFont);
        content.add(hostCpuLabel);
        JTextField hostCpuField = new JTextField(String.valueOf((int) SimulationConfig.DEFAULT_BASE_HOST_PE_MIPS));
        hostCpuField.setBounds(left + labelW, top + 40 + 3 * (h + gap), inputW, h);
        hostCpuField.setFont(inputFont);
        content.add(hostCpuField);

        JLabel vmCpuLabel = new JLabel("VM CPU Requirement (MIPS)");
        vmCpuLabel.setBounds(left, top + 40 + 4 * (h + gap), labelW, h);
        vmCpuLabel.setFont(labelFont);
        content.add(vmCpuLabel);
        JTextField vmCpuField = new JTextField(String.valueOf((int) SimulationConfig.DEFAULT_VM_MIPS));
        vmCpuField.setBounds(left + labelW, top + 40 + 4 * (h + gap), inputW, h);
        vmCpuField.setFont(inputFont);
        content.add(vmCpuField);

        JButton start = new JButton("Start Simulation");
        start.setBounds(140, top + 40 + 5 * (h + gap) + 10, 240, 40);
        start.setBackground(new Color(0x2E7D32));
        start.setForeground(Color.WHITE);
        start.setFont(new Font("Segoe UI", Font.BOLD, 14));
        start.setFocusPainted(false);
        content.add(start);

        frame.add(content, BorderLayout.CENTER);
        frame.setVisible(true);

        start.addActionListener(e -> {
            // Basic validation
            try {
                int hosts = Integer.parseInt(hostsField.getText().trim());
                int vms = Integer.parseInt(vmsField.getText().trim());
                long duration = Long.parseLong(durationField.getText().trim());
                double hostCpu = Double.parseDouble(hostCpuField.getText().trim());
                double vmCpu = Double.parseDouble(vmCpuField.getText().trim());

                if (hosts <= 0 || vms <= 0 || duration <= 0 || hostCpu <= 0 || vmCpu <= 0) {
                    throw new NumberFormatException("Values must be positive.");
                }

                // Set system properties used by SimulationConfig
                System.setProperty(SimulationConfig.HOST_COUNT_PROPERTY, String.valueOf(hosts));
                System.setProperty(SimulationConfig.VM_COUNT_PROPERTY, String.valueOf(vms));
                System.setProperty(SimulationConfig.CLOUDLET_LENGTH_PROPERTY, String.valueOf(duration));
                System.setProperty(SimulationConfig.BASE_HOST_PE_MIPS_PROPERTY, String.valueOf(hostCpu));
                System.setProperty(SimulationConfig.VM_MIPS_PROPERTY, String.valueOf(vmCpu));

                // Run simulation in background while suppressing console output
                JProgressBar progress = new JProgressBar();
                progress.setIndeterminate(true);
                progress.setPreferredSize(new Dimension(400, 24));
                JPanel panel = new JPanel(new FlowLayout());
                panel.add(new JLabel("Simulation running..."));
                panel.add(progress);

                final JFrame progressFrame = new JFrame("Running");
                progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                progressFrame.getContentPane().add(panel);
                progressFrame.pack();
                progressFrame.setLocationRelativeTo(frame);
                progressFrame.setVisible(true);

                LiveDashboard liveDashboard = LiveDashboard.open(frame);

                // Redirect stdout/stderr to hide verbose console logs (restore later)
                PrintStream originalOut = System.out;
                PrintStream originalErr = System.err;
                ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
                ByteArrayOutputStream bufferErr = new ByteArrayOutputStream();
                System.setOut(new PrintStream(bufferOut));
                System.setErr(new PrintStream(bufferErr));

                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        CloudSimulation sim = new CloudSimulation(liveDashboard);
                        sim.initialize();
                        sim.run();
                        sim.printResults();
                        return null;
                    }

                    @Override
                    protected void done() {
                        // restore console
                        System.setOut(originalOut);
                        System.setErr(originalErr);
                        progressFrame.dispose();
                        liveDashboard.complete();
                    }
                };
                worker.execute();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid positive numeric values.\n" + ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
