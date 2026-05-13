package com.niknastacy.omniparse.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.niknastacy.omniparse.util.HtmlUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DocxConverterService {
    public byte[] convertToText(MultipartFile docxFile) throws Exception {
        try (InputStream inputStream = docxFile.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();
            return text.getBytes(StandardCharsets.UTF_8);
        }
    }

    public byte[] convertToPdf(MultipartFile docxFile) throws Exception {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html><head><meta charset='UTF-8' /></head><body style='font-family: Arial, sans-serif;'>");

        try (InputStream inputStream = docxFile.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    htmlBuilder.append("<p style='font-size: 14px; margin-bottom: 10px; line-height: 1.4;'>")
                            .append(HtmlUtils.escapeHtml(text))
                            .append("</p>");
                }
            }
        }
        htmlBuilder.append("</body></html>");

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/arial.ttf"), "Arial");
            builder.withHtmlContent(htmlBuilder.toString(), "");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }
}
