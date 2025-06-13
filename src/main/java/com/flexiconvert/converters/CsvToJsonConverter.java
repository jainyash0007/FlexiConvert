package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@Component
@ConverterFor(ConversionType.CSV_TO_JSON)
public class CsvToJsonConverter implements FormatConverter {

    private static final Logger LOGGER = Logger.getLogger(CsvToJsonConverter.class.getName());

    // Config flags
    private final boolean warnOnMismatch = true;
    private final boolean skipMalformedRows = false;
    private final boolean fillMissingFields = true;

    @Override
    public void convert(File inputFile) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        List<String> headers = null;

        try (
            Reader reader = new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8);
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                .withTrim(true)
                .withIgnoreSurroundingSpaces()
                .withIgnoreEmptyLines()
                .withAllowMissingColumnNames()
                .withSkipHeaderRecord(false)) // Treat all rows as data
        ) {
            Iterator<CSVRecord> iterator = parser.iterator();
                if (!iterator.hasNext()) {
                    throw new IOException("CSV file is empty or contains no rows.");
                }

                while (iterator.hasNext()) {
                    CSVRecord csvRecord = iterator.next();
                    if (headers == null) {
                        headers = new ArrayList<>();
                        for (int i = 0; i < csvRecord.size(); i++) {
                            headers.add("__" + i);
                        }
                    }
                if (headers == null) {
                    headers = new ArrayList<>();
                    for (int i = 0; i < csvRecord.size(); i++) {
                        headers.add("__" + i);
                    }
                }

                int actualSize = csvRecord.size();
                int expectedSize = headers.size();

                if (actualSize < expectedSize && warnOnMismatch) {
                    LOGGER.warning("Row " + csvRecord.getRecordNumber()
                            + " has fewer values than headers (" + actualSize + " < " + expectedSize + ")");
                }

                if (skipMalformedRows && actualSize < expectedSize) {
                    continue;
                }

                Map<String, Object> jsonRow = new LinkedHashMap<>();

                for (int i = 0; i < expectedSize; i++) {
                    String header = headers.get(i);
                    String value = i < actualSize ? csvRecord.get(i) : "";
                    jsonRow.put(header, parseTypedValue(value));
                }

                if (fillMissingFields && actualSize < expectedSize) {
                    for (int j = actualSize; j < expectedSize; j++) {
                        jsonRow.put("MISSING_FIELD_" + j, "");
                    }
                }

                records.add(jsonRow);
            }
        }

        File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            mapper.writeValue(writer, records);
        }
    }

    private String cleanKey(String raw) {
        return raw == null ? "" : raw.replaceAll("[\"\\n\\r]+", " ").trim();
    }

    private Object parseTypedValue(String val) {
        if (val == null || val.isBlank()) return "";
        try {
            if (val.contains(".")) return Double.parseDouble(val);
            return Integer.parseInt(val);
        } catch (NumberFormatException ex) {
            return val;
        }
    }

    private String getOutputFileName(File inputFile) {
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex != -1 ? name.substring(0, dotIndex) : name) + ".json";
    }
}
