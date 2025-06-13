package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;

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

    @Test
    public void testDocxWithStylesAndImage() throws Exception {
        File input = new File(tempDir.toFile(), "styled_image.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {

            // Heading style
            XWPFParagraph heading = doc.createParagraph();
            XWPFRun hRun = heading.createRun();
            hRun.setText("HEADING TEXT");
            hRun.setBold(true);

            // Bold + italic
            XWPFParagraph styled = doc.createParagraph();
            XWPFRun sRun = styled.createRun();
            sRun.setText("BoldItalic ");
            sRun.setBold(true);
            sRun.setItalic(true);

            // Numbered paragraph (bullet simulation)
            XWPFParagraph bullet = doc.createParagraph();
            bullet.setNumID(java.math.BigInteger.valueOf(1));
            bullet.createRun().setText("Bullet point line");

            // Add an embedded image (1x1 transparent PNG)
            XWPFParagraph imgPara = doc.createParagraph();
            XWPFRun imgRun = imgPara.createRun();
            byte[] imgBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEeQH9iUg1GAAAAABJRU5ErkJggg=="
            );
            imgRun.addPicture(new ByteArrayInputStream(imgBytes), Document.PICTURE_TYPE_PNG, "dot.png", 1, 1);

            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxToPdfConverter converter = ctx.getBean(DocxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF with styled content and image should be created");
            assertTrue(Files.size(output.toPath()) > 0, "PDF file should not be zero bytes");
        }
    }
}
