package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.nio.file.Files;

public class TarToFolderConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        String baseName = inputFile.getName().replaceAll("(?i)\\.tar$", "");
        File outputDir = new File(inputFile.getParent(), baseName + "_untarred");
        outputDir.mkdirs();

        try (InputStream fis = new FileInputStream(inputFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(bis)) {

            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File outFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) parent.mkdirs();

                    try (OutputStream out = Files.newOutputStream(outFile.toPath())) {
                        tarIn.transferTo(out);
                    }
                }
            }
        }
    }
}
