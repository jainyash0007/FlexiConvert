package com.flexiconvert;

import com.flexiconvert.FileConverterService;
import com.flexiconvert.ConversionType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegressionMatrixTest {

    @Test
    public void testSampleDocsForEachConverter() throws Exception {
        FileConverterService service = new FileConverterService();

        for (ConversionType type : EnumSet.allOf(ConversionType.class)) {
            String from = type.name().split("_TO_")[0].toLowerCase();
            String resourcePath = "sampleDocs/sample." + from;

            URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);
            if (resourceUrl == null) {
                System.out.println("⚠️ Skipping " + type + " — no sample file: " + resourcePath);
                continue;
            }

            File input = new File(resourceUrl.toURI());
            File output;
            try {
                output = service.convert(input, type);
            } catch (UnsupportedOperationException e) {
                System.out.println("⚠️ Skipping unsupported: " + type);
                continue;
            }

            boolean outputExists;

            if (type == ConversionType.PDF_TO_IMAGES || type == ConversionType.PPTX_TO_IMAGES) {
                File parentDir = output.getParentFile();
                String base = output.getName().replaceAll("[.][^.]+$", "");
                File[] matches = parentDir.listFiles((dir, name) ->
                    name.startsWith(base + "_page") || name.startsWith(base + "_slide"));
                outputExists = matches != null && matches.length > 0;
            } else if (type.name().endsWith("_TO_FOLDER")) {
                outputExists = output.exists() && output.isDirectory();
            } else {
                outputExists = output.exists();
            }

            assertTrue(outputExists, "Output should exist for: " + type);
        }
    }
}
