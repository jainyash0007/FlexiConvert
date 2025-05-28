package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ZipToFolderConverterTest extends AbstractConverterTest {

    @Test
    public void testZipExtraction() throws Exception {
        // Create test zip file with a single entry
        File zipFile = new File(tempDir.toFile(), "test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry("hello.txt"));
            zos.write("Hello from zip!".getBytes());
            zos.closeEntry();
        }

        ZipToFolderConverter converter = new ZipToFolderConverter();
        converter.convert(zipFile);

        File outputFolder = new File(tempDir.toFile(), "test_unzipped");
        File extracted = new File(outputFolder, "hello.txt");

        assertTrue(extracted.exists(), "Extracted file should exist");
        String content = Files.readString(extracted.toPath());
        assertEquals("Hello from zip!", content.trim());
    }
}
