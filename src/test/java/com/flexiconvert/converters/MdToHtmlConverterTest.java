package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class MdToHtmlConverterTest extends AbstractConverterTest {

    @Test
    public void testMarkdownToHtmlConversion() throws Exception {
        String markdown = "# Hello World\nThis is **Markdown** to HTML.";

        File input = createTempFile("sample.md", markdown);
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            MdToHtmlConverter converter = ctx.getBean(MdToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created from Markdown");
            String html = Files.readString(output.toPath());

            assertTrue(html.contains("<h1>Hello World</h1>"), "Should convert header");
            assertTrue(html.contains("<strong>Markdown</strong>"), "Should convert bold text");
            assertTrue(html.contains("<html>") && html.contains("</html>"), "Should include HTML scaffold");
        }
    }

    @Test
    public void testEmptyMarkdownCreatesHtmlScaffold() throws Exception {
        File input = createTempFile("empty.md", "");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            MdToHtmlConverter converter = ctx.getBean(MdToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created even for empty Markdown");
            String html = Files.readString(output.toPath());

            assertTrue(html.contains("<body>") && html.contains("</body>"), "Should include empty HTML body");
        }
    }
}
