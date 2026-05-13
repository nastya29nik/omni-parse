package com.niknastacy.omniparse.controller;

import com.niknastacy.omniparse.service.DocxConverterService;
import com.niknastacy.omniparse.service.HtmlConverterService;
import com.niknastacy.omniparse.service.ImageConverterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/convert")
@Tag(name = "Document & Image Converter", description = "Stateless API для потоковой конвертации документов и OCR-распознавания в оперативной памяти")
public class ConverterController {

    private final HtmlConverterService htmlService;
    private final ImageConverterService imageService;
    private final DocxConverterService docxService;

    public ConverterController(HtmlConverterService htmlService,
                               ImageConverterService imageService,
                               DocxConverterService docxService) {
        this.htmlService = htmlService;
        this.imageService = imageService;
        this.docxService = docxService;
    }

    @Operation(summary = "Изображение ➔ PDF", description = "Упаковывает графический файл (JPEG, PNG, BMP) в PDF-страницу ровно по размерам исходной картинки.")
    @PostMapping(value = "/image-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> imageToPdf(@Parameter(description = "Файл изображения", required = true) @RequestParam("file") MultipartFile file) throws Exception {
        return createResponse(imageService.convertToPdf(file), "converted-image.pdf", MediaType.APPLICATION_PDF);
    }

    @Operation(summary = "Изображение ➔ TXT (OCR Распознавание)", description = "Распознает текст с фотографии или скриншота (Tesseract OCR) на русском и английском языках.")
    @PostMapping(value = "/image-to-txt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> imageToTxt(@Parameter(description = "Файл изображения", required = true) @RequestParam("file") MultipartFile file) throws Exception {
        return createResponse(imageService.convertToText(file), "ocr-result.txt", MediaType.TEXT_PLAIN);
    }

    @Operation(summary = "HTML ➔ PDF", description = "Рендерит HTML-код в чистый векторный PDF-документ.")
    @PostMapping(value = "/html-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> htmlToPdf(@Parameter(description = "HTML файл", required = true) @RequestParam("file") MultipartFile file) throws Exception {
        return createResponse(htmlService.convertToPdf(file), "converted-page.pdf", MediaType.APPLICATION_PDF);
    }

    @Operation(summary = "Word (DOCX) ➔ TXT", description = "Быстрое извлечение всего сырого текста из документа Microsoft Word.")
    @PostMapping(value = "/docx-to-txt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> docxToTxt(@Parameter(description = "Файл документа Word (.docx)", required = true) @RequestParam("file") MultipartFile file) throws Exception {
        return createResponse(docxService.convertToText(file), "extracted-word.txt", MediaType.TEXT_PLAIN);
    }

    @Operation(summary = "Word (DOCX) ➔ PDF", description = "Конвертация документа Word в PDF через intermediate HTML-верстку без использования жесткого диска.")
    @PostMapping(value = "/docx-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> docxToPdf(@Parameter(description = "Файл документа Word (.docx)", required = true) @RequestParam("file") MultipartFile file) throws Exception {
        return createResponse(docxService.convertToPdf(file), "converted-word.pdf", MediaType.APPLICATION_PDF);
    }

    private ResponseEntity<byte[]> createResponse(byte[] content, String filename, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}