package com.flexiconvert.ui.components;

import com.flexiconvert.ConversionType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FormatSelectionPanel extends JPanel {

    private final JComboBox<String> fromDropdown;
    private final JComboBox<String> toDropdown;
    private final Map<String, Set<String>> conversionMap = new TreeMap<>();
    private final List<String> allFromFormats = new ArrayList<>();

    public FormatSelectionPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setBorder(new TitledBorder("Select Conversion Type"));

        // Create standard non-editable dropdowns
        fromDropdown = new JComboBox<>();
        toDropdown = new JComboBox<>();
        
        buildConversionMapFromEnum();
        populateFromDropdown();

        fromDropdown.addActionListener(e -> updateToDropdown());

        add(new JLabel("From:"));
        add(fromDropdown);
        add(new JLabel("To:"));
        add(toDropdown);
    }

    private void buildConversionMapFromEnum() {
        for (ConversionType type : ConversionType.values()) {
            String[] parts = type.name().split("_TO_");
            if (parts.length == 2) {
                String from = parts[0].toLowerCase();
                String to = parts[1].toLowerCase();

                conversionMap
                    .computeIfAbsent(from, k -> new TreeSet<>())
                    .add(to);
            }
        }
    }

    private void populateFromDropdown() {
        fromDropdown.removeAllItems();
        allFromFormats.clear();
        
        for (String from : conversionMap.keySet()) {
            fromDropdown.addItem(from);
            allFromFormats.add(from);
        }
        
        if (fromDropdown.getItemCount() > 0) {
            fromDropdown.setSelectedIndex(0);
            updateToDropdown();
        }
    }

    private void updateToDropdown() {
        toDropdown.removeAllItems();
        String from = (String) fromDropdown.getSelectedItem();
        if (from != null) {
            Set<String> toOptions = conversionMap.getOrDefault(from, Set.of());
            
            for (String to : toOptions) {
                toDropdown.addItem(to);
            }
            
            if (!toOptions.isEmpty()) {
                toDropdown.setSelectedIndex(0);
            }
        }
    }

    public Optional<ConversionType> getSelectedConversionType() {
        String from = (String) fromDropdown.getSelectedItem();
        String to = (String) toDropdown.getSelectedItem();
        if (from != null && to != null) {
            String key = (from + "_TO_" + to).toUpperCase();
            try {
                return Optional.of(ConversionType.valueOf(key));
            } catch (IllegalArgumentException ignored) {}
        }
        return Optional.empty();
    }

    public void setFromFormat(String extension) {
        for (int i = 0; i < fromDropdown.getItemCount(); i++) {
            if (fromDropdown.getItemAt(i).equalsIgnoreCase(extension)) {
                fromDropdown.setSelectedIndex(i);
                return;
            }
        }
    }
}
