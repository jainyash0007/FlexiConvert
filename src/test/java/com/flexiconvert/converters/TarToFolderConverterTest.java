package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class TarToFolderConverterTest extends AbstractConverterTest {

    @Test
    public void testTarExtraction() throws Exception {
        // Create a valid .tar file with one text entry
        File tarFile = new File(tempDir.toFile(), "sample.tar");
        try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(tarFile))) {
            byte[] content = "Hello from tar!".getBytes();
            TarArchiveEntry entry = new TarArchiveEntry("hello.txt");
            entry.setSize(content.length);
            tarOut.putArchiveEntry(entry);
            tarOut.write(content);
            tarOut.closeArchiveEntry();
        }

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TarToFolderConverter converter = ctx.getBean(TarToFolderConverter.class);
            converter.convert(tarFile);

            File outputDir = new File(tempDir.toFile(), "sample_untarred");
            File extracted = new File(outputDir, "hello.txt");

            assertTrue(extracted.exists(), "Extracted file should exist");
            String fileContent = Files.readString(extracted.toPath());
            assertEquals("Hello from tar!", fileContent.trim());
        }
    }

    @Test
    public void testInvalidTarThrowsException() throws Exception {
        File fakeTar = createTempFile("invalid.tar", "not a real tar archive");

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TarToFolderConverter converter = ctx.getBean(TarToFolderConverter.class);
            assertThrows(IOException.class, () -> converter.convert(fakeTar),
                    "Should throw IOException for invalid tar input");
        }
    }
}
