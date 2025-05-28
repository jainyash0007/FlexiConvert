package com.flexiconvert;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractConverterTest {

    @TempDir
    public Path tempDir;

    protected File createTempFile(String filename, String content) throws IOException {
        File file = new File(tempDir.toFile(), filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    protected File getOutputFile(File inputFile, String newExtension) {
        String base = inputFile.getName().replaceFirst("[.][^.]+$", "");
        return new File(inputFile.getParent(), base + "." + newExtension);
    }
}
