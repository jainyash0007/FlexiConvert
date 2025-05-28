package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WebpToPngConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Could not read image: " + inputFile.getName());
        }

        File outputFile = new File(inputFile.getParent(),
                inputFile.getName().replaceAll("(?i)\\.webp$", ".png"));

        ImageIO.write(image, "png", outputFile);
    }
}
