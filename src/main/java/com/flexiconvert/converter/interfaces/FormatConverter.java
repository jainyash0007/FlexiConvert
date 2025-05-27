package com.flexiconvert.converter.interfaces;

import java.io.File;
import java.io.IOException;

public interface FormatConverter {
    void convert(File inputFile) throws IOException;
}
