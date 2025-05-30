package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class JsonToXmlConverterTest extends AbstractConverterTest {

    @Test
    public void testJsonObjectToXmlConversion() throws Exception {
        String content = "{\"id\": 1, \"name\": \"Alice\"}";

        File input = createTempFile("object.json", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToXmlConverter converter = ctx.getBean(JsonToXmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "xml");
            assertTrue(output.exists(), "XML file should be created");
            String xml = Files.readString(output.toPath());

            assertTrue(xml.contains("<id>1</id>"), "Should contain converted <id>");
            assertTrue(xml.contains("<name>Alice</name>"), "Should contain converted <name>");
        }
    }

    @Test
    public void testJsonArrayToXmlConversion() throws Exception {
        String content = "[{\"id\": 1}, {\"id\": 2}]";

        File input = createTempFile("array.json", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToXmlConverter converter = ctx.getBean(JsonToXmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "xml");
            assertTrue(output.exists(), "XML file should be created");
            String xml = Files.readString(output.toPath());

            assertTrue(xml.contains("<record>"), "Array elements should be wrapped as <record>");
            assertTrue(xml.contains("<id>1</id>") && xml.contains("<id>2</id>"), "Should convert both elements");
        }
    }

    @Test
    public void testInvalidJsonThrowsException() throws Exception {
        File input = createTempFile("invalid.json", "{ this is not valid JSON");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JsonToXmlConverter converter = ctx.getBean(JsonToXmlConverter.class);
            assertThrows(Exception.class, () -> converter.convert(input), "Should throw on malformed JSON");
        }
    }
}
