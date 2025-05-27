package com.flexiconvert.converter.ui.components;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.datatransfer.DataFlavor;

public class FileInputPanel extends JPanel {

    private final JTextField filePathField;
    private final JButton browseButton;
    private final JComboBox<String> historyDropdown;

    private final List<String> fileHistory = new ArrayList<>();
    
    private FileSelectedListener fileSelectedListener;

    public FileInputPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new TitledBorder("Select File"));

        filePathField = new JTextField();
        browseButton = new JButton("Browse");
        historyDropdown = new JComboBox<>();
        historyDropdown.setToolTipText("Recently used files");
        
        filePathField.setEditable(true);
        filePathField.setToolTipText("Enter path, browse, or drag a file");

        // File drop support on whole panel
        new DropTarget(this, new FileDropTarget());

        // File browser
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                updateFilePath(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // No auto-completion for now - just handle enter key
        filePathField.addActionListener(e -> updateFilePath(filePathField.getText().trim()));
        
        // History dropdown handling
        historyDropdown.addActionListener(e -> {
            String selected = (String)historyDropdown.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                filePathField.setText(selected);
                updateFilePath(selected);
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(filePathField, BorderLayout.CENTER);
        centerPanel.add(browseButton, BorderLayout.EAST);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JLabel("History:"), BorderLayout.WEST);
        bottomPanel.add(historyDropdown, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setFileSelectedListener(FileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    public String getFilePath() {
        return filePathField.getText().trim();
    }

    public void updateFilePath(String path) {
        File file = new File(path);
        filePathField.setText(path);

        if (file.exists() && file.isFile()) {
            if (!fileHistory.contains(path)) {
                fileHistory.add(0, path);
                updateHistoryDropdown();
            }

            if (fileSelectedListener != null) {
                fileSelectedListener.onFileSelected(file);
            }
        }
    }
    
    private void updateHistoryDropdown() {
        historyDropdown.removeAllItems();
        for (String path : fileHistory) {
            historyDropdown.addItem(path);
        }
    }

    private class FileDropTarget extends DropTargetAdapter {
        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                List<File> dropped = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (!dropped.isEmpty()) {
                    updateFilePath(dropped.get(0).getAbsolutePath());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(FileInputPanel.this, "Failed to read dropped file.");
            }
        }
    }

    public interface FileSelectedListener {
        void onFileSelected(File file);
    }
}
