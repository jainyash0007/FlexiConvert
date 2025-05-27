package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;

public class GenericToPdfConverter implements FormatConverter {

    private final String sourceExtension;

    public GenericToPdfConverter(String sourceExtension) {
        this.sourceExtension = sourceExtension;
    }

    @Override
    public void convert(File inputFile) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream content = new PDPageContentStream(doc, page);
        content.beginText();
        content.setFont(PDType1Font.COURIER, 10);
        content.setLeading(12f);
        content.newLineAtOffset(50, 750);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                content.showText(line);
                content.newLine();
                lineCount++;

                if (lineCount >= 60) {
                    content.endText();
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    content.beginText();
                    content.setFont(PDType1Font.COURIER, 10);
                    content.setLeading(12f);
                    content.newLineAtOffset(50, 750);
                    lineCount = 0;
                }
            }
        }

        content.endText();
        content.close();

        File output = new File(inputFile.getParent(), inputFile.getName().replaceAll("(?i)\\" + sourceExtension + "$", ".pdf"));
        doc.save(output);
        doc.close();
    }
}
