package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;

@Component
@ConverterFor(ConversionType.XLSX_TO_PDF)
public class XlsxToPdfConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputFile)) {
            File outputFile = new File(inputFile.getParent(), getOutputFileName(inputFile));

            try (PDDocument pdf = new PDDocument()) {
                PDType1Font font = PDType1Font.HELVETICA;
                float fontSize = 10;
                float margin = 50;
                float rowHeight = 20;
                float tableWidth = PDRectangle.LETTER.getWidth() - 2 * margin;

                for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                    Sheet sheet = workbook.getSheetAt(s);
                    PDPage page = new PDPage(PDRectangle.LETTER);
                    pdf.addPage(page);

                    PDPageContentStream content = new PDPageContentStream(pdf, page);
                    content.setFont(font, fontSize);
                    content.setLeading(14.5f);

                    float y = PDRectangle.LETTER.getHeight() - margin;
                    int lineCount = 0;

                    for (Row row : sheet) {
                        content.beginText();
                        content.setFont(font, fontSize);
                        content.newLineAtOffset(margin, y - lineCount * rowHeight);

                        StringBuilder rowText = new StringBuilder();
                        for (Cell cell : row) {
                            rowText.append(getCellText(cell).replace("\t", "    ")).append("    ");
                        }

                        content.showText(rowText.toString().trim());
                        content.endText();
                        lineCount++;

                        if ((y - lineCount * rowHeight) < margin) {
                            content.close();
                            page = new PDPage(PDRectangle.LETTER);
                            pdf.addPage(page);
                            content = new PDPageContentStream(pdf, page);
                            y = PDRectangle.LETTER.getHeight() - margin;
                        }
                    }
                    content.close();
                }

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
