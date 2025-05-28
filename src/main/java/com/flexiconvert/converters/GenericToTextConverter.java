package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;

import java.io.*;

public class GenericToTextConverter implements FormatConverter {

    private final String sourceExtension;

    public GenericToTextConverter(String sourceExtension) {
        this.sourceExtension = sourceExtension;
    }

    @Override
    public void convert(File sourceFile) throws IOException {
        String content;
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            content = sb.toString();
        }

        File output = new File(sourceFile.getParent(), sourceFile.getName().replaceAll("(?i)\\" + sourceExtension + "$", ".txt"));
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(content);
        }
    }
}
