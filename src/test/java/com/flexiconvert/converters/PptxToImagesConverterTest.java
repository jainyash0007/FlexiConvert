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

import static org.junit.jupiter.api.Assertions.*;

public class PptxToImagesConverterTest extends AbstractConverterTest {

    @Test
    public void testPptxToImagesConversion() throws Exception {
        File input = new File(tempDir.toFile(), "presentation.pptx");

        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(input)) {

            XSLFSlide slide1 = ppt.createSlide();
            XSLFTextBox box1 = slide1.createTextBox();
            box1.setAnchor(new Rectangle(50, 50, 400, 100));
            box1.setText("Slide 1 Content");

            XSLFSlide slide2 = ppt.createSlide();
            XSLFTextBox box2 = slide2.createTextBox();
            box2.setAnchor(new Rectangle(50, 50, 400, 100));
            box2.setText("Slide 2 Content");

            ppt.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PptxToImagesConverter converter = ctx.getBean(PptxToImagesConverter.class);
            converter.convert(input);

            File slide1 = new File(tempDir.toFile(), "presentation_slide1.png");
            File slide2 = new File(tempDir.toFile(), "presentation_slide2.png");

            assertTrue(slide1.exists(), "Slide 1 image should be created");
            assertTrue(slide2.exists(), "Slide 2 image should be created");
        }
    }

    @Test
    public void testEmptyPptxCreatesNoImages() throws Exception {
        File input = new File(tempDir.toFile(), "empty.pptx");
        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(input)) {
            ppt.write(out);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PptxToImagesConverter converter = ctx.getBean(PptxToImagesConverter.class);
            converter.convert(input);

            File slide1 = new File(tempDir.toFile(), "empty_slide1.png");
            assertFalse(slide1.exists(), "No slide images should be created for empty PPTX");
        }
    }
}
