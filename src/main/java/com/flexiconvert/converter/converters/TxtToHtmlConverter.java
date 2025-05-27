package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;

import java.io.*;

public class TxtToHtmlConverter implements FormatConverter {

    @Override
    public void convert(File txtFile) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n")
            .append("<title>").append(txtFile.getName()).append("</title>\n")
            .append("</head>\n<body>\n<pre>\n");

        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(escapeHtml(line)).append("\n");
            }
        }

        html.append("</pre>\n</body>\n</html>");

        File output = new File(txtFile.getParent(), txtFile.getName().replaceAll("(?i)\\.txt$", ".html"));
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(html.toString());
        }
    }

    private String escapeHtml(String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
