package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.Files;

public class GzToFolderConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        String baseName = inputFile.getName().replaceAll("(?i)\\.gz$", "");
        File outputDir = new File(inputFile.getParent(), baseName + "_gunzipped");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, baseName);

        try (InputStream fis = new FileInputStream(inputFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(bis);
             OutputStream out = Files.newOutputStream(outputFile.toPath())) {

            gzipIn.transferTo(out);
        }
    }
}
