
package com.musicrec;

import com.musicrec.database.DatabaseManager;
import com.musicrec.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );

            DatabaseManager.initialize();
            MainFrame.launch();

        } catch (Exception e) {
            e.printStackTrace();

            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                            null,
                            "Application startup failed:\n" + e.getMessage(),
                            "Startup Error",
                            JOptionPane.ERROR_MESSAGE
                    )
            );
        }
    }
}
