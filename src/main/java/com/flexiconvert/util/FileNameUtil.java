package com.flexiconvert.util;

import java.io.File;

/**
 * Utility class for file name operations.
 * Provides common functionality for file naming to reduce code duplication.
 */
public class FileNameUtil {

    /**
     * Changes the file extension of a file.
     * 
     * @param inputFile The input file
     * @param fromExtension The current extension (case-insensitive, without dot)
     * @param toExtension The new extension (without dot)
     * @return A new File object with the changed extension in the same parent directory
     */
    public static File changeExtension(File inputFile, String fromExtension, String toExtension) {
        String newName = inputFile.getName().replaceAll("(?i)\\." + fromExtension + "$", "." + toExtension);
        return new File(inputFile.getParent(), newName);
    }

    /**
     * Gets the base name of a file without its extension.
     * 
     * @param file The file
     * @param extension The extension to remove (case-insensitive, without dot)
     * @return The base name without the extension
     */
    public static String getBaseName(File file, String extension) {
        String name = file.getName();
        if (name.toLowerCase().endsWith("." + extension.toLowerCase())) {
            return name.substring(0, name.length() - extension.length() - 1);
        }
        return name;
    }

    /**
     * Removes an extension from a filename using regex.
     * 
     * @param fileName The file name
     * @param extension The extension to remove (case-insensitive, without dot)
     * @return The filename without the extension
     */
    public static String removeExtension(String fileName, String extension) {
        return fileName.replaceAll("(?i)\\." + extension + "$", "");
    }
}
