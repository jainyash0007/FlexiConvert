package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.interfaces.FormatConverter;
import com.flexiconvert.annotations.ConverterFor;
import org.springframework.stereotype.Component;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.cos.COSName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;


@Component
@ConverterFor(ConversionType.PDF_MEDIA_TO_IMAGES)
public class PdfMediaToImagesConverter implements FormatConverter {

    @Override
    public void convert(File inputFile) throws IOException {
        File outputDir = new File(inputFile.getParent(),
                inputFile.getName().replaceAll("(?i)\\.pdf$", "") + "_media");
        outputDir.mkdirs();

        try (PDDocument document = PDDocument.load(inputFile)) {
            int imgIndex = 1;

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName xObjectName : resources.getXObjectNames()) {
                    PDXObject xObject = resources.getXObject(xObjectName);
                    if (xObject instanceof PDImageXObject image) {
                        File imgFile = new File(outputDir, "image" + imgIndex + ".png");
                        try (FileOutputStream out = new FileOutputStream(imgFile)) {
                            ImageIO.write(image.getImage(), "png", out);
                        }
                        imgIndex++;
                    }
                }
            }
        }
    }
}
