package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class CsvToXmlConverterTest extends AbstractConverterTest {

    @Test
    public void testValidCsvToXmlConversion() throws Exception {
        String content = "id,name,age\n1,Alice,30\n2,Bob,25";

        File input = createTempFile("sample.csv", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToXmlConverter converter = ctx.getBean(CsvToXmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "xml");
            assertTrue(output.exists(), "XML file should be created from CSV");

            String xml = Files.readString(output.toPath());
            assertTrue(xml.contains("<records>"), "Should contain <records>");
            assertTrue(xml.contains("<record>"), "Should contain <record>");
            assertTrue(xml.contains("<name>Alice</name>"), "Should contain converted data");
        }
    }

    @Test
    public void testEmptyCsvThrowsException() throws Exception {
        File input = createTempFile("empty.csv", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToXmlConverter converter = ctx.getBean(CsvToXmlConverter.class);
            assertThrows(IOException.class, () -> converter.convert(input), "Should throw IOException for empty CSV");
        }
    }

    @Test
    public void testCsvWithMissingValues() throws Exception {
        String content = "id,name,age\n1,Alice\n2,Bob,25";  // missing age in first row

        File input = createTempFile("incomplete.csv", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToXmlConverter converter = ctx.getBean(CsvToXmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "xml");
            assertTrue(output.exists(), "Output XML should be created");
            String xml = Files.readString(output.toPath());

            assertTrue(xml.contains("<age></age>") || xml.contains("<age/>"),
                "Missing values should be written as empty XML elements");
        }
    }
}
