package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class PptxToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testPptxToPdfConversion() throws Exception {
        File input = new File(tempDir.toFile(), "slides.pptx");

        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(input)) {

            XSLFSlide slide1 = ppt.createSlide();
            XSLFTextBox box1 = slide1.createTextBox();
            box1.setAnchor(new Rectangle(50, 50, 400, 100));
            box1.setText("Slide 1 content");

            XSLFSlide slide2 = ppt.createSlide();
            XSLFTextBox box2 = slide2.createTextBox();
            box2.setAnchor(new Rectangle(50, 50, 400, 100));
            box2.setText("Slide 2 content");

            ppt.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PptxToPdfConverter converter = ctx.getBean(PptxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from PPTX");
            assertTrue(Files.size(output.toPath()) > 0, "PDF file should not be empty");
        }
    }

    @Test
    public void testEmptyPptxStillCreatesPdf() throws Exception {
        File input = new File(tempDir.toFile(), "empty.pptx");

        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(input)) {
            ppt.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PptxToPdfConverter converter = ctx.getBean(PptxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF should still be created from empty PPTX");
            assertTrue(Files.size(output.toPath()) > 0, "PDF should not be zero bytes");
        }
    }
}
