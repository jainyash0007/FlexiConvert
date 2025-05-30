package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class PdfToImagesConverterTest extends AbstractConverterTest {

    @Test
    public void testSinglePagePdfToImage() throws Exception {
        File input = new File(tempDir.toFile(), "single.pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(input);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PdfToImagesConverter converter = ctx.getBean(PdfToImagesConverter.class);
            converter.convert(input);

            File output = new File(tempDir.toFile(), "single_page1.png");
            assertTrue(output.exists(), "Image for page 1 should be created");
            assertTrue(Files.size(output.toPath()) > 0, "Image should not be empty");
        }
    }

    @Test
    public void testMultiPagePdfToImages() throws Exception {
        File input = new File(tempDir.toFile(), "multi.pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.addPage(new PDPage());
            doc.save(input);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PdfToImagesConverter converter = ctx.getBean(PdfToImagesConverter.class);
            converter.convert(input);

            File page1 = new File(tempDir.toFile(), "multi_page1.png");
            File page2 = new File(tempDir.toFile(), "multi_page2.png");

            assertTrue(page1.exists(), "Page 1 image should be created");
            assertTrue(page2.exists(), "Page 2 image should be created");
        }
    }
}
