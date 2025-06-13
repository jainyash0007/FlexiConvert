package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;


@Component
@ConverterFor(ConversionType.PPTX_TO_PDF)
public class PptxToPdfConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(inputFile));
             PDDocument pdf = new PDDocument()) {

            Dimension pgsize = ppt.getPageSize();
            for (XSLFSlide slide : ppt.getSlides()) {
                BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.WHITE);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
                slide.draw(graphics);
                graphics.dispose();

                PDPage page = new PDPage(new PDRectangle(pgsize.width, pgsize.height));
                pdf.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(pdf, img);
                try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                    content.drawImage(pdImage, 0, 0, pgsize.width, pgsize.height);
                }
            }

            File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));
            pdf.save(outputFile);
        }
    }

    private String getOutputFileName(File inputFile) {
        String name = inputFile.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            name = name.substring(0, dotIndex);
        }
        return name + ".pdf";
    }
}
