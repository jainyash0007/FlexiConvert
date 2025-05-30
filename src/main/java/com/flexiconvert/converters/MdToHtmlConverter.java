package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.*;


@Component
@ConverterFor(ConversionType.MD_TO_HTML)
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
