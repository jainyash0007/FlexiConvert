package com.flexiconvert.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class CsvToJsonConverterTest extends AbstractConverterTest {

    @Test
    public void testValidCsvToJsonConversion() throws Exception {
        // Simulate 3 columns
        String content = "1,Alice,30\n2,Bob,25";

        File input = createTempFile("sample.csv", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToJsonConverter converter = ctx.getBean(CsvToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            assertTrue(output.exists(), "JSON output should be created");
            String json = Files.readString(output.toPath());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            assertEquals(2, root.size(), "Should contain 2 rows");
            assertEquals("Alice", root.get(0).get("__1").asText());
            assertEquals(30, root.get(0).get("__2").asInt());
            assertEquals("Bob", root.get(1).get("__1").asText());
        }
    }

    @Test
    public void testEmptyCsvThrowsException() throws Exception {
        File input = createTempFile("empty.csv", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToJsonConverter converter = ctx.getBean(CsvToJsonConverter.class);
            assertThrows(IOException.class, () -> converter.convert(input), "Should throw IOException for empty CSV file");
        }
    }

    @Test
    public void testCsvWithMissingValues() throws Exception {
        String content = "1,Alice\n2,Bob,25"; // First row has only 2 values

        File input = createTempFile("incomplete.csv", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToJsonConverter converter = ctx.getBean(CsvToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            assertTrue(output.exists(), "Output should still be created even with missing values");

            String json = Files.readString(output.toPath());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            assertEquals(2, root.size());
            assertEquals("Alice", root.get(0).get("__1").asText());
            assertEquals("", root.get(0).path("__2").asText(""), "Missing age should be empty string in first row");
        }
    }
}
