package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PptxMediaToImagesConverterTest extends AbstractConverterTest {

    @Test
    public void testPptxWithEmbeddedImage() throws Exception {
        // Create a dummy image
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, 100, 50);
        g.dispose();

        File imageFile = new File(tempDir.toFile(), "image.png");
        ImageIO.write(img, "png", imageFile);

        File input = new File(tempDir.toFile(), "slides.pptx");
        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(input)) {

            try (var is = imageFile.toURI().toURL().openStream()) {
                byte[] imgBytes = IOUtils.toByteArray(is);
                ppt.addPicture(imgBytes, PictureData.PictureType.PNG);
            }
            ppt.createSlide(); // ensure at least one slide
            ppt.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PptxMediaToImagesConverter converter = ctx.getBean(PptxMediaToImagesConverter.class);
            converter.convert(input);

            File outputDir = new File(tempDir.toFile(), "slides_media");
            assertTrue(outputDir.exists() && outputDir.isDirectory(), "Output dir should be created");

            File[] files = outputDir.listFiles((dir, name) -> name.endsWith(".png"));
            assertNotNull(files);
            assertTrue(files.length > 0, "At least one image should be extracted");
        }
    }

    @Test
    public void testPptxWithNoImages() throws Exception {
        File input = new File(tempDir.toFile(), "empty.pptx");
        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(input)) {
            ppt.createSlide();
            ppt.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PptxMediaToImagesConverter converter = ctx.getBean(PptxMediaToImagesConverter.class);
            converter.convert(input);

            File outputDir = new File(tempDir.toFile(), "empty_media");
            assertTrue(outputDir.exists(), "Output dir should still be created");
            File[] files = outputDir.listFiles((dir, name) -> name.matches(".*\\.(png|jpg|jpeg|bmp|gif)"));
            assertNotNull(files);
            assertEquals(0, files.length, "No images should be extracted from a media-less PPTX");
        }
    }
}
