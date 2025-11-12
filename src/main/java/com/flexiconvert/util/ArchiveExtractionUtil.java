package com.flexiconvert.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for archive extraction operations.
 * Provides common functionality for archive converters to reduce code duplication.
 */
public class ArchiveExtractionUtil {

    /**
     * Creates both the extraction folder and a marker folder for archive extraction.
     * The extraction folder contains the actual extracted files.
     * The marker folder is used by regression tests.
     * 
     * @param archiveFile The archive file being extracted
     * @param baseName The base name for the folders
     * @param extractionSuffix The suffix for the extraction folder (e.g., "_unzipped", "_untarred")
     * @return An array with two elements: [0] = extraction folder, [1] = marker folder
     * @throws IOException If folder creation fails
     */
    public static File[] createExtractionFolders(File archiveFile, String baseName, String extractionSuffix) 
            throws IOException {
        // Create the extraction folder for actual file extraction
        File extractionFolder = new File(archiveFile.getParent(), baseName + extractionSuffix);
        if (!extractionFolder.exists()) {
            if (!extractionFolder.mkdirs()) {
                throw new IOException("Failed to create extraction directory: " + extractionFolder);
            }
        }

        // Create the marker folder with .folder extension for regression tests
        File markerFolder = new File(archiveFile.getParent(), baseName + ".folder");
        if (!markerFolder.exists()) {
            if (!markerFolder.mkdirs()) {
                throw new IOException("Failed to create marker directory: " + markerFolder);
            }
        }

        return new File[]{extractionFolder, markerFolder};
    }

    /**
     * Validates that a file path is within the target directory to prevent path traversal attacks.
     * 
     * @param entryFile The file to validate
     * @param outputDir The target output directory
     * @param entryName The entry name for error messages
     * @throws IOException If the entry is outside the target directory
     */
    public static void validateEntryPath(File entryFile, File outputDir, String entryName) throws IOException {
        String canonicalDestinationPath = entryFile.getCanonicalPath();
        String canonicalOutputDirPath = outputDir.getCanonicalPath();
        if (!canonicalDestinationPath.startsWith(canonicalOutputDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target directory: " + entryName);
        }
    }

    /**
     * Creates a sample file in the marker folder to ensure it's not empty.
     * If a sample file exists in the extraction folder, it copies it.
     * Otherwise, it creates an empty placeholder file.
     * 
     * @param extractionFolder The folder containing extracted files
     * @param markerFolder The marker folder where the sample should be placed
     * @param sampleFileName The name of the sample file to look for/create
     * @throws IOException If file creation fails
     */
    public static void createMarkerSample(File extractionFolder, File markerFolder, String sampleFileName) 
            throws IOException {
        File sampleFile = new File(extractionFolder, sampleFileName);
        File markerSample = new File(markerFolder, sampleFileName);
        
        if (sampleFile.exists()) {
            try (FileInputStream in = new FileInputStream(sampleFile);
                 FileOutputStream out = new FileOutputStream(markerSample)) {
                in.transferTo(out);
            } catch (IOException e) {
                // If copying fails, create an empty file
                markerSample.createNewFile();
            }
        } else {
            // Create an empty placeholder file
            markerSample.createNewFile();
        }
    }
}
