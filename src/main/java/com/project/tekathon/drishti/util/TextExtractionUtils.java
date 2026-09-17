package com.project.tekathon.drishti.util;

import com.project.tekathon.drishti.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

public final class TextExtractionUtils {

    private TextExtractionUtils() {
    }

    public static String extractText(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            if (name.endsWith(".pdf")) {
                return extractPdf(file.getBytes());
            }
            if (name.endsWith(".docx")) {
                return extractDocx(file.getBytes());
            }
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("Unable to extract text from uploaded document");
        }
    }

    private static String extractPdf(byte[] bytes) throws IOException {
        try (var document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String extractDocx(byte[] bytes) throws IOException {
        try (var inputStream = new ByteArrayInputStream(bytes); var document = new XWPFDocument(inputStream)) {
            StringBuilder builder = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> builder.append(paragraph.getText()).append(System.lineSeparator()));
            return builder.toString().trim();
        }
    }
}
