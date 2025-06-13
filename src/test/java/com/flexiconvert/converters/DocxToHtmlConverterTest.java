package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class DocxToHtmlConverterTest extends AbstractConverterTest {

    @Test
    public void testDocxToHtmlWithFormattingAndImage() throws Exception {
        File input = new File(tempDir.toFile(), "sample.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {

            // Paragraph with styled text and hyperlink
            XWPFParagraph para = doc.createParagraph();
            XWPFRun boldItalic = para.createRun();
            boldItalic.setBold(true);
            boldItalic.setItalic(true);
            boldItalic.setText("BoldItalicText ");

            XWPFHyperlinkRun hyperlink = para.insertNewHyperlinkRun(1, "http://example.com");
            hyperlink.setText("ExampleLink");

            // Paragraph with embedded image
            para = doc.createParagraph();
            XWPFRun imgRun = para.createRun();
            byte[] imgBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEeQH9iUg1GAAAAABJRU5ErkJggg=="
            ); // 1x1 PNG
            imgRun.addPicture(new ByteArrayInputStream(imgBytes), Document.PICTURE_TYPE_PNG, "dot.png", 1, 1);

            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToHtmlConverter converter = ctx.getBean(DocxToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML output should be created");

            String html = Files.readString(output.toPath());
            assertTrue(html.contains("BoldItalicText"), "Text content should be present");
            assertTrue(html.contains("<b>") && html.contains("<i>"), "Bold and italic tags should be present");
            assertTrue(html.contains("<a href=\"http://example.com\">ExampleLink</a>"), "Hyperlink should be present");
            assertTrue(html.contains("<img src=\"") && html.contains(".png"), "Image tag with .png file URI should be present");
            assertTrue(html.contains("</html>"), "HTML should be well formed");
        }
    }

    @Test
    public void testDocxToHtmlHandlesEmptyFile() throws Exception {
        File input = new File(tempDir.toFile(), "empty.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToHtmlConverter converter = ctx.getBean(DocxToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML output should be created");
            String html = Files.readString(output.toPath());
            assertTrue(html.contains("<body>"), "Body should be present");
        }
    }
}
