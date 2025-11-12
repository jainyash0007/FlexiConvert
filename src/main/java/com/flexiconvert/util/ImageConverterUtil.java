package com.flexiconvert.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for simple image format conversions.
 * Provides common functionality for image converters to reduce code duplication.
 */
public class ImageConverterUtil {

    /**
     * Converts an image file from one format to another using ImageIO.
     * 
     * @param inputFile The input image file
     * @param fromExtension The source file extension (e.g., "png", "jpg")
     * @param toExtension The target file extension (e.g., "jpg", "png")
     * @throws IOException If the image cannot be read or written
     */
    public static void convertImage(File inputFile, String fromExtension, String toExtension) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Failed to read image: " + inputFile.getName());
        }

        File outputFile = FileNameUtil.changeExtension(inputFile, fromExtension, toExtension);
        ImageIO.write(image, toExtension, outputFile);
    }
}
