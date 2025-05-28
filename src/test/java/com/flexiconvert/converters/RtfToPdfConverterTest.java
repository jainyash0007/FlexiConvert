package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class RtfToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testRtfToPdfConversion() throws Exception {
        String content = "{\\rtf1\\ansi\\deff0 {\\fonttbl {\\f0 Courier;}}\n" +
                         "\\f0\\fs24 Hello RTF World!\n}";

        File input = createTempFile("sample.rtf", content);
        RtfToPdfConverter converter = new RtfToPdfConverter();
        converter.convert(input);

        File output = getOutputFile(input, "pdf");
        assertTrue(output.exists(), "PDF file should be created from RTF");
        assertTrue(Files.size(output.toPath()) > 0, "PDF file should not be empty");
    }
}
