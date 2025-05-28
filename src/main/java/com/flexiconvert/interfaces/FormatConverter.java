package com.flexiconvert.interfaces;

import java.io.File;
import java.io.IOException;

public interface FormatConverter {
    void convert(File inputFile) throws IOException;
}
