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

import static org.junit.jupiter.api.Assertions.*;

public class PngToWebpConverterTest extends AbstractConverterTest {

    @Test
    public void testPngToWebpConversion() throws Exception {
        // Create a dummy PNG image
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.ORANGE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        File input = new File(tempDir.toFile(), "test.png");
        ImageIO.write(img, "png", input);

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PngToWebpConverter converter = ctx.getBean(PngToWebpConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "webp");
            assertTrue(output.exists(), "WebP file should be created");

            System.gc();
            Thread.sleep(100);

            // Note: WebP read support is not always guaranteed; presence is sufficient here
            assertTrue(output.length() > 0, "WebP file should not be empty");
        }
    }

    @Test
    public void testInvalidPngThrowsException() throws Exception {
        File badFile = createTempFile("corrupt.png", "not-a-real-image");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PngToWebpConverter converter = ctx.getBean(PngToWebpConverter.class);
            assertThrows(IOException.class, () -> converter.convert(badFile), "Should throw for unreadable image");
        }
    }
}
