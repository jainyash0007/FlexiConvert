package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@ConverterFor(ConversionType.XLSX_TO_CSV)
public class XlsxToCsvConverter implements FormatConverter {

    @Override
    public void convert(File xlsxFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(xlsxFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int sheetCount = workbook.getNumberOfSheets();

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String safeSheetName = sheet.getSheetName().replaceAll("[\\\\/:*?\"<>|]", "_");

                File outputFile;
                if (sheetCount == 1) {
                    // Match test case expectations
                    outputFile = new File(xlsxFile.getParent(),
                            xlsxFile.getName().replaceAll("(?i)\\.xlsx$", ".csv"));
                } else {
                    // Create per-sheet file
                    outputFile = new File(xlsxFile.getParent(), safeSheetName + ".csv");
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                    for (Row row : sheet) {
                        StringBuilder line = new StringBuilder();
                        int lastCell = row.getLastCellNum();
                        for (int c = 0; c < lastCell; c++) {
                            Cell cell = row.getCell(c);
                            String value = getCellValueAsString(cell, evaluator);
                            line.append(escapeCsv(value));
                            if (c < lastCell - 1) {
                                line.append(",");
                            }
                        }
                        writer.println(line);
                    }
                }
            }
        }
    }

    private String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        CellType type = evaluator.evaluateInCell(cell).getCellType();
        switch (type) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:  // already handled by evaluateInCell
                return cell.toString();
            case BLANK:
            default:
                return "";
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
