package com.flexiconvert;

import com.flexiconvert.config.AppConfig;
import com.flexiconvert.ui.MainWindow;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            FileConverterService converterService = context.getBean(FileConverterService.class);

            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow(converterService); // updated constructor
                window.setVisible(true);
            });
        }
    }
}
