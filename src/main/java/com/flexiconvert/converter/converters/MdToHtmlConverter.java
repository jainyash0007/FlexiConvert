package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.*;

public class MdToHtmlConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        // Read Markdown content
        StringBuilder mdContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                mdContent.append(line).append("\n");
            }
        }

        // Parse Markdown
        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdContent.toString());

        // Render to HTML
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        // Wrap in HTML boilerplate
        String finalHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" +
                inputFile.getName() + "</title></head><body>\n" + html + "\n</body></html>";

        // Write to output file
        File outputFile = new File(inputFile.getParent(), inputFile.getName().replaceAll("(?i)\\.md$", ".html"));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(finalHtml);
        }
    }
}
