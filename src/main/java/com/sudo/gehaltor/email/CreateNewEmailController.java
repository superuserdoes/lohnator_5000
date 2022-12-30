package com.sudo.gehaltor.email;


import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.data.AppSettings;
import com.sudo.gehaltor.model.Employees;
import com.sudo.gehaltor.services.PDF_Executor;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class CreateNewEmailController {

    @FXML TextField textField_sender_email;
    @FXML TextField textField_recipient_email;
    @FXML TextField textField_subject;
    @FXML Button btn_add_file;
    @FXML Button btn_send_email;
    @FXML HTMLEditor htmlEditor_message_text;
    @FXML TextArea textArea_attachments;
    private List<File> attachments;

    public CreateNewEmailController(List<File> attachments) {
        this.attachments = attachments;
    }

    public void initialize(){
        set_sender();
        set_receiver();
        set_subject();
        set_attachments();
        set_message_text();
        change_attachments();
        send_email();
    }

    private void send_email() {
        btn_send_email.disableProperty().bind(
                Bindings.when(textField_recipient_email.textProperty().isEmpty())
                        .then(true)
                        .otherwise(false)
        );

        Task send_email = new Task() {
            @Override
            protected Object call() throws Exception {
                OLDEmail OLDEmail = new OLDEmail();
                OLDEmail.send_message_and_or_file(Employees.getCurrentEmployee().getEmail(), textField_subject.getText(), htmlEditor_message_text.getHtmlText(), attachments);
                return true;
            }
        };

        btn_send_email.setOnAction(actionEvent -> {
            PDF_Executor.getInstance().submit(send_email);
            // close window
            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
            // executor shutdown
            send_email.setOnSucceeded(workerStateEvent -> PDF_Executor.getInstance().shutdown() );
        });
    }

    private void change_attachments() {
        btn_add_file.setOnAction(actionEvent -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(AppConfiguration.PROGRAM_TITLE.getValue() + " - Add attachment(s)");
            chooser.setInitialDirectory(Paths.get(AppConfiguration.EMPLOYEE_SAVE_PATH.getValue() + Employees.getCurrentEmployee().getFullName()).toFile());
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files","*.pdf", "*.PDF"));
            List<File> files = chooser.showOpenMultipleDialog(((Node) actionEvent.getSource()).getScene().getWindow());
            if ( files != null){
                attachments = files;
                set_attachments();
                set_message_text();
            }
        });
    }

    private void set_receiver() {
        textField_recipient_email.setText(Employees.getCurrentEmployee().getEmail());
    }

    private void set_sender() {
        textField_sender_email.setText(AppSettings.getInstance().getEmail());
    }

    private void set_subject() {
        textField_subject.setText("Lohnzettel");
    }

    private void set_attachments(){
        StringBuilder all_files = new StringBuilder();
        attachments.forEach(file -> all_files.append(file.getName() + ", "));
        // delete comma and whitespace ', ' at the end
        all_files.deleteCharAt(all_files.length()-2);
        all_files.deleteCharAt(all_files.length()-1);
        textArea_attachments.setText(all_files.toString());
    }

    private void set_message_text(){
        StringBuilder message = new StringBuilder();
        message.append("<html>\n" +
                "       <head>\n" +
                "           <meta charset=\"utf-8\"/>" +
                "           <style>\n" +
                "               body {\n" +
                "                       font-family: \"times new roman\";\n" +
                "                    }\n" +
                "           </style>" +
                "       </head>");
        message.append("<body>Hallo " + Employees.getCurrentEmployee().getName() + ",</br></br>");
        if (attachments.size() == 1)
            message.append("Ich schicke dir den Lohnzettel, bitte sieh dir den Anhang an.</br></br>");
        else
            message.append("Ich schicke dir die Lohnzettel, bitte sieh dir die Anhaege an.</br></br>");
        message.append("Mit freundlichen Grüßen,</br>Dein Chef" +
                        "</body>" +
                        "</html>");
        htmlEditor_message_text.setHtmlText(message.toString());
    }

}
