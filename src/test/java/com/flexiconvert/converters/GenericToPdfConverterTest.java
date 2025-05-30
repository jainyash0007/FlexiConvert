package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class GenericToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testSimpleTextToPdfConversion() throws Exception {
        String content = "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}";

        File input = createTempFile("HelloWorld.java", content);
        GenericToPdfConverter converter = new GenericToPdfConverter(".java");
        converter.convert(input);

        File output = getOutputFile(input, "pdf");
        assertTrue(output.exists(), "PDF file should be created");
        assertTrue(Files.size(output.toPath()) > 0, "PDF should not be empty");
    }

    @Test
    public void testEmptyFileStillCreatesPdf() throws Exception {
        File input = createTempFile("empty.py", "");
        GenericToPdfConverter converter = new GenericToPdfConverter(".py");
        converter.convert(input);

        File output = getOutputFile(input, "pdf");
        assertTrue(output.exists(), "PDF should still be created from empty file");
        assertTrue(Files.size(output.toPath()) > 0, "PDF should not be zero bytes");
    }
}
