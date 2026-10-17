package com.fot.ams;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Faculty of Technology Academic Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(760, 500);
            frame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            JLabel title = new JLabel(
                    "<html><center>Faculty of Technology<br>Academic Management System</center></html>",
                    SwingConstants.CENTER
            );
            title.setFont(new Font("SansSerif", Font.BOLD, 24));

            JTextArea info = new JTextArea();
            info.setEditable(false);
            info.setFont(new Font("SansSerif", Font.PLAIN, 15));
            info.setText(
                    "Project structure created for 4 group members.\n\n" +
                    "Member 01: User & Admin Management\n" +
                    "Member 02: Course & Academic Management\n" +
                    "Member 03: Marks & Grades\n" +
                    "Member 04: Attendance, Medical & Notices\n\n" +
                    "Next step: implement each module and connect it to MySQL."
            );

            panel.add(title, BorderLayout.NORTH);
            panel.add(info, BorderLayout.CENTER);
            frame.setContentPane(panel);
            frame.setVisible(true);
        });
    }
}
