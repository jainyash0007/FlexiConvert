package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


@Component
@ConverterFor(ConversionType.PNG_TO_JPG)
public class PngToJpgConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Failed to read image: " + inputFile.getName());
        }

        File output = new File(inputFile.getParent(),
                inputFile.getName().replaceAll("(?i)\\.png$", ".jpg"));

        ImageIO.write(image, "jpg", output);
    }
}
