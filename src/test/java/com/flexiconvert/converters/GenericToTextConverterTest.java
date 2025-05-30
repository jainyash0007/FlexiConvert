package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class GenericToTextConverterTest extends AbstractConverterTest {

    @Test
    public void testGenericJavaToTextConversion() throws Exception {
        String content = "public class Sample {\n    // comment\n    int x = 5;\n}";

        File input = createTempFile("Sample.java", content);
        GenericToTextConverter converter = new GenericToTextConverter(".java");
        converter.convert(input);

        File output = getOutputFile(input, "txt");
        assertTrue(output.exists(), "Output text file should exist");
        String outputContent = Files.readString(output.toPath());
        assertTrue(outputContent.contains("int x = 5;"), "Converted content should match original");
    }

    @Test
    public void testEmptyFileStillCreatesText() throws Exception {
        File input = createTempFile("empty.py", "");
        GenericToTextConverter converter = new GenericToTextConverter(".py");
        converter.convert(input);

        File output = getOutputFile(input, "txt");
        assertTrue(output.exists(), "Text file should be created for empty input");
        String outputContent = Files.readString(output.toPath());
        assertTrue(outputContent.isBlank(), "Output should be empty");
    }
}
