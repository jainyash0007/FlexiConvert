package com.flexiconvert;

import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class FileConverterService {

    private final Map<ConversionType, FormatConverter> converterMap = new EnumMap<>(ConversionType.class);

    public FileConverterService(ApplicationContext context) {
        List<FormatConverter> converters = new ArrayList<>(context.getBeansOfType(FormatConverter.class).values());

        for (FormatConverter converter : converters) {
            ConverterFor annotation = converter.getClass().getAnnotation(ConverterFor.class);
            if (annotation != null) {
                converterMap.put(annotation.value(), converter);
            }
        }

        // 🔧 Manually map shared Generic converters
        Map<String, FormatConverter> allBeans = context.getBeansOfType(FormatConverter.class);

        converterMap.put(ConversionType.JAVA_TO_PDF, allBeans.get("javaToPdf"));
        converterMap.put(ConversionType.PY_TO_PDF, allBeans.get("pyToPdf"));
        converterMap.put(ConversionType.HTML_TO_PDF, allBeans.get("htmlToPdf"));
        converterMap.put(ConversionType.XML_TO_PDF, allBeans.get("xmlToPdf"));

        converterMap.put(ConversionType.JAVA_TO_TXT, allBeans.get("javaToTxt"));
        converterMap.put(ConversionType.PY_TO_TXT, allBeans.get("pyToTxt"));
        converterMap.put(ConversionType.HTML_TO_TXT, allBeans.get("htmlToTxt"));
        converterMap.put(ConversionType.XML_TO_TXT, allBeans.get("xmlToTxt"));
    }

    public File convert(File inputFile, ConversionType type) throws IOException {
        FormatConverter converter = converterMap.get(type);
        if (converter == null) {
            throw new UnsupportedOperationException("Conversion type not supported: " + type);
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "flexiconvert-temp-" + UUID.randomUUID());
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        File tempInputFile = new File(tempDir, inputFile.getName());
        Files.copy(inputFile.toPath(), tempInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        converter.convert(tempInputFile);
        
        String name = tempInputFile.getName();
        int dot = name.lastIndexOf('.');
        String baseName = (dot != -1) ? name.substring(0, dot) : name;
        String outputExtension = type.name().split("_TO_")[1].toLowerCase();
        String outputFileName = baseName + "." + outputExtension;
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        File expectedOutput = new File(tempInputFile.getParent(), outputFileName);
        // System.out.println("📂 Verifying final output path: " + expectedOutput.getAbsolutePath());
        // System.out.println("📂 Output exists? " + expectedOutput.exists());
        if (expectedOutput.exists()) {
            return expectedOutput;
        }

        if (type.name().endsWith("_MEDIA_TO_IMAGES")) {
            File mediaDir = new File(tempInputFile.getParent(), baseName + "_media");
            if (mediaDir.exists()) return mediaDir;
        }
        return new File(tempDir, outputFileName);
    }

    public File createUniqueFile(File file) {
        if (!file.exists()) return file;

        String name = file.getName();
        String baseName = name;
        String extension = "";
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex > 0) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }

        int counter = 1;
        File newFile;
        do {
            newFile = new File(file.getParent(), baseName + "-" + counter + extension);
            counter++;
        } while (newFile.exists());

        return newFile;
    }

    public void convertLegacy(File inputFile, ConversionType type) throws IOException {
        convert(inputFile, type);
    }
}
