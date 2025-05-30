package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class XlsxToCsvConverterTest extends AbstractConverterTest {

    @Test
    public void testBasicXlsxToCsvConversion() throws Exception {
        // Create a simple Excel file
        String[][] data = {
            {"Name", "Age", "Email"},
            {"John Smith", "30", "john@example.com"},
            {"Jane Doe", "25", "jane@example.com"},
            {"Bob Johnson", "45", "bob@example.com"}
        };
        
        File input = createSimpleExcelFile("basic.xlsx", data);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToCsvConverter converter = ctx.getBean(XlsxToCsvConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "csv");
            assertTrue(output.exists(), "CSV file should be created from XLSX");
            assertTrue(output.length() > 0, "CSV file should not be empty");

            // Read CSV and verify content
            List<String[]> csvData = readCsvFile(output);
            assertEquals(data.length, csvData.size(), "CSV should have same number of rows as Excel");
            
            // Verify header row
            assertArrayEquals(data[0], csvData.get(0), "Header row should match");
            
            // Verify data rows
            for (int i = 1; i < data.length; i++) {
                assertArrayEquals(data[i], csvData.get(i), "Data row " + i + " should match");
            }
        }
    }
    
    @Test
    public void testExcelWithFormulas() throws Exception {
        // Create Excel file with formulas
        File input = createTempFile("formulas.xlsx", "xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Item");
            headerRow.createCell(1).setCellValue("Quantity");
            headerRow.createCell(2).setCellValue("Price");
            headerRow.createCell(3).setCellValue("Total");
            
            // Create data rows with formulas
            String[][] rowData = {
                {"Item A", "5", "10.50", ""},
                {"Item B", "3", "15.75", ""},
                {"Item C", "10", "5.99", ""},
                {"", "", "Grand Total:", ""}
            };
            
            for (int i = 0; i < rowData.length; i++) {
                Row row = sheet.createRow(i + 1);
                // First 3 columns are data
                for (int j = 0; j < 3; j++) {
                    Cell cell = row.createCell(j);
                    if (rowData[i][j].isEmpty()) {
                        continue;
                    }
                    
                    // Try to parse as number if possible
                    try {
                        double value = Double.parseDouble(rowData[i][j]);
                        cell.setCellValue(value);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(rowData[i][j]);
                    }
                }
                
                // Add formula for total column (except last row)
                Cell totalCell = row.createCell(3);
                if (i < rowData.length - 1) {
                    totalCell.setCellFormula("B" + (i + 2) + "*C" + (i + 2));
                    // Set a value that the formula would calculate to (for CSV output)
                    if (i == 0) totalCell.setCellValue(5 * 10.50);
                    else if (i == 1) totalCell.setCellValue(3 * 15.75);
                    else if (i == 2) totalCell.setCellValue(10 * 5.99);
                }
            }
            
            // Add sum formula to last row
            Cell sumCell = sheet.getRow(4).createCell(3);
            sumCell.setCellFormula("SUM(D2:D4)");
            // Set a value that the formula would calculate to
            sumCell.setCellValue(5*10.50 + 3*15.75 + 10*5.99);
            
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToCsvConverter converter = ctx.getBean(XlsxToCsvConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "csv");
            assertTrue(output.exists(), "CSV file should be created");
            
            // Read CSV and verify formula results were converted
            List<String[]> csvData = readCsvFile(output);
            assertEquals(5, csvData.size(), "CSV should have 5 rows");
            
            // Check formula results were properly calculated
            double item1Total = Double.parseDouble(csvData.get(1)[3]);
            double item2Total = Double.parseDouble(csvData.get(2)[3]);
            double item3Total = Double.parseDouble(csvData.get(3)[3]);
            double grandTotal = Double.parseDouble(csvData.get(4)[3]);
            
            assertEquals(5 * 10.50, item1Total, 0.01, "First item total should be correct");
            assertEquals(3 * 15.75, item2Total, 0.01, "Second item total should be correct");
            assertEquals(10 * 5.99, item3Total, 0.01, "Third item total should be correct");
            assertEquals(item1Total + item2Total + item3Total, grandTotal, 0.01, "Grand total should be sum of items");
        }
    }
    
    @Test
    public void testMultipleWorksheetsExcelToCsv() throws Exception {
        // Create Excel with multiple worksheets
        File input = createTempFile("multiple_sheets", "xlsx");
        
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
            XlsxToCsvConverter converter = ctx.getBean(XlsxToCsvConverter.class);
            converter.convert(input);

            // The implementation might create multiple CSV files (one per sheet)
            // or combine them into a single CSV with separators
            // We'll check for the first approach first
            File outputSheet1 = new File(input.getParent(), "Employees.csv");
            File outputSheet2 = new File(input.getParent(), "Budget.csv");
            
            // If separate files were created
            if (outputSheet1.exists() && outputSheet2.exists()) {
                // Verify content of first sheet's CSV
                List<String[]> csvSheet1 = readCsvFile(outputSheet1);
                assertEquals(4, csvSheet1.size(), "First sheet CSV should have 4 rows");
                assertEquals("Carol White", csvSheet1.get(3)[1], "Data should match source");
                
                // Verify content of second sheet's CSV
                List<String[]> csvSheet2 = readCsvFile(outputSheet2);
                assertEquals(4, csvSheet2.size(), "Second sheet CSV should have 4 rows");
                assertEquals("Marketing", csvSheet2.get(2)[0], "Data should match source");
            } 
            // If a single combined CSV was created
            else {
                File output = getOutputFile(input, "csv");
                assertTrue(output.exists(), "CSV file should be created");
                
                // Read all content and check for data from both sheets
                List<String[]> csvData = readCsvFile(output);
                assertTrue(csvData.size() >= 8, "Combined CSV should have at least 8 rows");
                
                // Check for data from both sheets
                boolean foundAliceSmith = false;
                boolean foundMarketing = false;
                
                for (String[] row : csvData) {
                    if (row.length > 1) {
                        if ("Alice Smith".equals(row[1])) foundAliceSmith = true;
                        if ("Marketing".equals(row[0])) foundMarketing = true;
                    }
                }
                
                assertTrue(foundAliceSmith, "CSV should contain data from first sheet");
                assertTrue(foundMarketing, "CSV should contain data from second sheet");
            }
        }
    }
    
    @Test
    public void testExcelWithSpecialCharacters() throws Exception {
        // Create Excel with cells containing commas, quotes, and special characters
        File input = createTempFile("special_chars.xlsx", "xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Data with special characters that need handling in CSV
            String[][] data = {
                {"ID", "Description", "Notes"},
                {"1", "Product, with comma", "This is \"quoted\" text"},
                {"2", "Line 1\nLine 2", "Special chars: @#$%^&*()"},
                {"3", "Price: $1,234.56", "More text with \"quotes\" and, commas"}
            };
            
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                }
            }
            
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToCsvConverter converter = ctx.getBean(XlsxToCsvConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "csv");
            assertTrue(output.exists(), "CSV file should be created");
            
            // Read raw content to check escaping
            String content = readFileContents(output);
            
            // Check for proper escaping of commas and quotes
            assertTrue(content.contains("\"Product, with comma\""), 
                       "Commas in fields should be properly escaped");
            assertTrue(content.contains("\"This is \"\"quoted\"\" text\"") || 
                       content.contains("\"This is \\\"quoted\\\" text\""), 
                       "Quotes in fields should be properly escaped");
        }
    }
    
    @Test
    public void testEmptyExcelFile() throws Exception {
        // Create an empty Excel file
        File input = createTempFile("empty.xlsx", "xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Empty Sheet");
            
            try (FileOutputStream fos = new FileOutputStream(input)) {
                workbook.write(fos);
            }
        }
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XlsxToCsvConverter converter = ctx.getBean(XlsxToCsvConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "csv");
            assertTrue(output.exists(), "CSV file should be created from empty Excel file");
            
            // File should exist but might be empty
            List<String[]> csvData = readCsvFile(output);
            assertTrue(csvData.isEmpty() || (csvData.size() == 1 && csvData.get(0).length == 0), 
                      "CSV from empty Excel should be empty or have just an empty row");
        }
    }
    
    /*
     * Helper methods
     */
    
    private File createSimpleExcelFile(String filename, String[][] data) throws IOException {
        File file = createTempFile(filename, "xlsx");
        
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
    }
    
    private List<String[]> readCsvFile(File csv) throws IOException {
        List<String[]> lines = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Simple CSV parsing - this doesn't handle all CSV edge cases
                // but should be sufficient for test validation
                String[] fields;
                
                // Handle quoted fields with commas
                if (line.contains("\"")) {
                    // Use a more sophisticated approach for lines with quotes
                    List<String> fieldList = new ArrayList<>();
                    boolean inQuotes = false;
                    StringBuilder field = new StringBuilder();
                    
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        
                        if (c == '"') {
                            // Check for escaped quotes
                            if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                                field.append('"');
                                i++; // Skip the next quote
                            } else {
                                inQuotes = !inQuotes;
                            }
                        } else if (c == ',' && !inQuotes) {
                            fieldList.add(field.toString());
                            field.setLength(0);
                        } else {
                            field.append(c);
                        }
                    }
                    
                    // Add the last field
                    fieldList.add(field.toString());
                    fields = fieldList.toArray(new String[0]);
                } else {
                    // Simple case: no quotes
                    fields = line.split(",");
                }
                
                lines.add(fields);
            }
        }
        
        return lines;
    }
    
    private String readFileContents(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}