package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.util.ArchiveExtractionUtil;
import com.flexiconvert.util.FileNameUtil;
import org.springframework.stereotype.Component;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.nio.file.Files;


@Component
@ConverterFor(ConversionType.TAR_TO_FOLDER)
public class TarToFolderConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        String baseName = FileNameUtil.getBaseName(inputFile, "tar");
        
        // Create extraction and marker folders
        File[] folders = ArchiveExtractionUtil.createExtractionFolders(inputFile, baseName, "_untarred");
        File outputDir = folders[0];
        File markerFolder = folders[1];

        try (InputStream fis = new FileInputStream(inputFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            TarArchiveInputStream tarIn = new TarArchiveInputStream(bis)) {

            TarArchiveEntry firstEntry = tarIn.getNextTarEntry();
            if (firstEntry == null) {
                throw new IOException("Invalid or empty TAR archive: " + inputFile.getName());
            }

            do {
                File outFile = new File(outputDir, firstEntry.getName());
                
                // Check for tar slip vulnerability
                ArchiveExtractionUtil.validateEntryPath(outFile, outputDir, firstEntry.getName());

                if (firstEntry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) parent.mkdirs();

                    try (OutputStream out = Files.newOutputStream(outFile.toPath())) {
                        tarIn.transferTo(out);
                    }
                }

            } while ((firstEntry = tarIn.getNextTarEntry()) != null);
            
            // Create marker sample file
            ArchiveExtractionUtil.createMarkerSample(outputDir, markerFolder, "sample.txt");
        }
    }
}
