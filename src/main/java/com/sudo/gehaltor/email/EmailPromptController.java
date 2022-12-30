package com.sudo.gehaltor.email;

import com.sudo.gehaltor.config.AppProperties;
import com.sudo.gehaltor.data.AppSettings;
import com.sudo.gehaltor.services.PDF_Executor;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailPromptController {
    @FXML BorderPane email_from_pane;
    @FXML GridPane grid_pane_center;
    @FXML GridPane grid_pane_bottom;
    @FXML Label progress_label;
    @FXML ProgressBar progressBar;
    @FXML TextArea textArea;
    @FXML Label email_from_label;
    @FXML TextField email_from_field;
    //@FXML ComboBox<String> email_from_field;
    @FXML ComboBox<String> email_from_extension;
    @FXML Label email_error_label;
    @FXML CheckBox save_email_checkbox;
    @FXML Button button_download;
    @FXML ToggleButton button_search_by_name;
    private String error;
    private boolean valid_email = true;
    private String email = null;
    private Stage stage;
    private EmailConfig emailConfig;
    private boolean show_error = false;
    private boolean search_by_email = true;
    private boolean isDone = false;


    public EmailPromptController(Stage stage){
        this.stage = stage;
    }

    public EmailPromptController(Stage stage, EmailConfig config){
        this.stage = stage;
        this.emailConfig = config;
    }

    public EmailPromptController(Stage stage, String error_message){
        this.stage = stage;
        error = error_message;
        show_error = true;
    }

    public void initialize(){
        enable_loading_part(false);

        if (show_error){
            email_error_label.setText(error);
            email_error_label.setTextFill(Color.RED);
        }

        button_download.setOnAction(actionEvent -> {
            if (save_email_checkbox.isSelected() && button_search_by_name.isSelected()){
                AppSettings.getInstance().setFinancial_advisor_name(email_from_field.getText());
            } else if (save_email_checkbox.isSelected() && !button_search_by_name.isSelected()){
                String email = email_from_field.getText() + email_from_extension.getValue().toString();
                System.out.println("EMAIL TO BE SET: " + email);
                AppSettings.getInstance().setFinancial_advisor_email(email);
            } else {
                AppSettings.getInstance().setFinancial_advisor_email("");
                AppSettings.getInstance().setFinancial_advisor_name("");
            }
            // save
            AppProperties.getInstance().save_properties();
            // download
            downloadAttachments();
        });

        email_from_field.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER){
                this.button_download.fire();
//                downloadAttachments();
            }
        });

        if (button_search_by_name.isSelected()){
            email_from_field.setText(AppSettings.getInstance().getFinancial_advisor_name());
        } else {
            get_advisor_email();
        }

        button_search_by_name.setOnAction(actionEvent -> {
            email_from_field.clear();
            email_from_extension.getSelectionModel().selectFirst();
            System.out.println("button_search_by_name pressed! " + button_search_by_name.isSelected());
            if (button_search_by_name.isSelected()){
                System.out.println("TOGGLED!");
                email_error_label.setVisible(false);
                email_from_field.setPromptText("name");
                email_from_label.setText("Enter sender's name:");
                email_from_extension.setDisable(true);
                email_from_extension.setOpacity(0.20);
                save_email_checkbox.setDisable(true);
                save_email_checkbox.setOpacity(0.20);
                button_download.textProperty().unbind();
                button_download.textProperty().bind(Bindings.concat("Search for ",email_from_field.textProperty()));
                search_by_email = false;
                email_from_field.setText(AppSettings.getInstance().getFinancial_advisor_name());
            } else {
                System.out.println("NOT toggled!");
                email_error_label.setVisible(false);
                email_from_label.setText("Enter sender's e-mail address:");
                email_from_field.setPromptText("e-mail");
                email_from_extension.setDisable(false);
                email_from_extension.setOpacity(1);
                save_email_checkbox.setDisable(false);
                save_email_checkbox.setOpacity(1);
                button_download.textProperty().unbind();
                button_download.setText("Download Attachment(s)");
                search_by_email = true;
                get_advisor_email();
            }
            email_from_field.requestFocus();
        });

    }

    private void get_advisor_email() {
        String email_first_part = "", email_second_part = "";
        if (!AppSettings.getInstance().getFinancial_advisor_email().isEmpty() || !AppSettings.getInstance().getFinancial_advisor_email().isBlank()){
            email_first_part = AppSettings.getInstance().getFinancial_advisor_email().substring(0, AppSettings.getInstance().getFinancial_advisor_email().indexOf('@'));
            email_second_part = AppSettings.getInstance().getFinancial_advisor_email().substring(AppSettings.getInstance().getFinancial_advisor_email().indexOf('@'));
        }
        email_from_field.setText(email_first_part);
        if (email_second_part.isEmpty() || email_second_part.isBlank())
            email_from_extension.getSelectionModel().selectFirst();
        else
            email_from_extension.setValue(email_second_part);
    }

    private void enable_loading_part(boolean enabled) {
        grid_pane_bottom.setManaged(enabled);
        progressBar.setVisible(enabled);
        textArea.setVisible(enabled);
        progress_label.setVisible(enabled);
        if (enabled){
            email_from_pane.setCenter(null);
            email_from_pane.setBottom(null);
            email_from_pane.setCenter(grid_pane_bottom);
        } else {
            email_from_pane.setCenter(null);
            email_from_pane.setBottom(null);
            email_from_pane.setCenter(grid_pane_center);
        }
    }


    private void downloadAttachments(){
        if (search_by_email){
            System.out.println("Gonna do the e-mail search by e-mail input");
            String email_from = email_from_field.getText();
            String email_extension = email_from_extension.getValue();
            if (!email_extension.contains(".") || email_from.isEmpty() || email_from.equalsIgnoreCase(" "))
                valid_email = false;
            else
                valid_email = true;

            String email = email_from + email_extension;

            if (isValidEmailAddress(email)){
                this.email = email;
                show_loading_screen(true);
            } else {
                show_error_label_message("E-Mail");
            }
        } else {
            String name = email_from_field.getText();
            if (!name.contains(".") && !name.trim().isEmpty()){
                this.email = name;
                show_loading_screen(true);
            } else {
                show_error_label_message("Name");
            }
        }
    }

    private void show_error_label_message(String text){
        email_error_label.setVisible(true);
        email_error_label.setText(text + " entered is NOT valid!");
        email_error_label.setTextFill(Color.RED);
        email_error_label.setStyle("-fx-font-weight: bold;");
    }

    private void show_loading_screen(boolean show){
        if (show){
            enable_loading_part(true);
            search_emails();
        } else {
            enable_loading_part(false);
        }
    }

    private void search_emails(){
        try {
            OLDEmail OLDEmail = new OLDEmail(emailConfig, this.email, true); // important, EMAIL

            Task task = OLDEmail.get_check_email_task();
            if (task != null) {
                // scroll to top
                task.messageProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> {
                    textArea.selectPositionCaret(textArea.getLength());
                    textArea.deselect();
                });
                progress_label.setText("Connecting...");
                progress_label.setStyle("-fx-text-fill: black;" +
                                        "-fx-font-weight: normal;");
                progressBar.progressProperty().bind(task.progressProperty());
                // update label from another thread
                OLDEmail.text_labelProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> Platform.runLater(() -> progress_label.setText(newValue.toString())));
                textArea.textProperty().bind(task.messageProperty());

                task.setOnSucceeded(workerStateEvent -> {
                    progress_label.setText("DONE!");
//                    final ArrayList<File> attachments = OLDEmail.get_downloaded_attachments();
                    onSucceeded();
                });
                task.setOnFailed(workerStateEvent -> {
                    progress_label.setText("ERROR!");
                    progress_label.setStyle("-fx-text-fill: red;" +
                            "-fx-font-weight: bold;");
                    onFailed();
                });
                PDF_Executor.getInstance().submit(task);
            }
        } catch (MessagingException e) { e.printStackTrace(); }
    }

    public boolean isDone(){
        return isDone;
    }
    private void onSucceeded() {
        isDone = true;
        stage.close();
    }


    private void onFailed(){
        textArea.textProperty().unbind();
        Task task = new Task<>() {
            @Override
            protected String call() throws Exception {
                for (int i = 5; i > 0; i--){
                    String text = "\n\n\t\t\t\t~ Please retry in " + i + " seconds. ~";
                    if (i==5) textArea.appendText(text);
                    if (i<5) textArea.setText(textArea.getText().replaceAll(
                                "\\n\\n\\t\\t\\t\\t~\\sPlease\\sretry\\sin\\s\\d\\sseconds\\.\\s~$",
                                text));
                    Thread.sleep(1000);
                }
                return null;
            }
        };
//        textArea.textProperty().bind(task.valueProperty());
        PDF_Executor.getInstance().submit(task);
        task.setOnSucceeded(workerStateEvent -> show_loading_screen(false));
    }

    private void close_window(){
        Stage stage = new Stage();
        try {
            stage = (Stage) button_download.getScene().getWindow();
            stage.close();
        } catch (Exception e){ System.out.println("STAGE couldn't be closed!"); }
        if (stage == null){
            this.stage.close();
        }
    }

    public boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException ex) {
            this.valid_email = false;
        }
        return this.valid_email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

