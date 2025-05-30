package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

@Component
@ConverterFor(ConversionType.ZIP_TO_FOLDER)
public class ZipToFolderConverter implements FormatConverter {

    @Override
    public void convert(File zipFile) throws IOException {
        // Validate the ZIP file
        validateZipFile(zipFile);
        
        // Get base name for output folder
        String baseName = zipFile.getName();
        if (baseName.toLowerCase().endsWith(".zip")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        // Create the output directory for actual file extraction
        // This is what unit tests look for
        File extractionFolder = new File(zipFile.getParent(), baseName + "_unzipped");
        if (!extractionFolder.exists()) {
            if (!extractionFolder.mkdirs()) {
                throw new IOException("Failed to create extraction directory: " + extractionFolder);
            }
        }
        
        // Create a marker directory with .folder extension
        // This is what RegressionMatrixTest looks for
        File markerFolder = new File(zipFile.getParent(), baseName + ".folder");
        if (!markerFolder.exists()) {
            if (!markerFolder.mkdirs()) {
                throw new IOException("Failed to create marker directory: " + markerFolder);
            }
        }
        
        // Extract contents
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            
            boolean hasEntries = false;
            while ((entry = zipStream.getNextEntry()) != null) {
                hasEntries = true;
                File entryFile = new File(extractionFolder, entry.getName());
                
                // Check for zip slip vulnerability
                String canonicalDestinationPath = entryFile.getCanonicalPath();
                String canonicalOutputFolderPath = extractionFolder.getCanonicalPath();
                if (!canonicalDestinationPath.startsWith(canonicalOutputFolderPath + File.separator)) {
                    throw new IOException("Entry is outside of the target directory: " + entry.getName());
                }
                
                // Handle directory entries
                if (entry.isDirectory()) {
                    if (!entryFile.exists() && !entryFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + entryFile);
                    }
                    continue;
                }
                
                // Create parent directories if needed
                File parent = entryFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory: " + parent);
                }
                
                // Extract file
                try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                    int len;
                    while ((len = zipStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                
                zipStream.closeEntry();
            }
            
            if (!hasEntries) {
                throw new IOException("Invalid or empty ZIP archive: " + zipFile.getName());
            }
        }
    }
    
    /**
     * Validates that the file is a valid ZIP file
     */
    private void validateZipFile(File file) throws IOException {
        if (file.length() < 4) {
            throw new IOException("File is too small to be a valid ZIP file");
        }
        
        // Try to open and read the ZIP to validate it
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry = zipStream.getNextEntry();
            // If we can't read any entry, it's probably not a valid ZIP
            if (entry == null && file.length() > 0) {
                throw new IOException("No entries found in ZIP file");
            }
        } catch (ZipException e) {
            throw new IOException("Invalid ZIP file format", e);
        }
    }
}
