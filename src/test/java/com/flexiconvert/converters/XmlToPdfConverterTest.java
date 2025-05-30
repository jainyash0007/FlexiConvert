package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class XmlToPdfConverterTest extends AbstractConverterTest {

    @Test
    public void testXmlToPdfConversion() throws Exception {
        // Create a simple XML document with some elements
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <person id="1">
                    <name>John Doe</name>
                    <email>john@example.com</email>
                    <age>30</age>
                </person>
                <person id="2">
                    <name>Jane Smith</name>
                    <email>jane@example.com</email>
                    <age>25</age>
                </person>
            </root>
            """;

        File input = createTempFile("test.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToPdfConverter converter = ctx.getBean(XmlToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from XML");
            assertTrue(output.length() > 0, "PDF file should not be empty");

            // Verify the PDF content contains key elements from the XML
            String pdfText = extractTextFromPdf(output);
            assertTrue(pdfText.contains("John Doe"), "PDF should contain 'John Doe' from XML");
            assertTrue(pdfText.contains("jane@example.com"), "PDF should contain 'jane@example.com' from XML");
        }
    }

    @Test
    public void testEmptyXml() throws Exception {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>";

        File input = createTempFile("empty.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToPdfConverter converter = ctx.getBean(XmlToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from empty XML");
            
            // Even with empty XML, the PDF should be valid
            PDDocument document = PDDocument.load(output);
            assertNotNull(document, "Should create a valid PDF document");
            document.close();
        }
    }

    @Test
    public void testInvalidXml() throws Exception {
        // Create invalid XML content
        String xmlContent = "<root><unclosed>";

        File input = createTempFile("invalid.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToPdfConverter converter = ctx.getBean(XmlToPdfConverter.class);
            
            // Should throw an exception when trying to parse invalid XML
            assertThrows(Exception.class, () -> converter.convert(input), 
                    "Should throw exception for invalid XML");
        }
    }
    
    @Test
    public void testComplexXml() throws Exception {
        // Create a more complex XML with attributes, namespaces, and nested elements
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <library xmlns:book="http://example.com/books">
                <book:book id="1" category="fiction">
                    <book:title>The Great Gatsby</book:title>
                    <book:author>F. Scott Fitzgerald</book:author>
                    <book:year>1925</book:year>
                    <book:characters>
                        <book:character>Jay Gatsby</book:character>
                        <book:character>Daisy Buchanan</book:character>
                    </book:characters>
                </book:book>
                <book:book id="2" category="non-fiction">
                    <book:title>In Cold Blood</book:title>
                    <book:author>Truman Capote</book:author>
                    <book:year>1966</book:year>
                </book:book>
            </library>
            """;

        File input = createTempFile("complex.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToPdfConverter converter = ctx.getBean(XmlToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from complex XML");
            
            // Verify the PDF contains some content from the complex XML
            String pdfText = extractTextFromPdf(output);
            assertTrue(pdfText.contains("The Great Gatsby") || 
                       pdfText.contains("Gatsby") || 
                       pdfText.contains("Fitzgerald"),
                      "PDF should contain book information");
        }
    }
    
    @Test 
    public void testLargeXml() throws Exception {
        // Generate a large XML file
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<data>\n");
        
        // Add 100 items
        for (int i = 0; i < 100; i++) {
            xmlBuilder.append("  <item id=\"").append(i).append("\">\n");
            xmlBuilder.append("    <name>Item ").append(i).append("</name>\n");
            xmlBuilder.append("    <value>").append(i * 10).append("</value>\n");
            xmlBuilder.append("  </item>\n");
        }
        
        xmlBuilder.append("</data>");
        
        File input = createTempFile("large.xml", xmlBuilder.toString());
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToPdfConverter converter = ctx.getBean(XmlToPdfConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "pdf");
            assertTrue(output.exists(), "PDF file should be created from large XML");
            assertTrue(output.length() > 0, "PDF file should not be empty");
        }
    }
    
    /**
     * Helper method to extract text content from a PDF file.
     */
    private String extractTextFromPdf(File pdf) throws IOException {
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}