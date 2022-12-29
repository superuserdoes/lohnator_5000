package com.sudo.gehaltor.printer;

import com.sudo.gehaltor.config.AppConfiguration;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CustomPrinterSettings {

    private Stage window;
    //---------------------------------------------
    //------------------ GENERAL ------------------
    //---------------------------------------------
    // - Print Service
    @FXML ChoiceBox<String> choice_box_ps_name;
    @FXML Label label_ps_status;
    @FXML Label label_ps_type;
    @FXML Label label_ps_info;
    @FXML CheckBox checkbox_print_service_ptf;
    // - Print Range
    @FXML RadioButton radio_print_range_all;
    @FXML RadioButton radio_print_range_pages;
    @FXML TextField text_field_pr_from;
    @FXML TextField text_field_pr_to;
    // - Copies
    @FXML Spinner<Integer> spinner_copies_num_of_copies;
    @FXML CheckBox checkbox_copies_collate;
    //---------------------------------------------
    //---------------- PAGE SETUP -----------------
    //---------------------------------------------
    // - Media
    @FXML ChoiceBox<String> choice_box_media_size;
    @FXML ChoiceBox<String> choice_box_media_source;
    // - Orientation
    @FXML RadioButton radio_page_setup_portrait;
    @FXML RadioButton radio_page_setup_landscape;
    @FXML RadioButton radio_page_setup_reverse_portrait;
    @FXML RadioButton radio_page_setup_reverse_landscape;
    // - Margins
    @FXML TextField text_field_margins_left;
    @FXML TextField text_field_margins_right;
    @FXML TextField text_field_margins_top;
    @FXML TextField text_field_margins_bottom;
    //---------------------------------------------
    //---------------- APPEARANCE -----------------
    //---------------------------------------------
    // - Color Appearance
    @FXML RadioButton radio_color_appearance_monochrome;
    @FXML RadioButton radio_color_appearance_color;
    // - Quality
    @FXML RadioButton radio_quality_draft;
    @FXML RadioButton radio_quality_normal;
    @FXML RadioButton radio_quality_high;
    // - Sides
    @FXML RadioButton radio_sides_one_side;
    @FXML RadioButton radio_sides_tumble;
    @FXML RadioButton radio_sides_duplex;
    // - Job Attributes
    @FXML CheckBox checkbox_job_attributes_banner_page;
    @FXML Spinner<Integer> spinner_job_attributes_priority;
    @FXML TextField text_field_job_attributes_job_name;
    @FXML TextField text_field_job_attributes_user_name;
    //---------------------------------------------
    //----------------- BUTTONS -------------------
    //---------------------------------------------
    @FXML Button btn_print;
    @FXML Button btn_cancel;
    //---------------------------------------------
    //---------------------------------------------
    //---------------------------------------------
    private RadioButton settings_radio_print_range = new RadioButton("All");
    private RadioButton settings_radio_orientation = new RadioButton("Portrait");
    private RadioButton settings_radio_color_appearance = new RadioButton("Monochrome");
    private RadioButton settings_radio_quality = new RadioButton("Normal");
    private RadioButton settings_radio_sides = new RadioButton("One Side");
    //---------------------------------------------
    //---------------------------------------------
    //---------------------------------------------
    private File file;
    private List<File> files;
    private SimpleStringProperty customPrinterName = new SimpleStringProperty();
    private ObservableList<String> printer_names = FXCollections.observableArrayList();

    public CustomPrinterSettings(List<File> files){
        try {
            showPrinterSettings();
            this.files = files;
            setup_appearance_job_attributes_job_name(); // FIXME
        }catch (Exception e){};
    }

    public CustomPrinterSettings(File file) {
        try {
            showPrinterSettings();
            this.file = file;
            setup_appearance_job_attributes_job_name();
        } catch (IOException e) {e.printStackTrace();}
    }

    public void initialize(){
        setup_tab_general();
        setup_tab_page_setup();
        setup_tab_appearance();
        setup_buttons();
    }

    private void setup_tab_general() {
        setup_general_labels();
        setup_general_choice_box();
        setup_general_checkbox();
        setup_general_radio_buttons();
        setup_general_spinner();
    }

    private void setup_general_labels() {
        label_ps_status.setText("Accepting jobs");
    }

    private void setup_general_checkbox() {
        checkbox_copies_collate.setDisable(true);
    }

    private void setup_general_choice_box() {
        available_printer_names();
        choice_box_ps_name.setItems(printer_names);
        choice_box_ps_name.getSelectionModel().selectFirst();
        setCustomPrinterName(choice_box_ps_name.getSelectionModel().getSelectedItem());
        choice_box_ps_name.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            System.out.println("NEW PRINTER!!!!");
            customPrinterNameProperty().bind(observableValue);
        });
    }

