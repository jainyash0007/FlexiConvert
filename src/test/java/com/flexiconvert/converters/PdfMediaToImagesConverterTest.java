package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PdfMediaToImagesConverterTest extends AbstractConverterTest {

    @Test
    public void testPdfWithEmbeddedImage() throws Exception {
        // Create a PDF with one embedded image
        File input = new File(tempDir.toFile(), "sample.pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create a dummy image
            BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, 100, 50);
            g.dispose();

            File imgFile = new File(tempDir.toFile(), "image.png");
            ImageIO.write(img, "png", imgFile);

            PDImageXObject pdImage = PDImageXObject.createFromFileByExtension(imgFile, document);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.drawImage(pdImage, 100, 600);
            }

            document.save(input);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PdfMediaToImagesConverter converter = ctx.getBean(PdfMediaToImagesConverter.class);
            converter.convert(input);

            File outputDir = new File(tempDir.toFile(), "sample_media");
            assertTrue(outputDir.exists() && outputDir.isDirectory(), "Output directory should exist");

            File[] imageFiles = outputDir.listFiles((dir, name) -> name.endsWith(".png"));
            assertNotNull(imageFiles);
            assertTrue(imageFiles.length > 0, "At least one image should be extracted");
        }
    }

    @Test
    public void testPdfWithNoImagesCreatesEmptyDir() throws Exception {
        File input = new File(tempDir.toFile(), "noimage.pdf");

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage(PDRectangle.A4));
            doc.save(input);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PdfMediaToImagesConverter converter = ctx.getBean(PdfMediaToImagesConverter.class);
            converter.convert(input);

            File outputDir = new File(tempDir.toFile(), "noimage_media");
            assertTrue(outputDir.exists(), "Directory should still be created");
            File[] imageFiles = outputDir.listFiles((dir, name) -> name.endsWith(".png"));
            assertNotNull(imageFiles);
            assertEquals(0, imageFiles.length, "No image files should be extracted");
        }
    }
}
