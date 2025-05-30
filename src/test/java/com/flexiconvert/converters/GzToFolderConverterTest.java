package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class GzToFolderConverterTest extends AbstractConverterTest {

    @Test
    public void testGzExtraction() throws Exception {
        // Create a valid .gz file with known content
        File gzFile = new File(tempDir.toFile(), "test.txt.gz");
        try (OutputStream fos = new FileOutputStream(gzFile);
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fos)) {
            gzipOut.write("Hello from GZ!".getBytes());
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GzToFolderConverter converter = ctx.getBean(GzToFolderConverter.class);
            converter.convert(gzFile);

            File outputDir = new File(tempDir.toFile(), "test.txt_gunzipped");
            File outputFile = new File(outputDir, "test.txt");

            assertTrue(outputFile.exists(), "Extracted file should exist");
            String content = Files.readString(outputFile.toPath());
            assertEquals("Hello from GZ!", content.trim());
        }
    }

    @Test
    public void testInvalidGzThrowsException() throws Exception {
        File badGz = createTempFile("invalid.gz", "not a real gzip");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GzToFolderConverter converter = ctx.getBean(GzToFolderConverter.class);
            assertThrows(IOException.class, () -> converter.convert(badGz), "Should throw IOException on invalid gzip");
        }
    }
}