//    private ObservableList<String> get_printer_names() {
////        ObservableList<String> printer_names = FXCollections.observableArrayList();
//        printer_names.add("Fax");
//        printer_names.add("HPD09BEF.liwest.at (HP ENVY Inspire 7200 series)");
//        printer_names.add("Microsoft Print to PDF");
//        printer_names.add("Microsoft XPS Document Writer");
//        return printer_names;
//    }

    private void setup_general_radio_buttons() {
        radio_print_range_pages.selectedProperty().addListener(observable -> {
            System.out.println("PAGES selected!");
            if (radio_print_range_pages.isSelected()){
                text_field_pr_from.setDisable(false);
                text_field_pr_to.setDisable(false);
                // FROM
                text_field_pr_from.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue.matches("[1-9][0-9]*")) {
                        newValue = obs.getValue().replaceAll("[^1-9][^0-9]*", "");
                    }
                    if (newValue == "") newValue = "1";
                    if (Integer.parseInt(newValue.strip()) <= Integer.parseInt(text_field_pr_to.getText().strip()))
                        text_field_pr_from.setText(newValue);
                    else
                        text_field_pr_from.setText(oldValue);
                });
                // TO
                text_field_pr_to.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (!newValue.matches("[0-9]+")) {
                        newValue = obs.getValue().replaceAll("[^\\d.]", "");
                    }
                    if (newValue == "") newValue = "1";
                    if (Integer.parseInt(newValue.strip()) >= Integer.parseInt(text_field_pr_from.getText().strip()))
                        text_field_pr_to.setText(newValue);
                    else
                        text_field_pr_to.setText(oldValue);
                });
            } else {
                text_field_pr_from.setDisable(true);
                text_field_pr_to.setDisable(true);
            }
        });

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(radio_print_range_all);
        toggleGroup.getToggles().add(radio_print_range_pages);

        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            System.err.println(t1 + " selected! (Tab General - Print Range)");
            settings_radio_print_range = (RadioButton) toggleGroup.getSelectedToggle();
        });
    }

    private void setup_general_spinner() {
        int min = 1, max = 99;
        spinner_copies_num_of_copies.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min,max));
        spinner_copies_num_of_copies.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue <= max){
                System.out.println("VALUE CHANGED: " + newValue);
            }
        });
    }

    // ---------------------------------------------------------------------------------
    // ---------------------------- PAGE SETUP -----------------------------------------
    // ---------------------------------------------------------------------------------
    private void setup_tab_page_setup() {
        setup_page_setup_media_size();
        setup_page_setup_media_source();
        setup_page_setup_radio_buttons();
        setup_page_setup_text_inputs();
    }

    private void setup_page_setup_media_size() {
        choice_box_media_size.setItems(get_media_size());
        choice_box_media_size.getSelectionModel().selectFirst();
    }

    private void setup_page_setup_media_source() {
        choice_box_media_source.setItems(get_media_source());
        choice_box_media_source.getSelectionModel().selectFirst();
        choice_box_media_source.setDisable(true);
    }

    private ObservableList<String> get_media_size() {
        ObservableList<String> paper_size = FXCollections.observableArrayList();
        paper_size.add("A4 (ISO/DIN & JIS)");
        return paper_size;
    }

    private ObservableList<String> get_media_source() {
        ObservableList<String> paper_source = FXCollections.observableArrayList();
        paper_source.add("Automatically Select");
        return paper_source;
    }

    private void setup_page_setup_radio_buttons() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(radio_page_setup_portrait);
        toggleGroup.getToggles().add(radio_page_setup_landscape);
        toggleGroup.getToggles().add(radio_page_setup_reverse_portrait);
        toggleGroup.getToggles().add(radio_page_setup_reverse_landscape);
        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
//            settings_radio_orientation.textProperty().set(t1.getProperties();
            System.out.println("NAME: " + t1.getUserData());
            System.out.println("NAME: " + (RadioButton) observableValue.getValue());
            System.err.println(t1 + " selected! (Tab Page Setup - Orientation)");
            settings_radio_orientation = (RadioButton) toggleGroup.getSelectedToggle();
        });
    }

    private void setup_page_setup_text_inputs() {
        // Left
        text_field_margins_left.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,2})?")) {
