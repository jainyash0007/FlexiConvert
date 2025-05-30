package com.flexiconvert.converters;

import com.flexiconvert.AbstractConverterTest;
import com.flexiconvert.config.AppConfig;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TxtToHtmlConverterTest extends AbstractConverterTest {

    @Test
    public void testBasicTextToHtmlConversion() throws Exception {
        // Create a simple text file
        String textContent = "Hello, world!\n\nThis is a test text file.\nIt has multiple lines.";
        
        File input = createTempFile("simple.txt", textContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TxtToHtmlConverter converter = ctx.getBean(TxtToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created from text file");
            assertTrue(output.length() > 0, "HTML file should not be empty");

            // Verify HTML content
            String htmlContent = readFileContents(output);
            
            // Check for essential HTML structure
            assertTrue(htmlContent.contains("<!DOCTYPE html>") || htmlContent.contains("<html>"), 
                      "Output should be a valid HTML document");
            assertTrue(htmlContent.contains("<body>"), "HTML should contain a body tag");
            
            // Check original text content is preserved
            assertTrue(htmlContent.contains("Hello, world!"), "Original text should be preserved");
            assertTrue(htmlContent.contains("This is a test text file."), "Original text should be preserved");
            
            // Check for proper line break handling
            assertTrue(htmlContent.contains("<pre>"), "HTML should contain <pre> tag to preserve line breaks");
        }
    }
    
    @Test
    public void testEmptyTextFile() throws Exception {
        // Create an empty text file
        File input = createTempFile("empty.txt", "");
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TxtToHtmlConverter converter = ctx.getBean(TxtToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created from empty text file");
            
            // Verify HTML structure is still valid
            String htmlContent = readFileContents(output);
            assertTrue(htmlContent.contains("<html>") || htmlContent.contains("<!DOCTYPE html>"), 
                      "Should create valid HTML structure even for empty files");
            assertTrue(htmlContent.contains("<body>"), "HTML should contain a body tag");
        }
    }
    
    @Test
    public void testTextWithSpecialCharacters() throws Exception {
        // Create text with HTML special characters
        String textContent = "Text with <special> & characters: \"quotes\", 'apostrophes', & ampersands.\n"
                + "Math symbols: 5 < 10 > 2\n"
                + "© Copyright symbol and other entities like &copy;";
        
        File input = createTempFile("special.txt", textContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TxtToHtmlConverter converter = ctx.getBean(TxtToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created");
            
            // Verify special characters are properly escaped
            String htmlContent = readFileContents(output);
            
            // Check that the text is present (possibly with HTML entities)
            // We don't check exact entity format since there are multiple valid ways to encode
            assertTrue(htmlContent.contains("special") && htmlContent.contains("characters"), 
                      "Original text should be preserved");
            
            // The < and > should be escaped or encoded
            assertFalse(htmlContent.contains("5 < 10 > 2"), 
                       "HTML special characters should be escaped");
            
            // Check that the HTML doesn't contain unescaped special characters in content areas
            // This is a bit tricky since we can't easily parse the HTML, but we can check for obvious issues
            String bodyContent = htmlContent.substring(htmlContent.indexOf("<body>"));
            assertFalse(bodyContent.matches(".*[^\\\\]<[a-z]+[^>]*>[^<]*[^\\\\]<[a-z/]+>.*"), 
                       "HTML should not contain unescaped tags from the original text");
        }
    }
    
    @Test
    public void testLargeTextFile() throws Exception {
        // Create a larger text file (100+ lines)
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 150; i++) {
            builder.append("This is line ").append(i).append(" of the test document.\n");
            
            // Add some blank lines
            if (i % 10 == 0) {
                builder.append("\n");
            }
            
            // Add some special formatting that might be recognized
            if (i % 25 == 0) {
                builder.append("SECTION ").append(i / 25).append(" HEADER\n\n");
            }
        }
        
        File input = createTempFile("large.txt", builder.toString());
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TxtToHtmlConverter converter = ctx.getBean(TxtToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created from large text file");
            assertTrue(output.length() > 0, "HTML file should not be empty");
            
            // Check that file size is reasonable (HTML should be larger than the text due to tags)
            assertTrue(output.length() > input.length(), 
                      "HTML output should be larger than input due to HTML tags");
            
            // Check for valid HTML structure
            String htmlContent = readFileContents(output);
            assertTrue(htmlContent.contains("<html>") || htmlContent.contains("<!DOCTYPE html>"), 
                      "Output should be a valid HTML document");
        }
    }
    
    @Test
    public void testTextWithUrls() throws Exception {
        // Create text with URLs that might be recognized
        String textContent = "Here are some URLs:\n"
                + "https://www.example.com\n"
                + "http://subdomain.example.org/page.html\n"
                + "Visit www.wikipedia.org for more information.\n"
                + "Send an email to user@example.com";
        
        File input = createTempFile("urls.txt", textContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TxtToHtmlConverter converter = ctx.getBean(TxtToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created");
            
            // Verify content - sophisticated converters might convert URLs to links
            String htmlContent = readFileContents(output);
            
            // Check that the original URLs are present (as text at minimum)
            assertTrue(htmlContent.contains("https://www.example.com") || 
                       htmlContent.contains("href=\"https://www.example.com\""), 
                      "URLs should be preserved in the HTML");
            
            // Ideally, URLs would be converted to links, but this depends on implementation
            if (htmlContent.contains("<a href=")) {
                System.out.println("Info: URLs were converted to hyperlinks (good!)");
            } else {
                System.out.println("Info: URLs were preserved as text only");
            }
        }
    }
    
    @Test
    public void testMarkdownStyleFormatting() throws Exception {
        // Create text with markdown-like formatting that might be recognized
        String textContent = "# This looks like a heading\n\n"
                + "This is a paragraph with **bold** and *italic* text.\n\n"
                + "- List item 1\n"
                + "- List item 2\n\n"
                + "> This is a blockquote\n\n"
                + "```\nThis is a code block\n```";
        
        File input = createTempFile("markdown.txt", textContent);
        
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TxtToHtmlConverter converter = ctx.getBean(TxtToHtmlConverter.class);
            converter.convert(input);

            File output = getOutputFile(input, "html");
            assertTrue(output.exists(), "HTML file should be created");
            
            // Verify content - sophisticated converters might recognize markdown-like syntax
            String htmlContent = readFileContents(output);
            
            // Check that original content is preserved (as text at minimum)
            assertTrue(htmlContent.contains("This looks like a heading"), "Heading text should be preserved");
            assertTrue(htmlContent.contains("bold") && htmlContent.contains("italic"), 
                      "Formatted text should be preserved");
            
            // Ideally, markdown would be converted to HTML formatting, but this depends on implementation
            if (htmlContent.contains("<h1>") || htmlContent.contains("<strong>") || 
                htmlContent.contains("<em>") || htmlContent.contains("<ul>") || 
                htmlContent.contains("<blockquote>") || htmlContent.contains("<pre>")) {
                System.out.println("Info: Markdown-like formatting was converted to HTML (good!)");
            } else {
                System.out.println("Info: Markdown-like formatting was preserved as text only");
            }
        }
    }
    
    /**
     * Helper method to read a file's contents as a string.
     */
    private String readFileContents(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}