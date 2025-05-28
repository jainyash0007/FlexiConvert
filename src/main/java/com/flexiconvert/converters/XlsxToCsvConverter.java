package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class XlsxToCsvConverter implements FormatConverter {

    @Override
    public void convert(File xlsxFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(xlsxFile);
             Workbook workbook = new XSSFWorkbook(fis);
             PrintWriter writer = new PrintWriter(
                     new FileWriter(xlsxFile.getParent() + "/" + xlsxFile.getName().replace(".xlsx", ".csv")))) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                StringBuilder line = new StringBuilder();
                for (Cell cell : row) {
                    line.append(cell.toString()).append(",");
                }
                writer.println(line);
            }
        }
    }
}
