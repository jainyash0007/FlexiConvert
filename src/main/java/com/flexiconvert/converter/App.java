package com.flexiconvert.converter;

import javax.swing.SwingUtilities;

import com.flexiconvert.converter.ui.MainWindow;

public class App {
    public static void main(String[] args) {
        // invokeLater to ensure GUI creation is on the Event Dispatch Thread
        // and to avoid blocking the main thread
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
