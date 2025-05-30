package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;


@Component
@ConverterFor(ConversionType.MD_TO_PDF)
public class MdToPdfConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // Step 1: Read Markdown content
        StringBuilder md = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                md.append(line).append("\n");
            }
        }

        // Step 2: Parse and render as plain text
        Parser parser = Parser.builder().build();
        Node document = parser.parse(md.toString());

        TextContentRenderer renderer = TextContentRenderer.builder().build();
        String text = renderer.render(document);

        // Step 3: Write to PDF using PDFBox
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

        File outputFile = new File(inputFile.getParent(), inputFile.getName().replaceAll("(?i)\\.md$", ".pdf"));
        pdf.save(outputFile);
        pdf.close();
    }
}
