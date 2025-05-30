package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class RtfToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testRtfToPdfConversion() throws Exception {
        String content = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Courier;}}\n" +
                         "\\f0\\fs24 Hello RTF World!\n}";

        File input = createTempFile("sample.rtf", content);
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            RtfToPdfConverter converter = ctx.getBean(RtfToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from RTF");
            assertTrue(Files.size(output.toPath()) > 0, "PDF file should not be empty");
        }
    }

    @Test
    public void testEmptyRtfFile() throws Exception {
        File input = createTempFile("empty.rtf", "");

        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            RtfToPdfConverter converter = ctx.getBean(RtfToPdfConverter.class);
            assertThrows(IOException.class, () -> converter.convert(input));
        }
    }
}
