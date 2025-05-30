package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class XlsxToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testBasicXlsxToPdfConversion() throws Exception {
        // Create a simple Excel file
        File input = createSimpleExcelFile("basic.xlsx", 
            new String[][] {
                {"Name", "Age", "Email"},
                {"John Smith", "30", "john@example.com"},
                {"Jane Doe", "25", "jane@example.com"},
                {"Bob Johnson", "45", "bob@example.com"}
            }
        );
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToPdfConverter converter = ctx.getBean(XlsxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from XLSX");
            assertTrue(output.length() > 0, "PDF file should not be empty");

            // Verify PDF contains key content from the Excel file
            String pdfText = extractTextFromPdf(output);
            assertTrue(pdfText.contains("John Smith"), "PDF should contain 'John Smith' from Excel");
            assertTrue(pdfText.contains("jane@example.com"), "PDF should contain 'jane@example.com' from Excel");
            assertTrue(pdfText.contains("45"), "PDF should contain age '45' from Excel");
        }
    }
    
    @Test
    public void testMultipleWorksheetsExcelToPdf() throws Exception {
        // Create Excel with multiple worksheets
        File input = createTempFile("multiple_sheets.xlsx", ".xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // First worksheet - Employee Data
            Sheet sheet1 = workbook.createSheet("Employees");
            String[][] employeeData = {
                {"ID", "Name", "Department", "Salary"},
                {"001", "Alice Smith", "Engineering", "65000"},
                {"002", "Bob Jones", "Marketing", "55000"},
                {"003", "Carol White", "Finance", "60000"}
            };
            createSheetData(sheet1, employeeData);
            
            // Second worksheet - Department Budget
            Sheet sheet2 = workbook.createSheet("Budget");
            String[][] budgetData = {
                {"Department", "Q1", "Q2", "Q3", "Q4", "Total"},
                {"Engineering", "100000", "120000", "115000", "125000", "460000"},
                {"Marketing", "50000", "60000", "65000", "75000", "250000"},
                {"Finance", "30000", "30000", "35000", "40000", "135000"}
            };
            createSheetData(sheet2, budgetData);
            
            // Save workbook
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToPdfConverter converter = ctx.getBean(XlsxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created");
            
            // Verify that content from both sheets is present in the PDF
            String pdfText = extractTextFromPdf(output);
            assertTrue(pdfText.contains("Alice Smith") && pdfText.contains("Engineering"), 
                       "PDF should contain data from first sheet");
            assertTrue(pdfText.contains("Department") && pdfText.contains("460000"), 
                       "PDF should contain data from second sheet");
        }
    }
    
    @Test
    public void testFormattedExcelToPdf() throws Exception {
        // Create Excel with formatting
        File input = createTempFile("formatted.xlsx", ".xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Formatted Data");
            
            // Create cell styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
            
            // Create rows and cells
            String[][] data = {
                {"Product", "Quantity", "Price", "Total"},
                {"Widget A", "5", "10.50", "52.50"},
                {"Widget B", "3", "15.75", "47.25"},
                {"Widget C", "10", "5.99", "59.90"}
            };
            
            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < data[r].length; c++) {
                    Cell cell = row.createCell(c);
                    
                    // Apply formatting to header row
                    if (r == 0) {
                        cell.setCellValue(data[r][c]);
                        cell.setCellStyle(headerStyle);
                    } 
                    // Apply number/currency formatting to numeric columns
                    else if (c >= 2) {
                        cell.setCellValue(Double.parseDouble(data[r][c]));
                        cell.setCellStyle(currencyStyle);
                    } 
                    else {
                        cell.setCellValue(data[r][c]);
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToPdfConverter converter = ctx.getBean(XlsxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created");
            
            // We mainly check that conversion completes successfully
            // The actual formatting preservation depends on the converter's capabilities
            String pdfText = extractTextFromPdf(output);
            assertTrue(pdfText.contains("Widget A") && pdfText.contains("Widget B"),
                      "PDF should contain product data");
        }
    }
    
    @Test
    public void testLargeExcelSheet() throws Exception {
        // Create a larger Excel file to test performance with bigger files
        File input = createTempFile("large.xlsx", ".xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Large Data Set");
            
            // Create header
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < 10; c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue("Column " + (c + 1));
            }
            
            // Add 100 rows of data
            for (int r = 1; r <= 100; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < 10; c++) {
                    Cell cell = row.createCell(c);
                    if (c % 2 == 0) {
                        cell.setCellValue("Value " + r + "-" + c);
                    } else {
                        cell.setCellValue(r * (c + 1) * 1.5);
                    }
                }
            }
            
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToPdfConverter converter = ctx.getBean(XlsxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from large Excel file");
            assertTrue(output.length() > 0, "PDF file should not be empty");
            
            // Just verify it's a valid PDF
            try (PDDocument document = PDDocument.load(output)) {
                assertNotNull(document, "Should be able to load the PDF");
                assertTrue(document.getNumberOfPages() > 0, "PDF should have at least one page");
            }
        }
    }
    
    @Test
    public void testEmptyExcelFile() throws Exception {
        // Create an empty Excel file
        File input = createTempFile("empty.xlsx", ".xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create an empty sheet
            workbook.createSheet("Empty Sheet");
            
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToPdfConverter converter = ctx.getBean(XlsxToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from empty Excel file");
            
            // Verify it's a valid PDF
            try (PDDocument document = PDDocument.load(output)) {
                assertNotNull(document, "Should be a valid PDF document");
            }
        }
    }
    
    /*
     * Helper methods
     */
    
    private File createSimpleExcelFile(String filename, String[][] data) throws IOException {
        File file = createTempFile(filename, ".xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Create data rows
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < data[0].length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
        
        return file;
    }
    
    private void createSheetData(Sheet sheet, String[][] data) {
        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < data[0].length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private String extractTextFromPdf(File pdf) throws IOException {
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}