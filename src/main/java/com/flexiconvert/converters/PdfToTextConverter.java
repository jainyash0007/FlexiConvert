package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@Component
@ConverterFor(ConversionType.PDF_TO_TXT)
public class PdfToTextConverter implements FormatConverter {

    @Override
    public void convert(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            File output = new File(pdfFile.getParent(), pdfFile.getName().replaceAll("(?i)\\.pdf$", ".txt"));
            try (FileWriter writer = new FileWriter(output)) {
                writer.write(text);
            }
        }
    }
}
