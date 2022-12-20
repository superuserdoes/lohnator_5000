package com.sudo.gehaltor.config;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum AppConfiguration {
    // ----------------------------------
    // Program settings
    // ----------------------------------
    PROGRAM_TITLE("Lohnator 5000"),
    WIDTH("940"),
    HEIGHT("970"),
    CSS_PATH("/com/example/lohnator/css/styles.css"),
    PROGRAMS_PATH(Paths.get(System.getProperty("user.home")).toFile() + File.separator + PROGRAM_TITLE.getValue() + File.separator),
    // ----------------------------------
    // Save_file(s)
    // ----------------------------------
    SETTINGS_FILE_NAME("settings.json"),
    EMPLOYEE_SAVE_PATH(PROGRAMS_PATH.value + "employee" + File.separator),
    EMPLOYEE_SAVEFILE(EMPLOYEE_SAVE_PATH.value + "employee.dat"),
    DOWNLOADS_PATH(PROGRAMS_PATH.value + "downloads" + File.separator),
    TEMP_PATH(PROGRAMS_PATH.value + "temp" + File.separator),
    TESSERACT("deu.traineddata"),
    TESSERACT_LANGUAGE("deu"),
    TESSERACT_FILE_PATH(PROGRAMS_PATH.value + "tesseract" + File.separator + TESSERACT.value),
    TESSERACT_PATH(PROGRAMS_PATH.value + "tesseract" + File.separator),
    TESSERACT_DOWNLOAD_PATH(TESSERACT_PATH.value + File.separator),
    TESSERACT_DOWNLOAD_URL("https://github.com/tesseract-ocr/tessdata/raw/4767ea922bcc460e70b87b1d303ebdfed0897da8/deu.traineddata"),
    // ----------------------------------
    // Login screen
    // ----------------------------------
//    EMAIL("@hotmail.com"),
//    PASSWORD(""),
//    EMAIL_FROM("@hotmail.com"),
//    AUTO_LOGIN("false"),
    // ----------------------------------
    // AUTO-EMAIL-SENDER
    // ----------------------------------
    LOHNATOR_EMAIL("lohnarator5000@gmail.com"),
    LOHNATOR_PASSWORD(""),
    // ----------------------------------
    // E-MAIL SERVER CONFIG
    // ----------------------------------
//    EMAIL_HOST_OUTLOOK("smtp-mail.outlook.com"),
    EMAIL_HOST_OUTLOOK("outlook.office365.com"),
    EMAIL_HOST_GMAIL("smtp.gmail.com"),
    SERVER_PROTOCOL("imaps"),
    // ----------------------------------
    // PDF file details
    // ----------------------------------
    FILE_EXTENSION(".pdf"),
    IMAGE_EXTENSION(".png"),
    PDF_TITLE("LOHN-GEHALTSABRECHNUNG"),
    // ----------------------------------
    // Dummy person(s)
    // ----------------------------------
    DUMMY_SV_NUMBER("1234567890"),
    DUMMY_NAME("No"),
    DUMMY_SURNAME("Employees"),
    DUMMY_FILE_NAME("NO_DATA"),
    DUMMY_EMAIL1("no_data_"),
    DUMMY_EMAIL2("@hotmail.com"),
    DUMMY_IMAGE_TEXT("No Data"),
    DUMMY_IMAGE_FILE_NAME("No_Data"),
    DUMMY_IMAGE_EXTENSION("png");



    private String value;

    private static final Map<String,AppConfiguration> ENUM_MAP;

    AppConfiguration(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    static {
        Map<String,AppConfiguration> map = new ConcurrentHashMap<String, AppConfiguration>();
        for (AppConfiguration instance : AppConfiguration.values()) {
            map.put(instance.getValue().toLowerCase(),instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static AppConfiguration get (String name) {
        return ENUM_MAP.get(name.toLowerCase());
    }
}