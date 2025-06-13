package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.poi.xwpf.usermodel.*;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@ConverterFor(ConversionType.DOCX_TO_PDF)
public class DocxToPdfConverter implements FormatConverter {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 12;
    private static final float LEADING = 14.5f;
    private static final PDFont FONT_REGULAR = PDType1Font.HELVETICA;
    private static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final PDFont FONT_ITALIC = PDType1Font.HELVETICA_OBLIQUE;
    private static final PDFont FONT_BOLD_ITALIC = PDType1Font.HELVETICA_BOLD_OBLIQUE;
    private static final float MAX_IMG_WIDTH = 400f; // Maximum width for images in points
    private static final float IMG_PADDING = 10f;    // Padding around images

    @Override
    public void convert(File docxFile) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(new FileInputStream(docxFile));
             PDDocument pdf = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);

            PDPageContentStream content = new PDPageContentStream(pdf, page);
            float width = PDRectangle.A4.getWidth() - 2 * MARGIN;
            float yPosition = PDRectangle.A4.getHeight() - MARGIN;

            // First pass - analyze paragraphs to identify headings
            List<Boolean> isHeading = new ArrayList<>();
            for (XWPFParagraph para : docx.getParagraphs()) {
                // Simple heuristic: consider as heading if all text is bold and short
                boolean allBold = true;
                int totalLength = 0;
                for (XWPFRun run : para.getRuns()) {
                    if (run.text() != null) {
                        totalLength += run.text().length();
                        if (!run.isBold()) {
                            allBold = false;
                            break;
                        }
                    }
                }
                isHeading.add(allBold && totalLength < 30);
            }

            // Second pass - render with proper formatting
            int paraIndex = 0;
            for (XWPFParagraph para : docx.getParagraphs()) {
                boolean isBullet = para.getNumID() != null;
                boolean isParaHeading = paraIndex < isHeading.size() && isHeading.get(paraIndex);
                paraIndex++;
                
                // Extra vertical space before headings
                if (isParaHeading) {
                    yPosition -= LEADING * 0.5;
                    if (yPosition - LEADING < MARGIN) {
                        content.close();
                        page = new PDPage(PDRectangle.A4);
                        pdf.addPage(page);
                        content = new PDPageContentStream(pdf, page);
                        yPosition = PDRectangle.A4.getHeight() - MARGIN;
                    }
                }

                // Process paragraph text
                StringBuilder paraText = new StringBuilder();
                List<ContentItem> contentItems = new ArrayList<>();
                
                for (XWPFRun run : para.getRuns()) {
                    // First, check for embedded pictures
                    List<XWPFPicture> pictures = run.getEmbeddedPictures();
                    if (pictures != null && !pictures.isEmpty()) {
                        for (XWPFPicture picture : pictures) {
                            XWPFPictureData pictureData = picture.getPictureData();
                            if (pictureData != null) {
                                // Add image content item
                                contentItems.add(new ImageItem(pictureData.getData()));
                            }
                        }
                    }
                    
                    // Then process text content
                    String text = run.text();
                    if (text == null || text.isEmpty()) continue;
                    
                    // Clean text - fix common DOCX extraction issues
                    text = text.replace("\r", "");
                    text = text.replace("\t", "    "); // Replace tabs with spaces
                    
                    // Remove any other control characters that might cause issues
                    text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
                    
                    PDFont font = FONT_REGULAR;
                    if (run.isBold() && run.isItalic()) {
                        font = FONT_BOLD_ITALIC;
                    } else if (run.isBold()) {
                        font = FONT_BOLD;
                    } else if (run.isItalic()) {
                        font = FONT_ITALIC;
                    }
                    
                    // Special handling for headings - always use bold and slightly larger font
                    if (isParaHeading) {
                        font = FONT_BOLD;
                    }
                    
                    paraText.append(text);
                    contentItems.add(new TextItem(text, font));
                }
                
                // Skip empty paragraphs
                if (paraText.length() == 0 && contentItems.stream().noneMatch(item -> item instanceof ImageItem)) {
                    continue;
                }
                
                // Determine font size based on content type
                float fontSize = FONT_SIZE;
                if (isParaHeading) {
                    fontSize = FONT_SIZE * 1.2f;  // Slightly larger for headings
                }
                
                // Create text content
                content.beginText();
                content.setLeading(LEADING);
                content.newLineAtOffset(MARGIN, yPosition);
                float remainingWidth = width;
                float startX = MARGIN;
                float currentX = startX;
                
                // Start a new page if needed
                if (yPosition - LEADING < MARGIN) {
                    content.endText();
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    pdf.addPage(page);
                    content = new PDPageContentStream(pdf, page);
                    content.beginText();
                    content.setLeading(LEADING);
                    yPosition = PDRectangle.A4.getHeight() - MARGIN;
                    content.newLineAtOffset(MARGIN, yPosition);
                    currentX = MARGIN;
                    remainingWidth = width;
                }
                
                // Add bullet if needed
                if (isBullet) {
                    content.setFont(FONT_REGULAR, fontSize);
                    String bullet = "• ";
                    content.showText(bullet);
                    float bulletWidth = FONT_REGULAR.getStringWidth(bullet) / 1000 * fontSize;
                    currentX += bulletWidth;
                    remainingWidth -= bulletWidth;
                }
                
                // Process each content item (text or image)
                for (ContentItem item : contentItems) {
                    if (item instanceof TextItem) {
                        TextItem textItem = (TextItem) item;
                        content.setFont(textItem.font, fontSize);
                        
                        // Process character by character to maintain proper spacing
                        String text = textItem.text;
                        int startIdx = 0;
                        int endIdx;
                        int textLength = text.length();
                        
                        while (startIdx < textLength) {
                            // Look for natural breaking points (spaces, hyphens)
                            int nextBreakPoint = text.indexOf(' ', startIdx);
                            if (nextBreakPoint == -1) nextBreakPoint = text.indexOf('-', startIdx);
                            if (nextBreakPoint == -1) nextBreakPoint = textLength;
                            
                            // If we find a breaking point, check if it fits
                            String segment = text.substring(startIdx, nextBreakPoint + (nextBreakPoint < textLength ? 1 : 0));
                            float segmentWidth = textItem.font.getStringWidth(segment) / 1000 * fontSize;
                            
                            if (segmentWidth <= remainingWidth) {
                                // Show this segment
                                content.showText(segment);
                                currentX += segmentWidth;
                                remainingWidth -= segmentWidth;
                                startIdx = nextBreakPoint + 1;
                            } else {
                                // Need to fit character by character
                                for (endIdx = startIdx + 1; endIdx <= textLength; endIdx++) {
                                    segment = text.substring(startIdx, endIdx);
                                    segmentWidth = textItem.font.getStringWidth(segment) / 1000 * fontSize;
                                    
                                    if (segmentWidth > remainingWidth) {
                                        if (endIdx == startIdx + 1) {
                                            // Even one character doesn't fit, need new line
                                            content.newLine();
                                            yPosition -= LEADING;
                                            
                                            if (yPosition - LEADING < MARGIN) {
                                                content.endText();
                                                content.close();
                                                page = new PDPage(PDRectangle.A4);
                                                pdf.addPage(page);
                                                content = new PDPageContentStream(pdf, page);
                                                content.beginText();
                                                content.setLeading(LEADING);
                                                yPosition = PDRectangle.A4.getHeight() - MARGIN;
                                                content.newLineAtOffset(MARGIN, yPosition);
                                                content.setFont(textItem.font, fontSize);
                                            }
                                            
                                            currentX = startX;
                                            remainingWidth = width;
                                        } else {
                                            // We found where to break
                                            endIdx--;
                                            break;
                                        }
                                    } else if (endIdx == textLength) {
                                        break;
                                    }
                                }
                                
                                // Show what fits
                                segment = text.substring(startIdx, endIdx);
                                content.showText(segment);
                                segmentWidth = textItem.font.getStringWidth(segment) / 1000 * fontSize;
                                currentX += segmentWidth;
                                remainingWidth -= segmentWidth;
                                
                                // If more text to show, add new line
                                if (endIdx < textLength) {
                                    content.newLine();
                                    yPosition -= LEADING;
                                    
                                    if (yPosition - LEADING < MARGIN) {
                                        content.endText();
                                        content.close();
                                        page = new PDPage(PDRectangle.A4);
                                        pdf.addPage(page);
                                        content = new PDPageContentStream(pdf, page);
                                        content.beginText();
                                        content.setLeading(LEADING);
                                        yPosition = PDRectangle.A4.getHeight() - MARGIN;
                                        content.newLineAtOffset(MARGIN, yPosition);
                                        content.setFont(textItem.font, fontSize);
                                    }
                                    
                                    currentX = startX;
                                    remainingWidth = width;
                                }
                                
                                startIdx = endIdx;
                            }
                        }
                    } else if (item instanceof ImageItem) {
                        // We need to end the text block to draw an image
                        content.endText();
                        
                        ImageItem imageItem = (ImageItem) item;
                        byte[] imageData = imageItem.imageData;
                        
                        try {
                            // Create PDF image from byte array
                            PDImageXObject pdImage = PDImageXObject.createFromByteArray(pdf, imageData, "img");
                            
                            // Calculate image dimensions with proper scaling
                            Dimension dimensions = calculateImageDimensions(pdImage, width);
                            float imgWidth = (float) dimensions.getWidth();
                            float imgHeight = (float) dimensions.getHeight();
                            
                            // Check if we need a new page for the image
                            if (yPosition - imgHeight < MARGIN) {
                                content.close();
                                page = new PDPage(PDRectangle.A4);
                                pdf.addPage(page);
                                content = new PDPageContentStream(pdf, page);
                                yPosition = PDRectangle.A4.getHeight() - MARGIN;
                            }
                            
                            // Center the image horizontally
                            float xOffset = MARGIN + (width - imgWidth) / 2;
                            
                            // Draw the image
                            content.drawImage(pdImage, xOffset, yPosition - imgHeight, imgWidth, imgHeight);
                            
                            // Update y position
                            yPosition = yPosition - imgHeight - IMG_PADDING;
                            
                            // Start a new text block for any remaining content
                            content.beginText();
                            content.setLeading(LEADING);
                            content.setFont(FONT_REGULAR, fontSize); // Reset font for any text after image
                            content.newLineAtOffset(MARGIN, yPosition);
                            currentX = MARGIN;
                            remainingWidth = width;
                        } catch (IOException e) {
                            // If there's an error with this image, log it and continue
                            System.err.println("Error processing image: " + e.getMessage());
                            
                            // Restart text block
                            content.beginText();
                            content.setLeading(LEADING);
                            content.setFont(FONT_REGULAR, fontSize);
                            content.newLineAtOffset(currentX, yPosition);
                        }
                    }
                }
                
                // End paragraph and add some spacing
                content.newLine();
                yPosition -= LEADING;
                
                // Add extra spacing after headings
                if (isParaHeading) {
                    yPosition -= LEADING * 0.5;
                }
                
                content.endText();
            }

            content.close();
            File output = new File(docxFile.getParent(), docxFile.getName().replace(".docx", ".pdf"));
            pdf.save(output);
        }
    }
    
    /**
     * Calculate appropriate image dimensions keeping aspect ratio and fitting within max width
     */
    private Dimension calculateImageDimensions(PDImageXObject image, float maxWidth) {
        float originalWidth = image.getWidth();
        float originalHeight = image.getHeight();
        
        // If image is wider than our maximum, scale it down
        if (originalWidth > maxWidth) {
            float scaleFactor = maxWidth / originalWidth;
            return new Dimension((int) maxWidth, (int) (originalHeight * scaleFactor));
        }
        
        // If image is already smaller than max width, use original dimensions
        // but cap it at MAX_IMG_WIDTH to prevent very large images
        if (originalWidth > MAX_IMG_WIDTH) {
            float scaleFactor = MAX_IMG_WIDTH / originalWidth;
            return new Dimension((int) MAX_IMG_WIDTH, (int) (originalHeight * scaleFactor));
        }
        
        return new Dimension((int) originalWidth, (int) originalHeight);
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float size = font.getStringWidth(testLine) / 1000 * fontSize;
            if (size > maxWidth) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    lines.add(word);
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }

    // Base class for content items (text or images)
    private static abstract class ContentItem {
    }
    
    // Class to store text content with formatting
    private static class TextItem extends ContentItem {
        String text;
        PDFont font;

        TextItem(String text, PDFont font) {
            this.text = text;
            this.font = font;
        }
    }
    
    // Class to store image content
    private static class ImageItem extends ContentItem {
        byte[] imageData;
        
        ImageItem(byte[] imageData) {
            this.imageData = imageData;
        }
    }
}
