package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;


@Component
@ConverterFor(ConversionType.XML_TO_JSON)
public class XmlToJsonConverter implements FormatConverter {

    @Override
    public void convert(File xmlFile) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper jsonMapper = new ObjectMapper();

        JsonNode rawNode = xmlMapper.readTree(xmlFile);
        String rootElementName = extractRootElementName(xmlFile);
        JsonNode wrappedNode = jsonMapper.createObjectNode().set(rootElementName, rawNode);

        String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrappedNode);

        File output = new File(xmlFile.getParent(), xmlFile.getName().replaceAll("(?i)\\.xml$", ".json"));
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(json);
        }
    }

    private String extractRootElementName(File xmlFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(xmlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.startsWith("<") && !line.startsWith("<?")) {
                    return line.replaceAll("<(\\w+)[^>]*>.*", "$1");
                }
            }
        }
        return "root"; // fallback
    }
}
