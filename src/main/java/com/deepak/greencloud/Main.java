package com.deepak.greencloud;

import javax.swing.SwingUtilities;
import com.deepak.greencloud.ui.SimulationLauncher;

/**
 * Entry point for the Green Cloud Framework GUI launcher.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimulationLauncher::showLauncher);
    }
}
