package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
@ConverterFor(ConversionType.DOCX_TO_TXT)
public class DocxToTextConverter implements FormatConverter {

    @Override
    public void convert(File docxFile) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(docxFile))) {
            StringBuilder sb = new StringBuilder();

            for (XWPFParagraph para : doc.getParagraphs()) {
                boolean isBullet = para.getNumID() != null;
                String text = para.getText();

                if (text == null || text.isBlank()) continue;

                // Add extra spacing before and after heading-style paragraphs
                if (para.getStyle() != null && para.getStyle().toLowerCase().startsWith("heading")) {
                    // Add a newline before heading if we're not at the start of the document
                    if (sb.length() > 0) {
                        sb.append(System.lineSeparator());
                    }

                    // Add the heading text with newlines after
                    sb.append(text).append(System.lineSeparator());
                    sb.append(System.lineSeparator());
                } else {
                    // Prepend bullet if it's a list item
                    if (isBullet) {
                        sb.append("• ");
                    }

                    sb.append(text).append(System.lineSeparator());
                }
            }

            File outputFile = new File(docxFile.getParent(), docxFile.getName().replaceAll("(?i)\\.docx$", ".txt"));
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                writer.write(sb.toString());
            }
        }
    }
}
