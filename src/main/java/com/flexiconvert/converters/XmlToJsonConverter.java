package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class XmlToJsonConverter implements FormatConverter {

    @Override
    public void convert(File xmlFile) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper jsonMapper = new ObjectMapper();

        JsonNode node = xmlMapper.readTree(xmlFile);
        String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);

        File output = new File(xmlFile.getParent(), xmlFile.getName().replaceAll("(?i)\\.xml$", ".json"));
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(json);
        }
    }
}
