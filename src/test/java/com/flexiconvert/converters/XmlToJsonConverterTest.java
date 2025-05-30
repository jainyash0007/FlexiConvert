package com.flexiconvert.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class XmlToJsonConverterTest extends AbstractConverterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testBasicXmlToJsonConversion() throws Exception {
        // Create a simple XML document with some elements
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <person>
                <name>John Doe</name>
                <email>john@example.com</email>
                <age>30</age>
            </person>
            """;

        File input = createTempFile("person.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToJsonConverter converter = ctx.getBean(XmlToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            assertTrue(output.exists(), "JSON file should be created from XML");
            assertTrue(output.length() > 0, "JSON file should not be empty");

            // Validate the JSON content
            JsonNode rootNode = mapper.readTree(output);
            JsonNode personNode = rootNode.get("person");
            
            assertEquals("John Doe", personNode.get("name").asText(), "Name should match");
            assertEquals("john@example.com", personNode.get("email").asText(), "Email should match");
            assertEquals(30, personNode.get("age").asInt(), "Age should match");
        }
    }

    @Test
    public void testXmlWithAttributesToJson() throws Exception {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <book id="123" category="fiction">
                <title>The Great Gatsby</title>
                <author birthyear="1896">F. Scott Fitzgerald</author>
                <year>1925</year>
                <price currency="USD">10.99</price>
            </book>
            """;

        File input = createTempFile("book.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToJsonConverter converter = ctx.getBean(XmlToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            assertTrue(output.exists(), "JSON file should be created");
            
            String jsonContent = new String(Files.readAllBytes(output.toPath()));
            JsonNode rootNode = mapper.readTree(jsonContent);
            
            // Verify that XML attributes were properly converted
            JsonNode bookNode = rootNode.get("book");
            assertEquals("123", bookNode.get("id").asText(), "Book ID attribute should be preserved");
            assertEquals("fiction", bookNode.get("category").asText(), "Category attribute should be preserved");
            assertEquals("The Great Gatsby", bookNode.get("title").asText(), "Title should match");
            
            // Check nested attribute handling
            JsonNode authorNode = bookNode.get("author");
            assertTrue(authorNode.has("birthyear"), "Author should have birthyear attribute");
            
            JsonNode priceNode = bookNode.get("price");
            assertTrue(priceNode.has("currency"), "Price should have currency attribute");
        }
    }

    @Test
    public void testEmptyXml() throws Exception {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>";

        File input = createTempFile("empty.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToJsonConverter converter = ctx.getBean(XmlToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            assertTrue(output.exists(), "JSON file should be created from empty XML");
            
            // Check the JSON output - for empty XML, expect a simple object
            JsonNode rootNode = mapper.readTree(output);
            assertTrue(rootNode.has("root"), "Should have a root node");
            assertTrue(rootNode.get("root").isEmpty(), "Root node should be empty");
        }
    }

    @Test
    public void testInvalidXml() throws Exception {
        // Create invalid XML content
        String xmlContent = "<root><unclosed>";

        File input = createTempFile("invalid.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToJsonConverter converter = ctx.getBean(XmlToJsonConverter.class);
            
            // Should throw an exception when trying to parse invalid XML
            assertThrows(Exception.class, () -> converter.convert(input), 
                    "Should throw exception for invalid XML");
        }
    }
    
    @Test
    public void testNestedXmlElements() throws Exception {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <library>
                <book>
                    <title>Book 1</title>
                    <author>
                        <firstName>John</firstName>
                        <lastName>Smith</lastName>
                        <contact>
                            <email>john@example.com</email>
                            <phone>123-456-7890</phone>
                        </contact>
                    </author>
                    <genres>
                        <genre>Fiction</genre>
                        <genre>Mystery</genre>
                    </genres>
                </book>
                <book>
                    <title>Book 2</title>
                    <author>
                        <firstName>Jane</firstName>
                        <lastName>Doe</lastName>
                    </author>
                </book>
            </library>
            """;

        File input = createTempFile("nested.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToJsonConverter converter = ctx.getBean(XmlToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            JsonNode rootNode = mapper.readTree(output);
            
            // Validate nested structure
            JsonNode libraryNode = rootNode.get("library");
            JsonNode books = libraryNode.get("book");
            assertTrue(books.isArray(), "Books should be an array");
            assertEquals(2, books.size(), "Should have 2 books");
            
            // Check first book's nested elements
            JsonNode firstBook = books.get(0);
            assertEquals("Book 1", firstBook.get("title").asText(), "First book title should match");
            
            // Check deeply nested elements
            JsonNode author = firstBook.get("author");
            assertEquals("John", author.get("firstName").asText(), "Author first name should match");
            assertEquals("Smith", author.get("lastName").asText(), "Author last name should match");
            
            // Check very deep nesting
            JsonNode contact = author.get("contact");
            assertEquals("john@example.com", contact.get("email").asText(), "Email should match");
            
            // Check array handling
            JsonNode genres = firstBook.get("genres").get("genre");
            assertTrue(genres.isArray(), "Genres should be an array");
            assertEquals(2, genres.size(), "Should have 2 genres");
            assertEquals("Fiction", genres.get(0).asText(), "First genre should be Fiction");
        }
    }
    
    @Test
    public void testXmlWithNamespaces() throws Exception {
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <order xmlns="http://example.com/orders" 
                   xmlns:cust="http://example.com/customers"
                   xmlns:prod="http://example.com/products">
                <id>12345</id>
                <cust:customer>
                    <cust:id>C001</cust:id>
                    <cust:name>Alice Johnson</cust:name>
                </cust:customer>
                <items>
                    <prod:item>
                        <prod:id>P001</prod:id>
                        <prod:name>Laptop</prod:name>
                        <prod:price>999.99</prod:price>
                    </prod:item>
                    <prod:item>
                        <prod:id>P002</prod:id>
                        <prod:name>Mouse</prod:name>
                        <prod:price>19.99</prod:price>
                    </prod:item>
                </items>
            </order>
            """;

        File input = createTempFile("namespaces.xml", xmlContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            XmlToJsonConverter converter = ctx.getBean(XmlToJsonConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "json");
            assertTrue(output.exists(), "JSON file should be created");
            
            // We're mainly checking that the conversion process completes without errors
            // The exact JSON representation of namespaces depends on the converter implementation
            String jsonContent = new String(Files.readAllBytes(output.toPath()));
            assertNotNull(jsonContent, "Should produce JSON content");
            assertTrue(jsonContent.length() > 0, "JSON should not be empty");
            
            // Just validate it's valid JSON
            JsonNode rootNode = mapper.readTree(output);
            assertNotNull(rootNode, "Should parse as valid JSON");
        }
    }
}