package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class PdfToTextConverterTest extends AbstractConverterTest {

    @Test
    public void testPdfToTextConversion() throws Exception {
        File input = new File(tempDir.toFile(), "sample.pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(50, 750);
                content.showText("Hello from PDFBox!");
                content.endText();
            }
            document.save(input);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PdfToTextConverter converter = ctx.getBean(PdfToTextConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "txt");
            assertTrue(output.exists(), "Text file should be created");
            String content = Files.readString(output.toPath());
            assertTrue(content.contains("Hello from PDFBox!"), "Extracted text should match PDF content");
        }
    }

    @Test
    public void testEmptyPdfToTextStillCreatesFile() throws Exception {
        File input = new File(tempDir.toFile(), "empty.pdf");

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(input);
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            PdfToTextConverter converter = ctx.getBean(PdfToTextConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "txt");
            assertTrue(output.exists(), "Text file should be created for empty PDF");
            String content = Files.readString(output.toPath());
            assertTrue(content.isBlank(), "Text file should be empty for blank PDF");
        }
    }
}
