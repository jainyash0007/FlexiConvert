package com.flexiconvert;

import com.flexiconvert.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegressionMatrixTest {

    private final FileConverterService service;

    public RegressionMatrixTest() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            service = ctx.getBean(FileConverterService.class);
        }
    }

    @Test
    public void testSampleDocsForEachConverter() throws Exception {
        for (ConversionType type : EnumSet.allOf(ConversionType.class)) {
            String from = switch (type) {
                case DOCX_MEDIA_TO_IMAGES -> "docx";
                case PPTX_MEDIA_TO_IMAGES -> "pptx";
                case PDF_MEDIA_TO_IMAGES  -> "pdf";
                default -> type.name().split("_TO_")[0].toLowerCase();
            };
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

            if (type.name().endsWith("_MEDIA_TO_IMAGES")) {
                File[] matches = output.listFiles((dir, name) -> name.matches(".*\\.(png|jpg|jpeg|gif|bmp)$"));
                outputExists = matches != null && matches.length > 0;
            } else if (type == ConversionType.PDF_TO_IMAGES || type == ConversionType.PPTX_TO_IMAGES) {
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

            // System.out.println("🧪 Checking output path for " + type + ": " + output.getAbsolutePath());
            // System.out.println("🧪 Output exists? " + output.exists());

            if (!output.exists()) {
                Thread.sleep(200);
            }

            assertTrue(outputExists, "Output should exist for: " + type);
        }
    }
}
