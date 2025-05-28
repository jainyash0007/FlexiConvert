package com.flexiconvert.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexiconvert.interfaces.FormatConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class JsonToCsvConverter implements FormatConverter {
    @Override
    public void convert(File inputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> records;
        try (Reader reader = Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
            records = mapper.readValue(reader, new TypeReference<List<Map<String, Object>>>() {});
        }

        if (records.isEmpty()) {
            throw new IOException("JSON array is empty.");
        }

        // Get all unique headers across records (preserves order of first record)
        Set<String> headers = new LinkedHashSet<>(records.get(0).keySet());
        for (Map<String, Object> record : records) {
            headers.addAll(record.keySet());
        }

        // Create output file
        File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
            // Write header row
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write data rows
            for (Map<String, Object> record : records) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object val = record.getOrDefault(header, "");
                    values.add(val != null ? val.toString().replaceAll(",", " ") : "");
                }
                writer.write(String.join(",", values));
                writer.newLine();
            }
        }
    }

    private String getOutputFileName(File inputFile) {
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            name = name.substring(0, dotIndex);
        }
        return name + ".csv";
    }
}
