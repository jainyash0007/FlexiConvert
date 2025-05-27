package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;

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
