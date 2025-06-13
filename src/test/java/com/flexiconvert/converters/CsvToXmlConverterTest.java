package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import static org.junit.jupiter.api.Assertions.*;

public class CsvToXmlConverterTest extends AbstractConverterTest {

    @Test
    public void testValidCsvToXmlConversion() throws Exception {
        String content = "1,Alice,30\n2,Bob,25";

        File input = createTempFile("sample.csv", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToXmlConverter converter = ctx.getBean(CsvToXmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "xml");
            assertTrue(output.exists(), "XML file should be created");

            String xml = Files.readString(output.toPath());
            assertTrue(xml.contains("<root>"), "Should contain <root>");
            assertTrue(xml.contains("<row>"), "Should contain <row>");
            assertTrue(xml.contains("<FIELD1>Alice</FIELD1>"), "Should contain converted field content");
        }
    }

    @Test
    public void testEmptyCsvThrowsException() throws Exception {
        File input = createTempFile("empty.csv", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToXmlConverter converter = ctx.getBean(CsvToXmlConverter.class);
            assertThrows(Exception.class, () -> converter.convert(input), "Should throw exception for empty CSV");
        }
    }

    @Test
    public void testCsvWithMissingValues() throws Exception {
        String content = "1,Alice\n2,Bob,25";  // row 1 is missing FIELD2

        File input = createTempFile("incomplete.csv", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            CsvToXmlConverter converter = ctx.getBean(CsvToXmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "xml");
            assertTrue(output.exists());

            // Parse the XML with DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(output);

            NodeList rows = doc.getElementsByTagName("row");
            assertEquals(2, rows.getLength(), "Should have 2 <row> elements");

            Element firstRow = (Element) rows.item(0);
            Node field2 = firstRow.getElementsByTagName("FIELD2").item(0);
            assertNotNull(field2, "FIELD2 should exist even if empty");
            assertEquals("", field2.getTextContent().trim(), "FIELD2 should be empty");
            System.out.println("== XML Preview ==");
            System.out.println(Files.readString(output.toPath()));
        }
    }
}
