package com.flexiconvert.util;

import org.apache.commons.csv.CSVFormat;

/**
 * Utility class for CSV parsing operations.
 * Provides common CSV format configurations to reduce code duplication.
 */
public class CsvParsingUtil {

    /**
     * Gets the standard CSV format configuration used across converters.
     * This includes trimming, ignoring surrounding spaces, and ignoring empty lines.
     * 
     * @return A configured CSVFormat instance
     */
    public static CSVFormat getStandardCsvFormat() {
        return CSVFormat.DEFAULT
                .withTrim()
                .withIgnoreSurroundingSpaces()
                .withIgnoreEmptyLines()
                .withAllowMissingColumnNames()
                .withSkipHeaderRecord(false);
    }
}
