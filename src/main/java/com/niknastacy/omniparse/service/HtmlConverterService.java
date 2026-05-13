package com.niknastacy.omniparse.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class HtmlConverterService {
    public byte[] convertToPdf(MultipartFile htmlFile) throws Exception {
        String htmlContent = new String(htmlFile.getBytes(), StandardCharsets.UTF_8);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }
}
