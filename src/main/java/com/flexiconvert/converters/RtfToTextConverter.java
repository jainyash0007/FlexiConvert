package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;


@Component
@ConverterFor(ConversionType.RTF_TO_TXT)
public class RtfToTextConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        RTFEditorKit rtfParser = new RTFEditorKit();
        Document doc = new DefaultStyledDocument();

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            rtfParser.read(fis, doc, 0);
        } catch (BadLocationException e) {
            throw new IOException("Failed to parse RTF content during read", e);
        }

        String text;
        try {
            text = doc.getText(0, doc.getLength());
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("RTF file is empty or contains no text.");
            }
        } catch (BadLocationException e) {
            throw new IOException("Failed to extract text from RTF document", e);
        }

        File outputFile = new File(inputFile.getParent(),
                inputFile.getName().replaceAll("(?i)\\.rtf$", ".txt"));

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(text);
        }
    }
}
