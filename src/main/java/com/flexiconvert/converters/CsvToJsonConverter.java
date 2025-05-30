package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;


@Component
@ConverterFor(ConversionType.CSV_TO_JSON)
public class CsvToJsonConverter implements FormatConverter {
    @Override
    public void convert(File inputFile) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty.");
            }

            // Split the header by comma
            String[] headers = headerLine.split(",");
            // Trim header values to remove any extra spaces
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            // Read data rows
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the data line by comma
                String[] values = line.split(",", -1); // -1 preserves empty fields
                Map<String, String> record = new LinkedHashMap<>();

                // Create a map of header -> value for each row
                for (int i = 0; i < headers.length; i++) {
                    record.put(headers[i], i < values.length ? values[i].trim() : "");
                }
                records.add(record);
            }
        }

        // Create output file in the same folder with .json extension
        File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));

        // Use Jackson to write the JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            mapper.writeValue(writer, records);
        }
    }

    private String getOutputFileName(File inputFile) {
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            name = name.substring(0, dotIndex);
        }
        return name + ".json";
    }
}
