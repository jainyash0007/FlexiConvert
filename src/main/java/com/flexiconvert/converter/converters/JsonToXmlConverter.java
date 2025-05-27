package com.flexiconvert.converter.converters;

import com.flexiconvert.converter.interfaces.FormatConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class JsonToXmlConverter implements FormatConverter {

    @Override
    public void convert(File jsonFile) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        JsonNode jsonNode = jsonMapper.readTree(jsonFile);
        String xml = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

        File output = new File(jsonFile.getParent(), jsonFile.getName().replaceAll("(?i)\\.json$", ".xml"));
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(xml);
        }
    }
}
