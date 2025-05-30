package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonToCsvConverterTest extends AbstractConverterTest {

    @Test
    public void testJsonToCsvConversion() throws Exception {
        // Create a simple JSON array with objects
        String jsonContent = """
            [
              {
                "id": 1,
                "name": "John Doe",
                "email": "john@example.com"
              },
              {
                "id": 2,
                "name": "Jane Smith",
                "email": "jane@example.com",
                "active": true
              },
              {
                "id": 3,
                "name": "Bob Johnson",
                "phone": "555-1234"
              }
            ]
            """;

        File input = createTempFile("test.json", jsonContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToCsvConverter converter = ctx.getBean(JsonToCsvConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "csv");
            assertTrue(output.exists(), "CSV file should be created from JSON");
            assertTrue(output.length() > 0, "CSV file should not be empty");

            // Verify the CSV content
            List<String> lines = readLines(output);
            assertEquals(4, lines.size(), "CSV should have header plus 3 data rows");
            
            // Check header
            String headerLine = lines.get(0);
            assertTrue(headerLine.contains("id"), "Header should contain id");
            assertTrue(headerLine.contains("name"), "Header should contain name");
            assertTrue(headerLine.contains("email"), "Header should contain email");
            assertTrue(headerLine.contains("active"), "Header should contain active");
            assertTrue(headerLine.contains("phone"), "Header should contain phone");
            
            // Check data rows
            String row1 = lines.get(1);
            assertTrue(row1.contains("1"), "Row 1 should contain id 1");
            assertTrue(row1.contains("John Doe"), "Row 1 should contain name John Doe");
            assertTrue(row1.contains("john@example.com"), "Row 1 should contain email");
            
            String row2 = lines.get(2);
            assertTrue(row2.contains("2"), "Row 2 should contain id 2");
            assertTrue(row2.contains("true"), "Row 2 should contain active=true");
        }
    }

    @Test
    public void testEmptyJsonArray() throws Exception {
        String jsonContent = "[]";

        File input = createTempFile("empty.json", jsonContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToCsvConverter converter = ctx.getBean(JsonToCsvConverter.class);
            
            // The converter should throw IOException for empty arrays
            assertThrows(IOException.class, () -> converter.convert(input), 
                    "Should throw IOException for empty JSON array");
        }
    }

    @Test
    public void testInvalidJson() throws Exception {
        // Create invalid JSON content
        String jsonContent = "{ this is not valid json }";

        File input = createTempFile("invalid.json", jsonContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToCsvConverter converter = ctx.getBean(JsonToCsvConverter.class);
            
            // Should throw an exception when trying to parse invalid JSON
            assertThrows(IOException.class, () -> converter.convert(input), 
                    "Should throw IOException for invalid JSON");
        }
    }
    
    @Test
    public void testNestedJsonStructure() throws Exception {
        // Create JSON with nested objects and arrays
        String jsonContent = """
            [
              {
                "id": 1,
                "name": "John Doe",
                "address": {
                  "street": "123 Main St",
                  "city": "Anytown"
                }
              },
              {
                "id": 2,
                "name": "Jane Smith",
                "tags": ["developer", "designer"]
              }
            ]
            """;

        File input = createTempFile("nested.json", jsonContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToCsvConverter converter = ctx.getBean(JsonToCsvConverter.class);
            
            // The converter should handle nested structures by flattening or using string representation
            converter.convert(input);
            
            File output = getOutputFile(input, "csv");
            assertTrue(output.exists(), "CSV file should be created from JSON with nested structures");
        }
    }
    
    private List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}