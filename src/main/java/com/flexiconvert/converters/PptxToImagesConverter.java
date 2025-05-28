package com.flexiconvert.converters;

import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class PptxToImagesConverter implements FormatConverter {

    @Override
    public void convert(File pptxFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(pptxFile);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            Dimension size = ppt.getPageSize();
            String baseName = pptxFile.getName().replaceAll("(?i)\\.pptx$", "");

            int i = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle(size));
                slide.draw(graphics);

                File out = new File(pptxFile.getParent(), baseName + "_slide" + i + ".png");
                ImageIO.write(img, "png", out);
                i++;
            }
        }
    }
}