//                text_field_margins_left.setText(oldValue);
                newValue = oldValue;
            }
            if (newValue == "") newValue = "1";
            text_field_margins_left.setText(newValue);
        });

        // Right
        text_field_margins_right.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,2})?")) { newValue = oldValue; }
            if (newValue == "") newValue = "1";
            text_field_margins_right.setText(newValue);
        });

        // Top
        text_field_margins_top.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,2})?")) { newValue = oldValue; }
            if (newValue == "") newValue = "1";
            text_field_margins_top.setText(newValue);
        });

        // Bottom
        text_field_margins_bottom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,2})?")) { newValue = oldValue; }
            if (newValue == "") newValue = "1";
            text_field_margins_bottom.setText(newValue);
        });
    }

    // ---------------------------------------------------------------------------------
    // ---------------------------- APPEARANCE -----------------------------------------
    // ---------------------------------------------------------------------------------

    private void setup_tab_appearance(){
        setup_appearance_color_appearance();
        setup_appearance_quality();
        setup_appearance_sides();
        setup_appearance_job_attributes();
    }

    private void setup_appearance_color_appearance() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(radio_color_appearance_monochrome);
        toggleGroup.getToggles().add(radio_color_appearance_color);
        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            System.err.println(t1 + " selected! (Tab Appearance - Color Appearance)");
            settings_radio_color_appearance = (RadioButton) toggleGroup.getSelectedToggle();
        });
    }

    private void setup_appearance_quality() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(radio_quality_draft);
        toggleGroup.getToggles().add(radio_quality_normal);
        toggleGroup.getToggles().add(radio_quality_high);
        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            System.err.println(t1 + " selected! (Tab Appearance - Quality)");
            settings_radio_quality = (RadioButton) toggleGroup.getSelectedToggle();
        });
    }

    private void setup_appearance_sides() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(radio_sides_one_side);
        toggleGroup.getToggles().add(radio_sides_tumble);
        toggleGroup.getToggles().add(radio_sides_duplex);
        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            System.err.println(t1 + " selected! (Tab Appearance - Sides)");
            settings_radio_sides = (RadioButton) toggleGroup.getSelectedToggle();
        });
    }

    private void setup_appearance_job_attributes() {
        setup_appearance_job_attributes_spinner();
//        setup_appearance_job_attributes_job_name();
        setup_appearance_job_attributes_user_name();
    }

    private void setup_appearance_job_attributes_job_name() {
        StringBuilder job_name = new StringBuilder("");
        if (files != null || !files.isEmpty()){
            files.forEach(file -> {
                job_name.append(file.getName() + ", ");
            });
            // delete comma and whitespace ', ' at the end
            job_name.deleteCharAt(job_name.length()-2);
            job_name.deleteCharAt(job_name.length()-1);
        } else
            job_name.append(this.file.getName());

        text_field_job_attributes_job_name.setText(job_name.toString());
    }

    private void setup_appearance_job_attributes_user_name() {
        text_field_job_attributes_user_name.setText(System.getProperty("user.name"));
    }

    private void setup_appearance_job_attributes_spinner() {
        int min = 1, max = 99;
        spinner_job_attributes_priority.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min,max));
        spinner_job_attributes_priority.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue <= max){
                System.out.println("VALUE CHANGED: " + newValue);
            }
        });
    }


    private void setup_buttons() {
        btn_print.setOnAction(event -> {
            System.err.println("PRINT!");
//            get_all_settings();
//            set_default_print_request_attributes();
            try {
//                print();
                print_all();
            } catch (Exception e) { e.printStackTrace(); }
        });

        btn_cancel.setOnAction(event -> {
            System.err.println("CANCEL!");
            this.window.close();
        });
    }

    private void get_all_settings() {
        System.err.println("Printer SETTINGS:");
        System.err.println("Name: " + choice_box_ps_name.getSelectionModel().selectedItemProperty().get());
        System.err.print("Print Range: " );
            if (radio_print_range_all.isSelected()) System.err.println("All");
            else{
                System.err.println("From " + text_field_pr_from.getText() + " To " + text_field_pr_to.getText());
            }
        System.err.println("Copies: " + spinner_copies_num_of_copies.valueProperty().get());
    }

    //------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------- SETTINGS -----------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private void available_printer_names(){
        PrintService[] printServices = PrinterJob.lookupPrintServices();
