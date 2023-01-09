package com.sudo.lohnator_5000.pdf.utilities;

public enum PDF_ERROR {
    NAME_NOT_FOUND("NAME_NOT_FOUND"),
    INCOMPATIBLE_PDF("ERROR: Incompatible PDF file!"),
    SS_NUMBER_NOT_FOUND( -1L);

    private long error_message_l;
    private String error_message;


    PDF_ERROR(long error_message_l) {
        this.error_message_l = error_message_l;
    }

    PDF_ERROR(String error_message) {
        this.error_message = error_message;
    }

    public long get_error_message_as_long() {
        return error_message_l;
    }

    public String get_error_message_as_string() {
        return error_message;
    }
}
