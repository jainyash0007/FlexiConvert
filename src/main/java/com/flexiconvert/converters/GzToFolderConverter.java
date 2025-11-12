package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.util.ArchiveExtractionUtil;
import com.flexiconvert.util.FileNameUtil;
import org.springframework.stereotype.Component;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.Files;


@Component
@ConverterFor(ConversionType.GZ_TO_FOLDER)
public class GzToFolderConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // Extract base name without .gz extension
        String baseName = FileNameUtil.removeExtension(inputFile.getName(), "gz");

        // Create extraction and marker folders
        File[] folders = ArchiveExtractionUtil.createExtractionFolders(inputFile, baseName, "_gunzipped");
        File outputDir = folders[0];
        File markerFolder = folders[1];

        // Output file in the extraction folder
        File outputFile = new File(outputDir, baseName);

        // Extract the gzip file
        try (InputStream fis = new FileInputStream(inputFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(bis);
             OutputStream out = Files.newOutputStream(outputFile.toPath())) {

            gzipIn.transferTo(out);
        }

        // Copy the extracted file to the marker folder
        File markerFile = new File(markerFolder, baseName);
        try (InputStream in = new FileInputStream(outputFile);
             OutputStream out = Files.newOutputStream(markerFile.toPath())) {
            in.transferTo(out);
        } catch (IOException e) {
            // If copying fails, at least create an empty file
            new File(markerFolder, "placeholder.txt").createNewFile();
        }
    }
}
