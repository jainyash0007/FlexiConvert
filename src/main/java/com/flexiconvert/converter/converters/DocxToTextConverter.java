package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;

public class DocxToTextConverter implements FormatConverter {

    @Override
    public void convert(File docxFile) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(docxFile))) {
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph para : doc.getParagraphs()) {
                sb.append(para.getText()).append(System.lineSeparator());
            }

            File output = new File(docxFile.getParent(), docxFile.getName().replaceAll("(?i)\\.docx$", ".txt"));
            try (FileWriter writer = new FileWriter(output)) {
                writer.write(sb.toString());
            }
        }
    }
}