//        DocPrintJob printJob = printServices[0].createPrintJob(); // set it to the first printer (avoiding null)
        System.err.println("ALL PRINTERS:");
        for (int i = 0; i < printServices.length; i++) {
            System.err.println("-> " + printServices[i].getName());
            printer_names.add(printServices[i].getName());
//            if (printServices[i].getName().toLowerCase().contains(customPrinterName.get().toLowerCase())) {
//                System.err.println("PRINTER FOUND: " + printServices[i].getName());
//                printJob = printServices[i].createPrintJob(); // get the right/desired printer
//            }
        }
    }

    private void set_default_print_request_attributes(){
        System.out.println("\n\nDEFAULT SETTINGS:");
        System.out.println("Printer Name: " + choice_box_ps_name.getSelectionModel().getSelectedItem());
        if (!settings_radio_print_range.getText().equalsIgnoreCase("All"))
            System.out.println("Print Range:\n\tFrom: " + text_field_pr_from.getText() + " To: " + text_field_pr_to.getText());
        else
            System.out.println("Print Range: " + settings_radio_print_range.getText());
        System.out.println("Number of Copies: " + spinner_copies_num_of_copies.getValue());
        System.out.println("Media Size: " + choice_box_media_size.getSelectionModel().getSelectedItem());
        System.out.println("Media Source: " + choice_box_media_source.getSelectionModel().getSelectedItem());
        System.out.println("Orientation: " + settings_radio_orientation.getText());
        System.out.println("Margins:");
        System.out.println("-Left: " + text_field_margins_left.getText());
        System.out.println("-Right: " + text_field_margins_right.getText());
        System.out.println("-Top: " + text_field_margins_top.getText());
        System.out.println("-Bottom: " + text_field_margins_bottom.getText());
        System.out.println("Color Appearance: " + settings_radio_color_appearance.getText());
        System.out.println("Quality: " + settings_radio_quality.getText());
        System.out.println("Sides: " + settings_radio_sides.getText());
        System.out.println("Job Name: " + text_field_job_attributes_job_name.getText());
        System.out.println("User Name: " + text_field_job_attributes_user_name.getText());
    }

    private void print() throws IOException, PrinterException {
        System.err.println("PRINTING: " + file.getName());
        PDDocument pdDocument = Loader.loadPDF(file);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintService(setCustomPrinter().getPrintService()); // set the right/desired printer
        job.setPageable(new PDFPageable(pdDocument));
        job.setJobName(file.getName());

        job.print(get_print_request_attributes());

        System.out.println("PRINT REQUEST ATTRIBUTE SET:");
        Attribute[] attributeArray = get_print_request_attributes().toArray();
        for (Attribute a : attributeArray)
            System.out.println(a.getName() + ": " + a);
        this.window.close();
    }

    private void print_all(){
        System.out.println("CustomPrinterSettings.print_all");
        if (files != null || !files.isEmpty()){
            files.forEach(file -> {

                this.file = file;
//                setup_appearance_job_attributes_job_name();
                try { print(); } catch (Exception e) {};

//                try {
//                    PDDocument pdDocument = Loader.loadPDF(file);
//
//                    PrinterJob job = PrinterJob.getPrinterJob();
//                    job.setPrintService(setCustomPrinter().getPrintService()); // set the right/desired printer
//                    job.setPageable(new PDFPageable(pdDocument));
//                    job.setJobName(file.getName());
//
//                    job.print(get_print_request_attributes());
//                } catch (Exception e){};
            });
        }
        this.window.close();
    }

    private PrintRequestAttributeSet get_print_request_attributes(){
        // Build a set of attributes
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        // orientation
        if (settings_radio_orientation.getText().equalsIgnoreCase("portrait"))
            attributes.add(OrientationRequested.PORTRAIT);
        else if (settings_radio_orientation.getText().equalsIgnoreCase("landscape"))
            attributes.add(OrientationRequested.LANDSCAPE);
        else if (settings_radio_orientation.getText().equalsIgnoreCase("reverse portrait"))
            attributes.add(OrientationRequested.REVERSE_PORTRAIT);
        else if (settings_radio_orientation.getText().equalsIgnoreCase("reverse landscape"))
            attributes.add(OrientationRequested.REVERSE_LANDSCAPE);
        // job name
        if (!text_field_job_attributes_job_name.getText().equalsIgnoreCase(file.getName())
            && files.size() == 1){
            attributes.add(new JobName(text_field_job_attributes_job_name.getText(), Locale.GERMAN));
        } else
            attributes.add(new JobName(file.getName(), Locale.GERMAN)); // default file name
        // user name
//        if (!text_field_job_attributes_user_name.getText().equalsIgnoreCase(System.getProperty("user.name"))){
//            attributes.add(new JobOriginatingUserName(text_field_job_attributes_user_name.getText(), Locale.GERMAN));
//        }
        // paper size (fixed)
        attributes.add(MediaSizeName.ISO_A4);
        // color
        if (settings_radio_color_appearance.getText().equalsIgnoreCase("color"))
            attributes.add(Chromaticity.COLOR);
        else
            attributes.add(Chromaticity.MONOCHROME);
        // all OR from to
        if (!settings_radio_print_range.getText().equalsIgnoreCase("All")){
            System.out.println("Print Range:\n\tFrom: " + text_field_pr_from.getText() + " To: " + text_field_pr_to.getText());
            attributes.add(new PageRanges(
                    Integer.parseInt(text_field_pr_from.getText()),
                    Integer.parseInt(text_field_pr_to.getText())
            ));
        }
        // number of copies
        attributes.add(new Copies(spinner_copies_num_of_copies.getValue()));
        // sides
        if (settings_radio_sides.getText().equalsIgnoreCase("one side"))
            attributes.add(Sides.ONE_SIDED);
        else if (settings_radio_sides.getText().equalsIgnoreCase("tumble"))
            attributes.add(Sides.TUMBLE);
        else if (settings_radio_sides.getText().equalsIgnoreCase("duplex"))
            attributes.add(Sides.DUPLEX);
        // quality
        if (settings_radio_quality.getText().equalsIgnoreCase("normal"))
            attributes.add(PrintQuality.NORMAL);
        else if (settings_radio_quality.getText().equalsIgnoreCase("draft"))
            attributes.add(PrintQuality.DRAFT);
        else if (settings_radio_quality.getText().equalsIgnoreCase("high"))
            attributes.add(PrintQuality.HIGH);

        return attributes;
    }

    private DocPrintJob setCustomPrinter(){
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        DocPrintJob printJob = printServices[0].createPrintJob(); // set it to the first printer (avoiding null)
        for (int i = 0; i < printServices.length; i++) {
            System.err.println("Looking for " + choice_box_ps_name.getSelectionModel().getSelectedItem() + " in -> " + printServices[i].getName());
            if (printServices[i].getName().toLowerCase().contains(customPrinterName.get().toLowerCase())) {
                System.err.println("PRINTER FOUND: " + printServices[i].getName());
                printJob = printServices[i].createPrintJob(); // get the right/desired printer
            }
        }
        return printJob;
    }

    private PrintRequestAttributeSet get_default_print_request_attributes(){
        // Build a set of attributes
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(OrientationRequested.PORTRAIT);
        attributes.add(new JobName(file.getName(), Locale.GERMAN));
        attributes.add(MediaSizeName.ISO_A4);
        attributes.add(Chromaticity.MONOCHROME);
        attributes.add(new Copies(1));
        attributes.add(Sides.ONE_SIDED);
        attributes.add(PrintQuality.NORMAL);
        return attributes;
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------


    public void display(final String title, final String message) {
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(250);
        window.setMinHeight(100);
//        window.getIcons().add(new Image(getClass().getResourceAsStream("Smart_parking_icon.png")));

        final Label label = new Label();
        label.setText(message);
        final Button button = new Button("OK");
        button.setOnAction(λ -> window.close());
        final VBox layout = new VBox();
        layout.getChildren().addAll(label, button);
        layout.setAlignment(Pos.CENTER);

        final Scene scene = new Scene(layout);
//        scene.getStylesheets().add(getClass().getResource("mainStyle.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    public void showPrinterSettings() throws IOException {
//        System.out.println(this.getClass().getResource("/com/sudo/gehaltor/main_window/printer.fxml"));
        window = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/com/sudo/gehaltor/main_window/printer.fxml"));
        fxmlLoader.setController(this);
        Scene scene = new Scene(fxmlLoader.load());
//            scene.getStylesheets().add(getClass().getResource(AppConfiguration.CSS_PATH.getValue()).toExternalForm());
        window.setTitle(AppConfiguration.PROGRAM_TITLE.getValue() + " - Printer Settings");
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        window.setScene(scene);
//        window.showAndWait();
        window.show();
    }

    public String getCustomPrinterName() {
        return customPrinterName.get();
    }

    public SimpleStringProperty customPrinterNameProperty() {
        return customPrinterName;
    }

    public void setCustomPrinterName(String customPrinterName) {
        this.customPrinterName.set(customPrinterName);
    }
}
