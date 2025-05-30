package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import com.luciad.imageio.webp.WebPWriteParam;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;


@Component
@ConverterFor(ConversionType.PNG_TO_WEBP)
public class PngToWebpConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Could not read image: " + inputFile.getName());
        }

        File outputFile = new File(inputFile.getParent(),
                inputFile.getName().replaceAll("(?i)\\.png$", ".webp"));

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IOException("No WebP ImageWriter found. Make sure webp-imageio is on the classpath.");
        }

        ImageWriter writer = writers.next();
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        writeParam.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType("Lossy");
        writeParam.setCompressionQuality(0.8f); // Adjust quality (0.0 to 1.0)

        writer.setOutput(new FileImageOutputStream(outputFile));
        writer.write(null, new javax.imageio.IIOImage(image, null, null), writeParam);
        writer.dispose();
    }
}
