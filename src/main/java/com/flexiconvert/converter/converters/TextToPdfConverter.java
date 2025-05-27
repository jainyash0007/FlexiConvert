package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;

public class TextToPdfConverter implements FormatConverter {

    @Override
    public void convert(File txtFile) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream content = new PDPageContentStream(doc, page);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.setLeading(14.5f);
        content.newLineAtOffset(50, 750);

        try (FileInputStream fis = new FileInputStream(txtFile)) {
            String text = new String(fis.readAllBytes());
            for (String line : text.split("\n")) {
                content.showText(line);
                content.newLine();
            }
        }

        content.endText();
        content.close();

        File output = new File(txtFile.getParent(), txtFile.getName().replace(".txt", ".pdf"));
        doc.save(output);
        doc.close();
    }
}
