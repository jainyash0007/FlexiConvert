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

public class PngToJpgConverterTest extends AbstractConverterTest {

    @Test
    public void testPngToJpgConversion() throws Exception {
        // Create a dummy PNG image
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 50);
        g.dispose();

        File input = new File(tempDir.toFile(), "test.png");
        ImageIO.write(img, "png", input);

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PngToJpgConverter converter = ctx.getBean(PngToJpgConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "jpg");
            assertTrue(output.exists(), "JPG file should be created");
            BufferedImage result = ImageIO.read(output);
            assertNotNull(result, "Resulting JPG image should be readable");
        }
    }

    @Test
    public void testInvalidPngThrowsException() throws Exception {
        File input = createTempFile("notanimage.png", "not-a-real-image");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PngToJpgConverter converter = ctx.getBean(PngToJpgConverter.class);
            assertThrows(IOException.class, () -> converter.convert(input), 
                    "Should throw IOException for invalid PNG file");
        }
    }
}
