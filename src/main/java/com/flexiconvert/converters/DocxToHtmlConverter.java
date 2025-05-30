package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

@ConverterFor(ConversionType.DOCX_TO_HTML)
public class DocxToHtmlConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // System.out.println("🔄 DOCX_TO_HTML: writing to " + getOutputFilePath(inputFile));

        try (XWPFDocument document = new XWPFDocument(new FileInputStream(inputFile))) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("    <meta charset=\"UTF-8\">\n")
                .append("    <title>").append(inputFile.getName().replace(".docx", "")).append("</title>\n")
                .append("    <style>\n")
                .append("        body { font-family: Arial, sans-serif; margin: 2em; }\n")
                .append("        p { margin-bottom: 1em; line-height: 1.5; }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n");

            // Convert document paragraphs to HTML
            document.getParagraphs().forEach(para -> {
                String text = para.getText();
                if (text != null && !text.isEmpty()) {
                    html.append("    <p>").append(escapeHtml(text)).append("</p>\n");
                }
            });

            html.append("</body>\n")
                .append("</html>");

            // Write HTML output
            File outputFile = new File(getOutputFilePath(inputFile));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(html.toString());
            }
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    private String getOutputFilePath(File inputFile) {
        String inputPath = inputFile.getAbsolutePath();
        String baseName = inputPath.substring(0, inputPath.lastIndexOf('.'));
        return baseName + ".html";
    }
}
