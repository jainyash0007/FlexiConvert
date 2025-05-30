package com.flexiconvert;

import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FileConverterServiceTest extends AbstractConverterTest {

    private final FileConverterService service;

    public FileConverterServiceTest() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            service = ctx.getBean(FileConverterService.class);
        }
    }

    @Test
    public void testMarkdownToHtmlConversion() throws Exception {
        File input = createTempFile("sample.md", "# Hello\nThis is **Markdown**");
        File output = service.convert(input, ConversionType.MD_TO_HTML);

        assertNotNull(output);
        assertTrue(output.exists());
        assertTrue(output.getName().endsWith(".html"));
    }

    @Test
    public void testInvalidConversionType() {
        File dummy = new File("fake.txt");

        assertThrows(UnsupportedOperationException.class, () ->
            service.convert(dummy, null)
        );
    }
}
