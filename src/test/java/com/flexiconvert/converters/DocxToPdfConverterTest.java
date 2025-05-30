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

public class DocxToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testDocxToPdfConversion() throws Exception {
        File input = new File(tempDir.toFile(), "sample.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("This should go into a PDF.");
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToPdfConverter converter = ctx.getBean(DocxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF should be created");
            assertTrue(Files.size(output.toPath()) > 0, "PDF should not be empty");
        }
    }

    @Test
    public void testEmptyDocxStillCreatesPdf() throws Exception {
        File input = new File(tempDir.toFile(), "empty.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToPdfConverter converter = ctx.getBean(DocxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF should still be created for empty DOCX");
            assertTrue(Files.size(output.toPath()) > 0, "PDF file should not be zero bytes");
        }
    }
}
