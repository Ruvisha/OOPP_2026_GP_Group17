package com.fot.ams;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.ui.LoginFrame;

import javax.swing.*;

/**
 * Application Entry Point.
 * Faculty of Technology Academic Management System
 * University of Ruhuna - Group 17
 */
public class Main {
    public static void main(String[] args) {
        // Set modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }

        // Pre-flight database connectivity check
        SwingUtilities.invokeLater(() -> {
            boolean connected = DatabaseConnection.testConnection();
            if (!connected) {
                int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Unable to connect to MySQL database 'faculty_academic_management' on localhost:3306.\n\n" +
                    "Please ensure:\n" +
                    "1. XAMPP Control Panel is open and MySQL service is STARTED.\n" +
                    "2. Import 'database/group17_db.sql' into MySQL if you have not already.\n\n" +
                    "Do you still wish to open the login window?",
                    "Database Connection Notice",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (choice != JOptionPane.YES_OPTION) {
                    System.exit(0);
                    return;
                }
            }

            // Launch login interface
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
