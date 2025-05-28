package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonToXmlConverter implements FormatConverter {

    @Override
    public void convert(File jsonFile) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        JsonNode original = jsonMapper.readTree(jsonFile);

        // Wrap in a root object if it's an array
        JsonNode wrapped;
        if (original.isArray()) {
            ObjectNode root = jsonMapper.createObjectNode();
            root.set("record", original); // each element will become <record>
            wrapped = root;
        } else {
            wrapped = original;
        }

        String xml = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapped);

        File output = new File(jsonFile.getParent(), jsonFile.getName().replaceAll("(?i)\\.json$", ".xml"));
        try (FileWriter writer = new FileWriter(output)) {
            writer.write(xml);
        }
    }
}
