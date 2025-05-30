package com.flexiconvert.config;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.converters.DocxToHtmlConverter;
import com.flexiconvert.converters.GenericToPdfConverter;
import com.flexiconvert.converters.GenericToTextConverter;
import com.flexiconvert.interfaces.FormatConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import javax.imageio.spi.IIORegistry;

@Configuration
@ComponentScan(basePackages = "com.flexiconvert")
public class AppConfig {
    @Bean
    @ConverterFor(ConversionType.JAVA_TO_TXT)
    public FormatConverter javaToTxt() {
        return new GenericToTextConverter(".java");
    }

    @Bean
    @ConverterFor(ConversionType.PY_TO_TXT)
    public FormatConverter pyToTxt() {
        return new GenericToTextConverter(".py");
    }

    @Bean
    @ConverterFor(ConversionType.HTML_TO_TXT)
    public FormatConverter htmlToTxt() {
        return new GenericToTextConverter(".html");
    }

    @Bean
    @ConverterFor(ConversionType.XML_TO_TXT)
    public FormatConverter xmlToTxt() {
        return new GenericToTextConverter(".xml");
    }

    @Bean
    @ConverterFor(ConversionType.JAVA_TO_PDF)
    public FormatConverter javaToPdf() {
        return new GenericToPdfConverter(".java");
    }

    @Bean
    @ConverterFor(ConversionType.PY_TO_PDF)
    public FormatConverter pyToPdf() {
        return new GenericToPdfConverter(".py");
    }

    @Bean
    @ConverterFor(ConversionType.HTML_TO_PDF)
    public FormatConverter htmlToPdf() {
        return new GenericToPdfConverter(".html");
    }

    @Bean
    @ConverterFor(ConversionType.XML_TO_PDF)
    public FormatConverter xmlToPdf() {
        return new GenericToPdfConverter(".xml");
    }

    @Bean
    @ConverterFor(ConversionType.DOCX_TO_HTML)
    public FormatConverter docxToHtmlConverter() {
        return new DocxToHtmlConverter();
    }
}