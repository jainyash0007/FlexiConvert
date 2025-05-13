package com.yash.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.PrintWriter;

public class FileConverterService {

    // Decides the conversion type
    public void convert(File inputFile, ConversionType type) throws IOException {
        switch (type) {
            case TXT_TO_PDF:
                convertTxtToPdf(inputFile);
                break;
            case DOCX_TO_PDF:
                convertDocxToPdf(inputFile);
                break;
            case XLSX_TO_CSV:
                convertXlsxToCsv(inputFile);
                break;
            default:
                throw new UnsupportedOperationException("Conversion type not supported");
        }
    }

    // Converts a .txt file to .pdf using PDFBox
    private void convertTxtToPdf(File txtFile) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream content = new PDPageContentStream(doc, page);
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.setLeading(14.5f);
        content.newLineAtOffset(50,750);

        try(FileInputStream fis = new FileInputStream(txtFile)) {
            String text = new String(fis.readAllBytes());
            for(String line : text.split("\n")) {
                content.showText(line);
                content.newLine();
            }
        }
        
        content.endText();
        content.close();

        File output = new File(txtFile.getParent(), txtFile.getName().replace(".txt", ".pdf"));
        doc.save(output);
        doc.close();
    }

    // Converts a .docx file to .pdf using PDFBox
    private void convertDocxToPdf(File docxFile) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(new FileInputStream(docxFile));
             PDDocument pdf = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);

            PDPageContentStream content = new PDPageContentStream(pdf, page);
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.setLeading(14.5f);
            content.newLineAtOffset(50, 750);

            for (XWPFParagraph para : docx.getParagraphs()) {
                content.showText(para.getText());
                content.newLine();
            }

            content.endText();
            content.close();

            File output = new File(docxFile.getParent(), docxFile.getName().replace(".docx", ".pdf"));
            pdf.save(output);
        }
    }

    // Converts a .xlsx file to .csv by flattening the sheet into comma-separated format
    private void convertXlsxToCsv(File xlsxFile) throws IOException {
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