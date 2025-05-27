package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;

public class RtfToPdfConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // Step 1: Extract plain text using RTFEditorKit
        RTFEditorKit rtfParser = new RTFEditorKit();
        Document doc = new DefaultStyledDocument();

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            rtfParser.read(fis, doc, 0);
        } catch (BadLocationException e) {
            throw new IOException("Failed to parse RTF content", e);
        }

        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            throw new IOException("Failed to extract text from RTF", e);
        }

        // Step 2: Create PDF using PDFBox
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);

        PDPageContentStream content = new PDPageContentStream(pdf, page);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.setLeading(14.5f);
        content.newLineAtOffset(50, 750);

        int lineCount = 0;
        for (String line : text.split("\n")) {
            content.showText(line);
            content.newLine();
            lineCount++;

            // Handle page overflow (basic implementation)
            if (lineCount >= 50) {
                content.endText();
                content.close();
                page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                content = new PDPageContentStream(pdf, page);
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.setLeading(14.5f);
                content.newLineAtOffset(50, 750);
                lineCount = 0;
            }
        }

        content.endText();
        content.close();

        File outputFile = new File(inputFile.getParent(),
                inputFile.getName().replaceAll("(?i)\\.rtf$", ".pdf"));
        pdf.save(outputFile);
        pdf.close();
    }
}
