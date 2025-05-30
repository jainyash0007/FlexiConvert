package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;
import org.springframework.stereotype.Component;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
@ConverterFor(ConversionType.RTF_TO_PDF)
public class RtfToPdfConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // Check for empty file
        if (inputFile.length() == 0) {
            throw new IOException("Cannot convert empty RTF file");
        }
        
        // Extract text from RTF
        String text = extractTextFromRtf(inputFile);
        
        // Create output file path
        String outputPath = inputFile.getAbsolutePath().replaceFirst("(?i)\\.rtf$", ".pdf");
        File outputFile = new File(outputPath);
        
        // Create PDF
        createPdf(text, outputFile);
    }

    private String extractTextFromRtf(File rtfFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(rtfFile)) {
            RTFEditorKit rtfParser = new RTFEditorKit();
            DefaultStyledDocument styledDoc = new DefaultStyledDocument();
            rtfParser.read(fis, styledDoc, 0);
            return styledDoc.getText(0, styledDoc.getLength());
        } catch (BadLocationException e) {
            throw new IOException("Error extracting text from RTF file", e);
        }
    }
    
    private void createPdf(String text, File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // Use standard Helvetica font
            PDFont font = PDType1Font.HELVETICA;
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 700);
                float leading = 1.5f * 12;
                
                // Replace any problematic characters before rendering
                String safeText = sanitizeTextForPdf(text);
                
                // Split by lines and render each line
                String[] lines = safeText.split("\n");
                for (String line : lines) {
                    // Further sanitize each line and handle empty lines
                    if (line.trim().isEmpty()) {
                        contentStream.newLineAtOffset(0, -leading); // Empty line
                        continue;
                    }
                    
                    // Break long lines
                    float maxWidth = page.getMediaBox().getWidth() - 100;
                    if (font.getStringWidth(line) / 1000 * 12 > maxWidth) {
                        int lastSpace = 0;
                        int startIndex = 0;
                        for (int i = 0; i < line.length(); i++) {
                            if (Character.isWhitespace(line.charAt(i))) {
                                if (font.getStringWidth(line.substring(startIndex, i)) / 1000 * 12 > maxWidth) {
                                    contentStream.showText(line.substring(startIndex, lastSpace));
                                    contentStream.newLineAtOffset(0, -leading);
                                    startIndex = lastSpace + 1;
                                }
                                lastSpace = i;
                            }
                        }
                        if (startIndex < line.length()) {
                            contentStream.showText(line.substring(startIndex));
                        }
                    } else {
                        contentStream.showText(line);
                    }
                    contentStream.newLineAtOffset(0, -leading);
                }
                contentStream.endText();
            }
            
            document.save(outputFile);
        }
    }
    
    /**
     * Sanitizes text to ensure only characters supported by the PDF font are used
     */
    private String sanitizeTextForPdf(String text) {
        if (text == null) return "";
        
        // Replace bullet character (U+F0B7) with hyphen
        text = text.replace('\uf0b7', '-');
        
        // Replace tab character (U+0009) with spaces
        text = text.replace('\t', ' ');
        
        // Remove or replace other problematic characters
        // This handles a wider range of special characters by removing anything outside the common range
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Handle control characters
            if (c < 32) {
                // Replace control characters with space
                result.append(' ');
            }
            // Keep ASCII (32-127) and common extended Latin characters
            else if (c < 128 || (c >= 0x00A0 && c <= 0x00FF)) {
                result.append(c);
            } else {
                // Replace other characters with a safe alternative
                result.append('?');
            }
        }
        
        return result.toString();
    }
}
