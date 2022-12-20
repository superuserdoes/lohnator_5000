package com.sudo.gehaltor.pdf.utilities;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.data.Employee;
import com.sudo.gehaltor.pdf.data.Data_Extractor;
import com.sudo.gehaltor.pdf.data.Page_Data_Extractor;
import javafx.concurrent.Task;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.sudo.gehaltor.pdf.utilities.PDF_File.removeToUnicodeMaps;
import static com.sudo.gehaltor.pdf.utilities.PDF_File.save_PDDocument_as_PDF;

public class PDF_Processor extends Task<Long> {
    private Map<Long, Employee> employees_found_in_PDF;
    public File getFile() {
        return file;
    }
    private final File file;
    private PDDocument pdDocument;
    private boolean corruptFile = false;

    public PDF_Processor(File file) {
        this.file = file;
        setting_document(file);
    }

    private void setting_document(File file) {
        try {
            // Close before opening a new one
            if (this.pdDocument != null)
                this.pdDocument.close();
            this.pdDocument = Loader.loadPDF(new File(file.getPath()));
            AccessPermission ap = pdDocument.getCurrentAccessPermission();
            if (!ap.canExtractContent()) {
                System.err.println("You do NOT have permission to extract text!");
                throw new IOException("You do NOT have permission to extract text!");
            }
        } catch (IOException e) { System.err.println("File NOT found! <=> Wrong Filetype!"); }
    }

    @Override
    protected Long call() {
        long timeStartFuture = System.currentTimeMillis();
        try {
            updateMessage("Processing <" + file.getName() + "> ...");
            // Load the PDF file again because of font caching
            if (corruptFile)
                setting_document(file);
            this.employees_found_in_PDF = new HashMap<>();
            for (int page_number = 1; page_number <= pdDocument.getNumberOfPages(); page_number++) {
                PDPage pdPage = this.pdDocument.getPage(page_number-1); //-1 because of indexing, paging starting at/with zero (0)
                if (corruptFile){
                    PDResources resources = pdPage.getResources();
                    removeToUnicodeMaps(resources);
                }
                // Set the page interval to extract. If you don't, then all pages would be extracted.
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(page_number);
                stripper.setEndPage(page_number);
                // Extracted text from PDF file
                String pdf_text = stripper.getText(this.pdDocument);
                // Data retrieval
                Page_Data_Extractor data_extractor = new Page_Data_Extractor(pdf_text.split(System.getProperty("line.separator")));
                // Check if text can be extracted, otherwise use OCR to do so
                if (data_extractor.getText().length == 0){
                    System.out.println("-----" + PDF_ERROR.INCOMPATIBLE_PDF.get_error_message_as_string() + " {" + file.getName() + "} ----- GONNA USE PDF_OCR!");
                    PDF_OCR pdf_ocr = new PDF_OCR(pdDocument);
//                    this.data_extractor = new Page_Data_Extractor(pdf_ocr.getText());
                    data_extractor.setText(pdf_ocr.getText());
                }
                // Try extracting data
                if (!extract_data(data_extractor, pdPage)){
                    // Not able to retrieve data, check(ing) Fonts
                    check_fonts(pdPage, data_extractor);
                }
                updateProgress(page_number, pdDocument.getNumberOfPages());
            }
            pdDocument.close();
        } catch (Exception e) {
            System.err.println(file.getName() + " -> Error occurred trying to process a PDF page!");
//            e.printStackTrace();
        }
        long timeEndFuture = System.currentTimeMillis();
        return timeEndFuture - timeStartFuture;
    }

