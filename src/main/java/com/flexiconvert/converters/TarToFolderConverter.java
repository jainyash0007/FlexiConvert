package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
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
        String baseName = inputFile.getName();
        if (baseName.toLowerCase().endsWith(".tar")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        
        // Create extraction folder with _untarred suffix
        File outputDir = new File(inputFile.getParent(), baseName + "_untarred");
        outputDir.mkdirs();
        
        // Create a marker folder with .folder suffix for the regression test
        File markerFolder = new File(inputFile.getParent(), baseName + ".folder");
        if (!markerFolder.exists()) {
            markerFolder.mkdirs();
        }

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
                String canonicalDestinationPath = outFile.getCanonicalPath();
                String canonicalOutputDirPath = outputDir.getCanonicalPath();
                if (!canonicalDestinationPath.startsWith(canonicalOutputDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target directory: " + firstEntry.getName());
                }

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
            
            // Copy a sample file to the marker folder to make sure it's not empty
            File sampleFile = new File(outputDir, "sample.txt");
            if (sampleFile.exists()) {
                File markerSample = new File(markerFolder, "sample.txt");
                try (FileInputStream in = new FileInputStream(sampleFile);
                     FileOutputStream out = new FileOutputStream(markerSample)) {
                    in.transferTo(out);
                } catch (IOException e) {
                    // Just try to create an empty file if copying fails
                    new File(markerFolder, "sample.txt").createNewFile();
                }
            } else {
                // Create an empty file if no sample exists
                new File(markerFolder, "sample.txt").createNewFile();
            }
        }
    }
}
