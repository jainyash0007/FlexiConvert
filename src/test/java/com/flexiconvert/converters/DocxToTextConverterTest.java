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

public class DocxToTextConverterTest extends AbstractConverterTest {

    @Test
    public void testDocxToTextConversion() throws Exception {
        File input = new File(tempDir.toFile(), "sample.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Extract this text.");
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToTextConverter converter = ctx.getBean(DocxToTextConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "txt");
            assertTrue(output.exists(), "Text file should be created from DOCX");
            String content = Files.readString(output.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(content.contains("Extract this text."), "Text file should contain expected content");
        }
    }

    @Test
    public void testEmptyDocxStillCreatesTxt() throws Exception {
        File input = new File(tempDir.toFile(), "empty.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToTextConverter converter = ctx.getBean(DocxToTextConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "txt");
            assertTrue(output.exists(), "Text file should be created for empty DOCX");
            String content = Files.readString(output.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(content.isBlank(), "Text file should be empty for empty DOCX");
        }
    }
}
