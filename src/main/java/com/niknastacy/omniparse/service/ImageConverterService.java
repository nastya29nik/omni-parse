package com.niknastacy.omniparse.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ImageConverterService {
    public byte[] convertToPdf(MultipartFile imageFile) throws Exception {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             InputStream inputStream = imageFile.getInputStream()) {

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                    doc, imageFile.getBytes(), imageFile.getOriginalFilename());

            PDRectangle pageSize = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            doc.save(os);
            return os.toByteArray();
        }
    }

    public byte[] convertToText(MultipartFile imageFile) throws Exception {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("./tessdata");

        try (InputStream inputStream = imageFile.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("Файл не является поддерживаемым изображением (JPEG, PNG, BMP).");
            }

            String resultText = tesseract.doOCR(image);
            return resultText.getBytes(StandardCharsets.UTF_8);
        }
    }
}
