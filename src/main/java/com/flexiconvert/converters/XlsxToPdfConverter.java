package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.ss.usermodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.nio.file.Files;

public class XlsxToPdfConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputFile)) {
            Sheet sheet = workbook.getSheetAt(0);  // first sheet
            File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));

            try (PDDocument pdf = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                pdf.addPage(page);

                PDPageContentStream content = new PDPageContentStream(pdf, page);
                PDType1Font font = PDType1Font.HELVETICA;
                float fontSize = 10;
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float y = yStart;
                float rowHeight = 20;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;

                for (Row row : sheet) {
                    float x = margin;
                    for (Cell cell : row) {
                        String text = getCellText(cell);
                        content.beginText();
                        content.setFont(font, fontSize);
                        content.newLineAtOffset(x, y);
                        content.showText(text);
                        content.endText();
                        x += tableWidth / row.getLastCellNum();
                    }
                    y -= rowHeight;
                    if (y < margin) break;  // simple pagination logic
                }

                content.close();
                pdf.save(outputFile);
            }
        }
    }

    private String getCellText(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> Double.toString(cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "[Unsupported]";
        };
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
