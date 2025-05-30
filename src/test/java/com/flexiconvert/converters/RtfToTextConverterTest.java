package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class RtfToTextConverterTest extends AbstractConverterTest {

    @Test
    public void testRtfToTextConversion() throws Exception {
        // Valid RTF content
        String rtf = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Courier;}}\n" +
                     "\\f0\\fs24 Hello RTF World!\n}";

        File input = createTempFile("sample.rtf", rtf);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            RtfToTextConverter converter = ctx.getBean(RtfToTextConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "txt");
            assertTrue(output.exists(), "TXT file should be created from RTF");
            String content = Files.readString(output.toPath());
            assertTrue(content.contains("Hello RTF World"), "Extracted text should be correct");
        }
    }

    @Test
    public void testEmptyRtfThrowsException() throws Exception {
        File input = createTempFile("empty.rtf", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            RtfToTextConverter converter = ctx.getBean(RtfToTextConverter.class);
            assertThrows(Exception.class, () -> converter.convert(input), 
                    "Should throw for empty RTF content");
        }
    }

    @Test
    public void testMalformedRtfThrowsException() throws Exception {
        File input = createTempFile("bad.rtf", "{ this is not valid RTF }");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            RtfToTextConverter converter = ctx.getBean(RtfToTextConverter.class);
            assertThrows(IOException.class, () -> converter.convert(input),
                    "Should throw for malformed RTF content");
        }
    }
}
