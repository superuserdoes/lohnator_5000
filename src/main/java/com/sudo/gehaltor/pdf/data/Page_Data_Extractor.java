package com.sudo.gehaltor.pdf.data;

import com.sudo.gehaltor.pdf.utilities.PDF_ERROR;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

// Helper class for extracting specific parts of text from a PDF file (page)
public class Page_Data_Extractor implements Data_Extractor {

    private String[] page_text_lines;  // text lines of attachment (pdf) file
    private final String DATE_FORMAT = "MMMM_yyyy";

    public Page_Data_Extractor(String[] text) {
        if (text != null) {
            this.page_text_lines = text;
        }
    }

    public void setText(String[] page_text_lines) {
        this.page_text_lines = page_text_lines;
    }

    public String[] getText() {
        return page_text_lines;
    }

    @Override
    public String getPageTitle() {
        try {
            char[] chars = page_text_lines[1].toCharArray();
            StringBuilder title = new StringBuilder();

            for (char aChar : chars) {
                if (Character.isLetter(aChar) || aChar == '-') {
                    title.append(aChar);
                } else if (Character.isWhitespace(aChar)) {
                    continue;
                }
                if (title.length() == 22) break; // Length of "LOHN-GEHALTSABRECHNUNG" = 22
            }
            return title.toString().trim();
        } catch (Exception e) {
            System.err.println(PDF_ERROR.INCOMPATIBLE_PDF.get_error_message_as_string());
            return PDF_ERROR.INCOMPATIBLE_PDF.get_error_message_as_string();
        }
    }

    @Override
    public String getDate() {
        DateFormat format = new SimpleDateFormat("MMMMyyyy", Locale.GERMANY);
        // Desired, custom formatting
        DateFormat format2 = new SimpleDateFormat(DATE_FORMAT, Locale.GERMANY);
        Date localDate;

        StringBuilder monthAndYear = new StringBuilder();
        try {
            char[] chars = page_text_lines[1].toCharArray();

            for (char aChar : chars) {
                if (Character.isLetterOrDigit(aChar)) {
                    monthAndYear.append(aChar);
                }
            }
            monthAndYear = new StringBuilder(monthAndYear.substring(21).trim()); // Length of "LOHNGEHALTSABRECHNUNG" = 21
            String month = monthAndYear.substring(0, monthAndYear.length() - 4);
            String year = monthAndYear.substring(monthAndYear.length() - 4);
            if (month.equalsIgnoreCase("Jänner")) // edge case used in Austria
                monthAndYear = new StringBuilder("Januar" + year);
            localDate = format.parse(monthAndYear.toString());
            monthAndYear = new StringBuilder(format2.format(localDate));
        } catch (ParseException e) {
            // Do nothing
        }
        return isDateValid(monthAndYear.toString()) ? monthAndYear.toString() : null;
    }

    @Override
    public boolean isDateValid(String dateString) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(DATE_FORMAT)
                .toFormatter(Locale.GERMAN);
        try {
            YearMonth ym = YearMonth.parse(dateString, formatter);
//            System.out.println(ym);
            return true;
        } catch (DateTimeParseException e) {
            //System.out.println("DATE_PARSING_ERROR: Can't parse date!");
        }
        return false;
    }

//    debug.append("[DEBUG]:\\t: " + );

    @Override
    public long getEmployeeSSNumber() {
        try {
            int ssNumberLine = 3;

            char[] chars = this.page_text_lines[ssNumberLine].toCharArray();
            StringBuilder stringBuilder = new StringBuilder();

            for (char aChar : chars) {
                if (Character.isDigit(aChar)) {
                    stringBuilder.append(aChar);
                    if (stringBuilder.length() == 10) // ssNumberLength = 10, excluding dash, ex: "1234-567890" -> "1234567890"
                        break;
                }
            }
            return Long.parseLong(stringBuilder.toString());
        } catch (Exception e){
            return PDF_ERROR.SS_NUMBER_NOT_FOUND.get_error_message_as_long();
        }
    }


    @Override
    public String getEmployeeName() {
        try {
            return getEmployeeNameAndSurname().substring(
                    0,
                    getEmployeeNameAndSurname().indexOf("\\s+")
            );
        } catch (Exception e) {
            return PDF_ERROR.NAME_NOT_FOUND.get_error_message_as_string();
//            return "NOT_FOUND";
        }
    }

    @Override
    public String getEmployeeSurname() {
        try {
            return getEmployeeNameAndSurname().substring(
                    getEmployeeNameAndSurname().indexOf("\\s+") + 1
            );
        } catch (Exception e) {
            return PDF_ERROR.NAME_NOT_FOUND.get_error_message_as_string();
//            return "NOT_FOUND";
        }
    }

    @Override
    public String getEmployeeNameAndSurname() {
        try {
            char[] chars = page_text_lines[2].toCharArray();    // Second page line containing employee full name (page line-1, indexing)
            StringBuilder stringBuilder = new StringBuilder();
            int offset = 2; // "DN: 1 NAME SURNAME..." -> length of 'DN:' = 2 (used at indexing)

            for (int i = offset; i < chars.length; i++) {
                if (Character.isLetter(chars[i])) {
                    stringBuilder.append(chars[i]);
                } else if (stringBuilder.length() > 0 && Character.isWhitespace(chars[i])) {
                    stringBuilder.append(' ');
                } else if (chars[i] == ',')
                    break;
            }
            return stringBuilder.toString().trim();
        } catch (Exception e){
            return PDF_ERROR.NAME_NOT_FOUND.get_error_message_as_string();
        }
    }
}

