package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.util.ImageConverterUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
@ConverterFor(ConversionType.JPG_TO_PNG)
public class JpgToPngConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        ImageConverterUtil.convertImage(inputFile, "jpg", "png");
    }
}
