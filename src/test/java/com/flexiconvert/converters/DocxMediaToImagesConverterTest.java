package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class DocxMediaToImagesConverterTest extends AbstractConverterTest {

    @Test
    public void testDocxWithEmbeddedImage() throws Exception {
        // Create a dummy DOCX with embedded image
        File input = new File(tempDir.toFile(), "sample.docx");

        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {

            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText("This document has an image:");

            // Create a dummy image
            BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 100, 50);
            g.setColor(Color.WHITE);
            g.drawString("Test", 10, 25);
            g.dispose();

            File tempImg = new File(tempDir.toFile(), "temp-image.png");
            ImageIO.write(img, "png", tempImg);

            try (FileInputStream imageInput = new FileInputStream(tempImg)) {
                run.addPicture(imageInput, XWPFDocument.PICTURE_TYPE_PNG, "temp-image.png",
                        Units.toEMU(100), Units.toEMU(50));
            }

            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxMediaToImagesConverter converter = ctx.getBean(DocxMediaToImagesConverter.class);
            converter.convert(input);

            File outputDir = new File(tempDir.toFile(), "sample_media");
            assertTrue(outputDir.exists() && outputDir.isDirectory(), "Output directory should exist");

            File[] imageFiles = outputDir.listFiles((dir, name) -> name.matches("sample_image\\d+\\..+"));
            assertNotNull(imageFiles);
            assertTrue(imageFiles.length > 0, "At least one image should be extracted");
        }
    }

    @Test
    public void testDocxWithNoImagesDoesNothing() throws Exception {
        File input = createTempFile("noimage.docx", "");

        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(input)) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Just text, no images here.");
            doc.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            DocxMediaToImagesConverter converter = ctx.getBean(DocxMediaToImagesConverter.class);
            converter.convert(input);

            File outputDir = new File(tempDir.toFile(), "noimage_media");
            assertTrue(outputDir.exists(), "Output dir should still be created even if empty");

            File[] imageFiles = outputDir.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
            assertNotNull(imageFiles);
            assertEquals(0, imageFiles.length, "No images should be extracted");
        }
    }
}
