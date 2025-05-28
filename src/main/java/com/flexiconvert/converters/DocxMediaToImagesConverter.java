package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.util.List;

public class DocxMediaToImagesConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFPictureData> pictures = document.getAllPictures();
            if (pictures.isEmpty()) {
                System.out.println("No embedded media found in: " + inputFile.getName());
                return;
            }

            String baseName = inputFile.getName().replaceAll("(?i)\\.docx$", "");
            File outputDir = new File(inputFile.getParent(), baseName + "_media");
            outputDir.mkdirs();

            int index = 1;
            for (XWPFPictureData pic : pictures) {
                String ext = pic.suggestFileExtension();
                File imageFile = new File(outputDir, baseName + "_image" + index + "." + ext);
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(pic.getData());
                }
                index++;
            }
        }
    }
}
