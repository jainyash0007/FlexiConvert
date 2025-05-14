package com.flexiconvert.converter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MainWindow extends JFrame {

    private JComboBox<ConversionType> conversionTypeDropdown;
    private JTextField filePathField;
    private JButton browseButton;
    private JButton convertButton;
    private JTextArea logArea;

    private File selectedFile;

    public MainWindow() {
        setTitle("FlexiConvert");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top panel (file input + browse)
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        filePathField = new JTextField();
        filePathField.setEditable(false);
        browseButton = new JButton("Browse");
        topPanel.add(filePathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);

        // Center panel (conversion type + button)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        conversionTypeDropdown = new JComboBox<>(ConversionType.values());
        convertButton = new JButton("Convert");

        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dropdownPanel.add(new JLabel("Conversion Type:"));
        dropdownPanel.add(conversionTypeDropdown);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(convertButton);

        centerPanel.add(dropdownPanel);
        centerPanel.add(buttonPanel);

        // Bottom panel (log area)
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        // Add all to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);

        // Action: Browse Button
        browseButton.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(MainWindow.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        // Action: Convert Button
        convertButton.addActionListener((ActionEvent e) -> {
            if (selectedFile == null || !selectedFile.exists()) {
                appendLog("Please select a valid input file.");
                return;
            }
            ConversionType selectedType = (ConversionType) conversionTypeDropdown.getSelectedItem();
            try {
                FileConverterService service = new FileConverterService();
                service.convert(selectedFile, selectedType);
                appendLog("✅ Conversion successful: " + selectedType.name());
            } catch (Exception ex) {
                appendLog("❌ Conversion failed: " + ex.getMessage());
            }
        });
    }

    private void appendLog(String message) {
        logArea.append(message + "\n");
    }
}
