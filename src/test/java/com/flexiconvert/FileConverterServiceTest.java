package com.flexiconvert;

import com.flexiconvert.FileConverterService;
import com.flexiconvert.ConversionType;

import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FileConverterServiceTest extends AbstractConverterTest {

    @Test
    public void testMarkdownToHtmlConversion() throws Exception {
        File input = createTempFile("sample.md", "# Hello\nThis is **Markdown**");

        FileConverterService service = new FileConverterService();
        File output = service.convert(input, ConversionType.MD_TO_HTML);

        assertNotNull(output);
        assertTrue(output.exists());
        assertTrue(output.getName().endsWith(".html"));
    }

    @Test
    public void testZipToFolderConversion() throws Exception {
        // You could prepare a zip in test/resources and copy it here
        // For now, just check that method doesn't crash
        // Use JUnit parameterized test in future for format matrix
    }

    @Test
    public void testInvalidConversionType() {
        FileConverterService service = new FileConverterService();
        File dummy = new File("fake.txt");

        // Simulate a known enum that's not mapped in converterMap (optional), or use reflection mock
        assertThrows(UnsupportedOperationException.class, () ->
            service.convert(dummy, null)  // Pass null directly for unsupported case
        );
    }
}
