package com.flexiconvert.converters;

import com.flexiconvert.ConversionType;
import com.flexiconvert.annotations.ConverterFor;
import com.flexiconvert.interfaces.FormatConverter;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Component
@ConverterFor(ConversionType.DOCX_TO_HTML)
public class DocxToHtmlConverter implements FormatConverter {

    private static final Set<String> HEADERS = Set.of(
            "EXPERIENCE", "PROJECTS", "SKILLS", "EDUCATION"
    );

    @Override
    public void convert(File inputFile) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(inputFile))) {

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("    <meta charset=\"UTF-8\">\n")
                .append("    <title>").append(inputFile.getName().replace(".docx", "")).append("</title>\n")
                .append("    <style>\n")
                .append("        body { font-family: Arial, sans-serif; margin: 2em; }\n")
                .append("        p { margin-bottom: 1em; line-height: 1.5; }\n")
                .append("        img { max-width: 100%; height: auto; }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n");

            // Prepare image output folder
            String baseName = inputFile.getName().replaceAll("(?i)\\.docx$", "");
            File mediaDir = new File(inputFile.getParentFile(), baseName + "_media");
            mediaDir.mkdirs();

            Map<String, String> imageMap = new HashMap<>();
            List<XWPFPictureData> pictures = document.getAllPictures();
            int imgIndex = 1;
            for (XWPFPictureData pic : pictures) {
                String ext = pic.suggestFileExtension();
                String fileName = baseName + "_image" + imgIndex + "." + ext;
                File imageFile = new File(mediaDir, fileName);
                Files.write(imageFile.toPath(), pic.getData());
                // System.out.println("🖼️ Saved image to: " + imageFile.getAbsolutePath());
                imageMap.put(pic.getPackagePart().getPartName().getName(), imageFile.getAbsolutePath());
                imgIndex++;
            }

            for (XWPFParagraph para : document.getParagraphs()) {
                String paraText = para.getText();
                if ((paraText == null || paraText.isBlank()) && para.getRuns().isEmpty()) continue;

                String lower = paraText.trim().toUpperCase();
                boolean isHeader = HEADERS.contains(lower);
                html.append("    ").append(isHeader ? "<h2>" : "<p>");

                for (IRunElement runElem : para.getIRuns()) {
                    if (!(runElem instanceof XWPFRun)) continue;
                    XWPFRun run = (XWPFRun) runElem;

                    // Handle embedded images inline
                    for (XWPFPicture picture : run.getEmbeddedPictures()) {
                        XWPFPictureData picData = picture.getPictureData();
                        if (picData != null) {
                            String key = picData.getPackagePart().getPartName().getName();
                            String absPath = imageMap.get(key);
                            if (absPath != null) {
                                String fileUri = new File(absPath).toURI().toString();
                                html.append("<img src=\"").append(fileUri).append("\" alt=\"Embedded image\">");
                            }
                        }
                    }

                    String text = run.text();
                    if (text == null || text.isBlank()) continue;

                    boolean bold = run.isBold();
                    boolean italic = run.isItalic();
                    boolean hyperlink = run instanceof XWPFHyperlinkRun;

                    StringBuilder runHtml = new StringBuilder();
                    if (hyperlink) {
                        XWPFHyperlinkRun linkRun = (XWPFHyperlinkRun) run;
                        String url = null;
                        if (linkRun.getHyperlink(document) != null) {
                            url = linkRun.getHyperlink(document).getURL();
                        }
                        if (url == null) {
                            url = "#";
                        }
                        runHtml.append("<a href=\"").append(url).append("\">");
                    }
                    if (bold && italic) {
                        runHtml.append("<b><i>").append(escapeHtml(text)).append("</i></b>");
                    } else if (bold) {
                        runHtml.append("<b>").append(escapeHtml(text)).append("</b>");
                    } else if (italic) {
                        runHtml.append("<i>").append(escapeHtml(text)).append("</i>");
                    } else {
                        runHtml.append(escapeHtml(text));
                    }
                    if (hyperlink) runHtml.append("</a>");

                    html.append(runHtml);
                }

                html.append(isHeader ? "</h2>\n" : "</p>\n");
            }

            html.append("</body>\n</html>");

            File outputFile = new File(getOutputFilePath(inputFile));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(html.toString());
            }
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private String getOutputFilePath(File inputFile) {
        String inputPath = inputFile.getAbsolutePath();
        String baseName = inputPath.substring(0, inputPath.lastIndexOf('.'));
        return baseName + ".html";
    }
}
