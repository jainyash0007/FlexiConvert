package com.flexiconvert.converter.ui.components;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class OutputPanel extends JPanel {

    private final JButton downloadButton;
    private final JCheckBox useDefaultDirToggle;
    private final JButton changeDefaultDirButton;
    private final JButton openOutputFolderButton;
    private final JLabel outputStatusLabel;

    private File defaultOutputDir = new File(System.getProperty("user.home") + File.separator + "FlexiConvert-Output");
    private File lastOutputFile;

    public OutputPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setBorder(new TitledBorder("Output Options"));

        downloadButton = new JButton("Download...");
        useDefaultDirToggle = new JCheckBox("Use Default Output Directory", true);
        changeDefaultDirButton = new JButton("Change...");
        openOutputFolderButton = new JButton("Open Output Folder");
        outputStatusLabel = new JLabel("Default: " + defaultOutputDir.getAbsolutePath());

        add(downloadButton);
        add(useDefaultDirToggle);
        add(changeDefaultDirButton);
        add(openOutputFolderButton);
        add(outputStatusLabel);

        createDefaultDirIfNeeded();

        useDefaultDirToggle.addActionListener(e -> updateStatusLabel());
        changeDefaultDirButton.addActionListener(e -> openDirectoryChooser());
        openOutputFolderButton.addActionListener(e -> openDirectory(defaultOutputDir));

        downloadButton.addActionListener(e -> {
            if (lastOutputFile == null || !lastOutputFile.exists()) {
                JOptionPane.showMessageDialog(this,
                        "No converted file available to download.",
                        "No File Available",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            saveFileWithDialog();
        });
    }

    public File getOutputDirectory() {
        return useDefaultDirToggle.isSelected() ? defaultOutputDir : null;
    }

    public void setLastOutputFile(File file) {
        this.lastOutputFile = file;
    }

    private void openDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            defaultOutputDir = chooser.getSelectedFile();
            updateStatusLabel();
        }
    }

    private void updateStatusLabel() {
        String path = useDefaultDirToggle.isSelected()
                ? "Default: " + defaultOutputDir.getAbsolutePath()
                : "Manual selection on each download";
        outputStatusLabel.setText(path);
    }

    private void openDirectory(File dir) {
        try {
            if (!Desktop.isDesktopSupported()) return;
            Desktop.getDesktop().open(dir);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to open folder: " + e.getMessage());
        }
    }

    private void createDefaultDirIfNeeded() {
        if (!defaultOutputDir.exists()) {
            defaultOutputDir.mkdirs();
        }
    }

    private void saveFileWithDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save As");
        chooser.setSelectedFile(new File(lastOutputFile.getName()));
        
        boolean fileSaved = false;
        
        while (!fileSaved) {
            int result = chooser.showSaveDialog(this);
            
            if (result != JFileChooser.APPROVE_OPTION) {
                // User canceled, just exit
                return;
            }
            
            File destFile = chooser.getSelectedFile();
            
            if (destFile.exists()) {
                int response = JOptionPane.showConfirmDialog(
                    this,
                    "File already exists:\n" + destFile.getName() + "\nDo you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_CANCEL_OPTION
                );
                
                if (response == JOptionPane.CANCEL_OPTION) {
                    // User wants to cancel completely
                    return;
                } else if (response == JOptionPane.NO_OPTION) {
                    // User wants to choose a different name, loop again
                    continue;
                }
                // YES option - proceed with overwrite
            }
            
            try {
                java.nio.file.Files.copy(
                        lastOutputFile.toPath(),
                        destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
                
                JOptionPane.showMessageDialog(this,
                        "File successfully saved to:\n" + destFile.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                fileSaved = true; // Exit loop
            } catch (IOException ex) {
                int retry = JOptionPane.showConfirmDialog(
                    this,
                    "Error saving file: " + ex.getMessage() + "\nTry again?",
                    "Download Failed",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (retry != JOptionPane.YES_OPTION) {
                    return; // Exit if user doesn't want to retry
                }
                // Otherwise loop again
            }
        }
    }
}
