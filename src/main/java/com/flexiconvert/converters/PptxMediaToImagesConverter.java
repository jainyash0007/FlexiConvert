package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;

import java.io.*;
import java.util.List;

public class PptxMediaToImagesConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            List<XSLFPictureData> pictures = ppt.getPictureData();
            if (pictures.isEmpty()) {
                System.out.println("No embedded images found in: " + inputFile.getName());
                return;
            }

            String baseName = inputFile.getName().replaceAll("(?i)\\.pptx$", "");
            File outputDir = new File(inputFile.getParent(), baseName + "_media");
            outputDir.mkdirs();

            int index = 1;
            for (XSLFPictureData pic : pictures) {
                String ext = pic.getType().extension.replace(".", "");
                File out = new File(outputDir, baseName + "_image" + index + "." + ext);
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(pic.getData());
                }
                index++;
            }
        }
    }
}