    private void check_fonts(PDPage pdPage, Page_Data_Extractor data_extractor) throws Exception {
        PDResources resources = pdPage.getResources();
        // Go through all fonts
        for (COSName fontName : resources.getFontNames()) {
            PDFont font = resources.getFont(fontName);
            COSBase encoding = font.getCOSObject().getDictionaryObject(COSName.ENCODING);
            if (!COSName.IDENTITY_H.equals(encoding)) { continue; }

            String font_name = font.getName();
            System.out.println("FONT -> " + font_name);
            // AAAAAB+Arial-xxxxxxxx
            int plus_char = font_name.indexOf('+')+1;
            if (plus_char != -1) { font_name = font_name.substring(plus_char); }
            // Check if it starts with Arial
            String PDFFontName = "Arial";
            if (font.getCOSObject().containsKey(COSName.TO_UNICODE) && !font_name.startsWith(PDFFontName)) { continue; }
            // font AAAAAB+Arial = xB+y
            // fonts: xB+y, xC+y, xD+y, xE+y, xB+y-ItalicMT, xC+y-BoldItalicMT, xD+yMT, xB+y-BoldMT...
            // Check if it contains any of above or similar fonts
            if (font_name.contains(PDFFontName) && font.getName().startsWith("AAAAA")) {
                // Check if extracted text is "valid" with correct unicode mapping by testing the page's title
//                            String document_title = data_extractor.getPageTitle();
                if ( !data_extractor.getPageTitle().equalsIgnoreCase(AppConfiguration.PDF_TITLE.getValue()) ) {
                    corruptFile = true;
                }
            }
        }
        if (corruptFile){
            this.call();
            corruptFile = false;
        }
    }

    private boolean extract_data(Data_Extractor data_extractor, PDPage pdPage) throws IOException {
        // Check if the current page contains "LOHN-GEHALTSABRECHNUNG"
        String document_title = data_extractor.getPageTitle();

        if (document_title.equalsIgnoreCase(AppConfiguration.PDF_TITLE.getValue())) {
//                    debug.append("[DEBUG]:\tdocument_title: " + document_title + "\n");

            String document_date = data_extractor.getDate();
//                    debug.append("[DEBUG]:\tdocument_date: " + document_date + "\n");

            long ss_number = data_extractor.getEmployeeSSNumber();
//                    debug.append("[DEBUG]:\tss_number: " + ss_number + "\n");

            String employee_name = data_extractor.getEmployeeNameAndSurname();
//                    debug.append("[DEBUG]:\temployee_name: " + employee_name + "\n");

            String employee_first_name = employee_name.substring(0, employee_name.indexOf(' '));
//                    debug.append("[DEBUG]:\temployee_first_name: " + employee_first_name + "\n");

            String employee_last_name = employee_name.substring(employee_name.indexOf(' ') + 1);
//                    debug.append("[DEBUG]:\temployee_last_name: " + employee_last_name + "\n");

            if (!employee_name.equalsIgnoreCase(PDF_ERROR.NAME_NOT_FOUND.get_error_message_as_string())) {

//                        PDPage pdPage = this.pdDocument.getPage(page_number - 1); //-1 because of indexing, paging starting at/with zero (0)
                PDDocument pdDocument_for_saving = new PDDocument();
                pdDocument_for_saving.setAllSecurityToBeRemoved(true);
                pdDocument_for_saving.addPage(pdPage);
//                            debug.append("[DEBUG]:\tA PDPage is saved!\n");

                // new employee
                Employee employee = new Employee(ss_number, employee_first_name, employee_last_name);
                // -----------

                // save PDF ?
//                        String filename = AppConfiguration.EMPLOYEE_SAVE_PATH.getValue() + employee_name + '\\' + employee_name + '_' + document_date + AppConfiguration.FILE_EXTENSION.getValue();
                String filename = get_filename_for(employee_name, document_date);
                File pdf_file = new File(filename);
                employee.add_file(save_PDDocument_as_PDF(pdf_file, pdDocument_for_saving));
                // close the document
                pdDocument_for_saving.close();

                // convert saved PDF to image
//                employee.add_image(convert_PDF_to_image(pdf_file));

                // save found employee
//                        employees.put(ss_number, employee);
                this.employees_found_in_PDF.put(ss_number, employee);
//                        employees_manager.add_person(employee);
            }
            return true;
        }
        return false;
    }

    public Map<Long, Employee> getEmployees_found_in_PDF() {
        return employees_found_in_PDF;
    }

    private String get_filename_for(String employee_name, String document_date){
        return AppConfiguration.EMPLOYEE_SAVE_PATH.getValue() + employee_name + File.separator +
                document_date.substring(document_date.indexOf('_') + 1) + File.separator +
                employee_name + '_' + document_date + AppConfiguration.FILE_EXTENSION.getValue();
    }

}

