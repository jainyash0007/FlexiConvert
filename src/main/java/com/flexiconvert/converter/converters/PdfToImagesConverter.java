package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfToImagesConverter implements FormatConverter {

    @Override
    public void convert(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            String baseName = pdfFile.getName().replaceAll("(?i)\\.pdf$", "");

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300); // high-res image
                File output = new File(pdfFile.getParent(), baseName + "_page" + (i + 1) + ".png");
                ImageIO.write(image, "png", output);
            }
        }
    }
}
