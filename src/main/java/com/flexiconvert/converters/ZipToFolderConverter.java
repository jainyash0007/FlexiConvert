package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;

public class ZipToFolderConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // Create output directory
        String baseName = inputFile.getName().replaceAll("(?i)\\.zip$", "");
        File outputDir = new File(inputFile.getParent(), baseName + "_unzipped");
        outputDir.mkdirs();

        try (ZipFile zipFile = new ZipFile(inputFile)) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                File outFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) parent.mkdirs();

                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(outFile.toPath())) {
                        in.transferTo(out);
                    }
                }
            }
        }
    }
}
