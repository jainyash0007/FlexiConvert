package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;

public class DocxToHtmlConverter implements FormatConverter {

    @Override
    public void convert(File docxFile) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(docxFile))) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n")
                .append("<title>").append(docxFile.getName()).append("</title>\n")
                .append("</head>\n<body>\n");

            for (XWPFParagraph para : doc.getParagraphs()) {
                html.append("<p>").append(escapeHtml(para.getText())).append("</p>\n");
            }

            html.append("</body>\n</html>");

            File output = new File(docxFile.getParent(), docxFile.getName().replaceAll("(?i)\\.docx$", ".html"));
            try (FileWriter writer = new FileWriter(output)) {
                writer.write(html.toString());
            }
        }
    }

    private String escapeHtml(String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
