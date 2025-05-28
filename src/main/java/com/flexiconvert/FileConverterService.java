package com.flexiconvert;

import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.converters.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileConverterService {

    private final Map<ConversionType, FormatConverter> converterMap = new HashMap<>();

    public FileConverterService() {
        converterMap.put(ConversionType.TXT_TO_PDF, new TextToPdfConverter());
        converterMap.put(ConversionType.DOCX_TO_PDF, new DocxToPdfConverter());
        converterMap.put(ConversionType.XLSX_TO_CSV, new XlsxToCsvConverter());
        converterMap.put(ConversionType.PDF_TO_TXT, new PdfToTextConverter());
        converterMap.put(ConversionType.DOCX_TO_TXT, new DocxToTextConverter());
        converterMap.put(ConversionType.DOCX_TO_HTML, new DocxToHtmlConverter());
        converterMap.put(ConversionType.TXT_TO_HTML, new TxtToHtmlConverter());
        converterMap.put(ConversionType.PDF_TO_IMAGES, new PdfToImagesConverter());
        converterMap.put(ConversionType.PPTX_TO_IMAGES, new PptxToImagesConverter());
        converterMap.put(ConversionType.JSON_TO_XML, new JsonToXmlConverter());
        converterMap.put(ConversionType.XML_TO_JSON, new XmlToJsonConverter());
        converterMap.put(ConversionType.XML_TO_PDF, new XmlToPdfConverter());
        converterMap.put(ConversionType.JAVA_TO_TXT, new GenericToTextConverter(".java"));
        converterMap.put(ConversionType.PY_TO_TXT, new GenericToTextConverter(".py"));
        converterMap.put(ConversionType.HTML_TO_TXT, new GenericToTextConverter(".html"));
        converterMap.put(ConversionType.XML_TO_TXT, new GenericToTextConverter(".xml"));
        converterMap.put(ConversionType.JAVA_TO_PDF, new GenericToPdfConverter(".java"));
        converterMap.put(ConversionType.PY_TO_PDF, new GenericToPdfConverter(".py"));
        converterMap.put(ConversionType.HTML_TO_PDF, new GenericToPdfConverter(".html"));
        converterMap.put(ConversionType.XML_TO_PDF, new GenericToPdfConverter(".xml"));
        converterMap.put(ConversionType.RTF_TO_TXT, new RtfToTextConverter());
        converterMap.put(ConversionType.RTF_TO_PDF, new RtfToPdfConverter());
        converterMap.put(ConversionType.MD_TO_HTML, new MdToHtmlConverter());
        converterMap.put(ConversionType.MD_TO_PDF, new MdToPdfConverter());
        converterMap.put(ConversionType.ZIP_TO_FOLDER, new ZipToFolderConverter());
        converterMap.put(ConversionType.TAR_TO_FOLDER, new TarToFolderConverter());
        converterMap.put(ConversionType.GZ_TO_FOLDER, new GzToFolderConverter());
        converterMap.put(ConversionType.JPG_TO_PNG, new JpgToPngConverter());
        converterMap.put(ConversionType.PNG_TO_JPG, new PngToJpgConverter());
        converterMap.put(ConversionType.PNG_TO_WEBP, new PngToWebpConverter());
        converterMap.put(ConversionType.WEBP_TO_PNG, new WebpToPngConverter());
        converterMap.put(ConversionType.DOCX_MEDIA_TO_IMAGES, new DocxMediaToImagesConverter());
        converterMap.put(ConversionType.PPTX_MEDIA_TO_IMAGES, new PptxMediaToImagesConverter());
        converterMap.put(ConversionType.CSV_TO_JSON, new CsvToJsonConverter());
        converterMap.put(ConversionType.JSON_TO_CSV, new JsonToCsvConverter());
        converterMap.put(ConversionType.CSV_TO_XML, new CsvToXmlConverter());
        converterMap.put(ConversionType.PPTX_TO_PDF, new PptxToPdfConverter());
        converterMap.put(ConversionType.XLSX_TO_PDF, new XlsxToPdfConverter());
    }

    public File convert(File inputFile, ConversionType type) throws IOException {
        FormatConverter converter = converterMap.get(type);

        if (converter == null) {
            throw new UnsupportedOperationException("Conversion type not supported: " + type);
        }

        // Create a temporary directory for our work
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "flexiconvert-temp-" + UUID.randomUUID());
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        // Copy the input file to the temp directory
        String inputFileName = inputFile.getName();
        File tempInputFile = new File(tempDir, inputFileName);
        Files.copy(inputFile.toPath(), tempInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Convert using the temp input file
        converter.convert(tempInputFile);

        // Figure out the output filename
        String outputExtension = type.name().split("_TO_")[1].toLowerCase();
        String baseName = inputFileName.substring(0, inputFileName.lastIndexOf('.'));
        String outputFileName = baseName + "." + outputExtension;
        
        // The converter will have created the output in the same temp directory
        return new File(tempDir, outputFileName);
    }

    /**
     * Creates a unique filename to avoid overwriting existing files
     */
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

    // For legacy calls
    public void convertLegacy(File inputFile, ConversionType type) throws IOException {
        convert(inputFile, type);
    }
}
