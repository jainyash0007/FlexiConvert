package com.yash.converter;

import java.io.File;

public class App 
{
    public static void main( String[] args )
    {
        if(args.length < 2) {
            System.out.println("Usage: java -jar converter.jar <conversion-type> <input-file-path>");
            System.out.println("Example: java -jar converter.jar TXT_TO_PDF sample.txt");
            return;
        }

        try {
            ConversionType type = ConversionType.valueOf(args[0]);
            File inputFile = new File(args[1]);

            if(!inputFile.exists()) {
                System.out.println("Input file not found: " + inputFile.getAbsolutePath());
                return;
            }

            FileConverterService converter = new FileConverterService();
            converter.convert(inputFile, type);
            
            System.out.println("Conversion completed successfully.");
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
