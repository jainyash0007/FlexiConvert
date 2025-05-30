package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;


@Component
@ConverterFor(ConversionType.DOCX_TO_TXT)
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
