package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@ConverterFor(ConversionType.CSV_TO_XML)
public class CsvToXmlConverter implements FormatConverter {

    private final boolean warnOnMismatch = true;
    private final boolean skipMalformedRows = false;

    @Override
    public void convert(File inputFile) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        int maxCols = 0;

        // First pass: read all rows and track max columns
        try (
            Reader reader = new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8);
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                .withTrim()
                .withIgnoreSurroundingSpaces()
                .withIgnoreEmptyLines()
                .withAllowMissingColumnNames()
                .withSkipHeaderRecord(false))
        ) {
            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                for (String val : record) {
                    row.add(escapeXml(val));
                }
                maxCols = Math.max(maxCols, row.size());
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new IOException("CSV file is empty or contains no rows.");
        }

        // Generate FIELD0, FIELD1, ..., FIELDn
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < maxCols; i++) {
            headers.add("FIELD" + i);
        }

        File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n");

            for (List<String> row : rows) {
                writer.write("  <row>\n");
                for (int i = 0; i < headers.size(); i++) {
                    String tag = headers.get(i);
                    String val = i < row.size() ? row.get(i) : "";
                    writer.write(String.format("    <%s>%s</%s>\n", tag, val, tag));
                }
                writer.write("  </row>\n");
            }

            writer.write("</root>\n");
        }
    }

    private String escapeXml(String input) {
        return input == null ? "" :
            input.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&apos;");
    }

    private String getOutputFileName(File inputFile) {
        String name = inputFile.getName();
        int dot = name.lastIndexOf('.');
        return (dot != -1 ? name.substring(0, dot) : name) + ".xml";
    }
}
