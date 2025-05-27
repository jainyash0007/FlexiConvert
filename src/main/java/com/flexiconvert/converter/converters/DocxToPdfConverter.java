package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocxToPdfConverter implements FormatConverter {

    @Override
    public void convert(File docxFile) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(new FileInputStream(docxFile));
             PDDocument pdf = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);

            PDPageContentStream content = new PDPageContentStream(pdf, page);
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.setLeading(14.5f);
            content.newLineAtOffset(50, 750);

            for (XWPFParagraph para : docx.getParagraphs()) {
                content.showText(para.getText());
                content.newLine();
            }

            content.endText();
            content.close();

            File output = new File(docxFile.getParent(), docxFile.getName().replace(".docx", ".pdf"));
            pdf.save(output);
        }
    }
}
