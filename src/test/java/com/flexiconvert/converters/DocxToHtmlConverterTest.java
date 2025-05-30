package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class DocxToHtmlConverterTest extends AbstractConverterTest {

    @Test
    public void testDocxToHtmlConversion() throws Exception {
        // Create a simple DOCX with a paragraph
        File input = new File(tempDir.toFile(), "sample.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Hello from DOCX!");
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToHtmlConverter converter = ctx.getBean(DocxToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created");
            String html = Files.readString(output.toPath());
            assertTrue(html.contains("<p>Hello from DOCX!</p>"), "HTML should contain converted paragraph");
            assertTrue(html.contains("<html>") && html.contains("</html>"), "HTML should be well formed");
        }
    }

    @Test
    public void testEmptyDocxToHtmlConversion() throws Exception {
        File input = new File(tempDir.toFile(), "empty.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToHtmlConverter converter = ctx.getBean(DocxToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should still be created for empty DOCX");
            String html = Files.readString(output.toPath());
            assertTrue(html.contains("<body>") && html.contains("</body>"), "HTML body should exist even if empty");
        }
    }
}
