package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class JpgToPngConverterTest extends AbstractConverterTest {

    @Test
    public void testJpgToPngConversion() throws Exception {
        // Create a simple dummy JPG image
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 50);
        g.dispose();

        File jpgFile = new File(tempDir.toFile(), "sample.jpg");
        ImageIO.write(img, "jpg", jpgFile);

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JpgToPngConverter converter = ctx.getBean(JpgToPngConverter.class);
            converter.convert(jpgFile);

            File output = getOutputFile(jpgFile, "png");
            assertTrue(output.exists(), "PNG file should be created from JPG");
            BufferedImage outImg = ImageIO.read(output);
            assertNotNull(outImg, "Converted PNG should be readable");
        }
    }

    @Test
    public void testInvalidJpgThrowsException() throws Exception {
        File badFile = createTempFile("invalid.jpg", "not a real image");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            JpgToPngConverter converter = ctx.getBean(JpgToPngConverter.class);
            assertThrows(IOException.class, () -> converter.convert(badFile), "Should fail on invalid JPG file");
        }
    }
}
