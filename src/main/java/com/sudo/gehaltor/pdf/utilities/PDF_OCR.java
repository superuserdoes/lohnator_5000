package com.sudo.gehaltor.pdf.utilities;

import com.sudo.gehaltor.config.AppConfiguration;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PDF_OCR {

    private final File trained_data_language_file = new File(AppConfiguration.TESSERACT_FILE_PATH.getValue());
    private String[] page_text_lines;  // text lines of attachment (pdf) file
    private ITesseract tesseract;

    public PDF_OCR(PDDocument pdDocument) {
        if (!trained_data_language_file.exists()) {
            trained_data_language_file.getParentFile().mkdirs();
            String download_url = AppConfiguration.TESSERACT_DOWNLOAD_URL.getValue();
            download(download_url);
        }
        setup_tesseract();
        run(pdDocument);
    }

    private void setup_tesseract() {
        tesseract = new Tesseract();
//        ITesseract tesseract = new Tesseract1();
        tesseract.setDatapath(AppConfiguration.TESSERACT_PATH.getValue());
        tesseract.setLanguage(AppConfiguration.TESSERACT_LANGUAGE.getValue()); // german language
//        tesseract.setOcrEngineMode(1);
    }

    private long download(String url) {
        try (InputStream in = URI.create(url).toURL().openStream()) {
//            tempFile.deleteOnExit();
            return Files.copy(in, Paths.get(String.valueOf(trained_data_language_file)));
        } catch (Exception e) {
            System.err.println("Couldn't download deu.traineddata from tesseract-ocr tessdata.");
        }
        return -1;
    }

    private void run(PDDocument pdDocument) {
        try {
            if (pdDocument.getNumberOfPages() > 1)
                page_text_lines = extractTextFromScannedDocument(pdDocument).split("\\R");
            else
                page_text_lines = extractTextFromScannedDocumentPage(pdDocument).split("\\R");
        } catch (Exception e) {
            System.err.println("ERROR: Couldn't process PDDocument!");
        }
    }

    private String extractTextFromScannedDocumentPage(PDDocument document) {
        StringBuilder out = new StringBuilder();
        File temp = PDF_File.convert_PDDocument_to_image(document, 0);
        try {
            String result = tesseract.doOCR(temp);
            out.append(result);
            temp.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    private String extractTextFromScannedDocument(PDDocument document) {
        StringBuilder out = new StringBuilder();
        try {
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                File tempFile = PDF_File.convert_PDDocument_to_image(document, page);

                String result = tesseract.doOCR(tempFile);
                out.append(result);

                // Delete temp file
                tempFile.delete();
            }
        } catch (Exception e) {
            System.err.println("ERROR: Couldn't process PDDocument.");
        }
        return out.toString();
    }

    public String[] getText() {
        return page_text_lines;
    }
}
