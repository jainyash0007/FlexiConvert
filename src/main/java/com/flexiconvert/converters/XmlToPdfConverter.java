package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;

public class XmlToPdfConverter implements FormatConverter {

    @Override
    public void convert(File xmlFile) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream content = new PDPageContentStream(doc, page);
        content.beginText();
        content.setFont(PDType1Font.COURIER, 10);
        content.setLeading(12f);
        content.newLineAtOffset(50, 750);

        try (BufferedReader reader = new BufferedReader(new FileReader(xmlFile))) {
            String line;
            int linesPerPage = 60;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                content.showText(line);
                content.newLine();
                lineCount++;

                if (lineCount >= linesPerPage) {
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

        File output = new File(xmlFile.getParent(), xmlFile.getName().replaceAll("(?i)\\.xml$", ".pdf"));
        doc.save(output);
        doc.close();
    }
}
