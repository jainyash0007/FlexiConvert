package com.flexiconvert.converter.ui;

import com.flexiconvert.converter.ConversionType;
import com.flexiconvert.converter.FileConverterService;
import com.flexiconvert.converter.ui.components.FileInputPanel;
import com.flexiconvert.converter.ui.components.FormatSelectionPanel;
import com.flexiconvert.converter.ui.components.OutputPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class MainWindow extends JFrame {

    private final FileInputPanel fileInputPanel;
    private final FormatSelectionPanel formatSelectionPanel;
    private final OutputPanel outputPanel;
    private final JButton convertButton;
    private final JTextArea resultLog;

    public MainWindow() {
        setTitle("FlexiConvert");
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panels
        fileInputPanel = new FileInputPanel();
        formatSelectionPanel = new FormatSelectionPanel();
        outputPanel = new OutputPanel();

        fileInputPanel.setFileSelectedListener(file -> {
            String ext = getFileExtension(file.getName()).toLowerCase();
            formatSelectionPanel.setFromFormat(ext);
        });

        convertButton = new JButton("Convert");

        resultLog = new JTextArea(8, 50);
        resultLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(resultLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Conversion Log"));

        JPanel top = new JPanel(new BorderLayout());
        top.add(fileInputPanel, BorderLayout.NORTH);
        top.add(formatSelectionPanel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(outputPanel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        convertButton.setPreferredSize(new Dimension(150, 40));
        buttonPanel.add(convertButton);
        center.add(buttonPanel, BorderLayout.CENTER);
        center.add(logScroll, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // Theme toggle
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton themeToggle = new JButton("🌙");
        themeToggle.setToolTipText("Toggle Light/Dark Theme");
        themeToggle.addActionListener(e -> {
            boolean isDark = themeToggle.getText().equals("🌙");
            toggleTheme(isDark);
            themeToggle.setText(isDark ? "☀️" : "🌙");
        });
        themePanel.add(themeToggle);
        add(themePanel, BorderLayout.SOUTH);

        convertButton.addActionListener(e -> triggerConversion());
    }

    private void triggerConversion() {
        resultLog.setText("");

        String inputPath = fileInputPanel.getFilePath();
        File inputFile = new File(inputPath);

        if (!inputFile.exists() || !inputFile.isFile()) {
            resultLog.append("❌ Invalid file path.\n");
            return;
        }

        Optional<ConversionType> typeOpt = formatSelectionPanel.getSelectedConversionType();
        if (typeOpt.isEmpty()) {
            resultLog.append("❌ Invalid conversion type.\n");
            return;
        }

        ConversionType type = typeOpt.get();

        try {
            FileConverterService service = new FileConverterService();
            
            // Always convert to a temporary file first
            File tempOutput = service.convert(inputFile, type);
            
            // Store the temporary output file for the Download button
            outputPanel.setLastOutputFile(tempOutput);
            
            // If using default output directory, also save a copy there now
            File outputDir = outputPanel.getOutputDirectory();
            if (outputDir != null) {
                File uniqueDestFile = service.createUniqueFile(new File(outputDir, tempOutput.getName()));
                Files.copy(tempOutput.toPath(), uniqueDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                resultLog.append("✅ Saved to default output: " + uniqueDestFile.getAbsolutePath() + "\n");
            } else {
                resultLog.append("✅ Converted. Click 'Download...' to save it.\n");
            }
        } catch (Exception ex) {
            resultLog.append("❌ Conversion failed: " + ex.getMessage() + "\n");
        }
    }

    private void toggleTheme(boolean darkMode) {
        try {
            UIManager.setLookAndFeel(darkMode
                    ? "com.formdev.flatlaf.FlatDarkLaf"
                    : "com.formdev.flatlaf.FlatLightLaf");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to apply theme.");
        }
    }

    private String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(dot + 1) : "";
    }
}
