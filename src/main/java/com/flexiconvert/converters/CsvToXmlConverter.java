package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;


@Component
@ConverterFor(ConversionType.CSV_TO_XML)
public class CsvToXmlConverter implements FormatConverter {
    @Override
    public void convert(File inputFile) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty.");
            }

            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1);
                Map<String, String> record = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    record.put(headers[i], i < values.length ? values[i].trim() : "");
                }
                records.add(record);
            }
        }

        File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write("<records>\n");

            for (Map<String, String> record : records) {
                writer.write("  <record>\n");
                for (Map.Entry<String, String> entry : record.entrySet()) {
                    writer.write(String.format("    <%s>%s</%s>\n",
                            escapeXml(entry.getKey()),
                            escapeXml(entry.getValue()),
                            escapeXml(entry.getKey())));
                }
                writer.write("  </record>\n");
            }

            writer.write("</records>\n");
        }
    }

    private String escapeXml(String input) {
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    private String getOutputFileName(File inputFile) {
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            name = name.substring(0, dotIndex);
        }
        return name + ".xml";
    }
}
