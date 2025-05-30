package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class MdToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testMarkdownToPdfConversion() throws Exception {
        String markdown = "# Title\n\nThis is **bold** text converted to PDF.";

        File input = createTempFile("test.md", markdown);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            MdToPdfConverter converter = ctx.getBean(MdToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from Markdown");
            assertTrue(Files.size(output.toPath()) > 0, "PDF should not be empty");
        }
    }

    @Test
    public void testEmptyMarkdownStillCreatesPdf() throws Exception {
        File input = createTempFile("empty.md", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            MdToPdfConverter converter = ctx.getBean(MdToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should still be created for empty Markdown");
            assertTrue(Files.size(output.toPath()) > 0, "PDF file should not be zero bytes");
        }
    }
}
