package com.parking;

import com.parking.service.ParkingManager;
import com.parking.ui.MainFrame;

import javax.swing.*;

/**
 * Application Entry Point.
 * Initializes the Swing look-and-feel, business manager, and primary UI window.
 */
public class Main {
    public static void main(String[] args) {
        // Configure modern UI look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Launch Application on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                ParkingManager manager = new ParkingManager();
                MainFrame mainFrame = new MainFrame(manager);
                mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch Parking Management System: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
