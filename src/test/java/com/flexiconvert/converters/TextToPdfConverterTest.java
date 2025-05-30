package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class TextToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testTextToPdfConversion() throws Exception {
        String content = "Line 1\nLine 2\nLine 3";

        File input = createTempFile("example.txt", content);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TextToPdfConverter converter = ctx.getBean(TextToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from TXT");
            assertTrue(Files.size(output.toPath()) > 0, "PDF should not be empty");
        }
    }

    @Test
    public void testEmptyTextFileStillCreatesPdf() throws Exception {
        File input = createTempFile("empty.txt", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TextToPdfConverter converter = ctx.getBean(TextToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF should be created for empty TXT");
            assertTrue(Files.size(output.toPath()) > 0, "PDF should not be zero bytes");
        }
    }
}
