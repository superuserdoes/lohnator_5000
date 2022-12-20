package com.sudo.gehaltor.email;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.services.PDF_Executor;
import com.sudo.gehaltor.view.TopMenuBarController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class LoginController {
    @FXML private TopMenuBarController topMenuBarController;
    @FXML BorderPane loginPane;
    @FXML Button login_button;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox emailExtension;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label errorLabel;
    private Scene scene;
    private Parent root;
    private Stage stage;
    private EmailPromptController emailPromptController;
    private EmailConfig emailConfig;

    public LoginController(Stage stage){
        this.stage = stage;
        System.out.println("Test 2");
    }

    public void initialize(){
        System.out.println("initializing...");
        disableTopMenuAddFile();
        errorLabel.setText("Password or e-mail entered not correct!");
        errorLabel.setVisible(false);
        // Disable/Enable login button if fields (not) empty
        login_button.disableProperty().bind(emailField.textProperty().isEmpty().or(passwordField.textProperty().isEmpty()).or(emailExtension.valueProperty().asString().isEmpty()));
//        login_button.setOnAction(event -> errorLabel.setVisible(false));
    }

    private void disableTopMenuAddFile() {
        topMenuBarController.disableMenuBarAddFile();
    }

    // todo
    private void save_login_creds(String username, String password){
        System.out.println("Saving login credentials...");
        save_properties(username, password);
    }

    private void save_properties(String username, String password){
        String file_path =  AppConfiguration.PROGRAMS_PATH.getValue() + "email.properties";
        File path = new File(file_path);
        if (!path.exists())
            path.getParentFile().mkdirs();

        try (OutputStream output = new FileOutputStream(path)) {
            Properties prop = new Properties();
            // set the properties value
            prop.setProperty("username", username);
            prop.setProperty("password", password);
            // save properties to project root folder
            prop.store(output, null);
            System.out.println(prop);
        } catch (IOException io) {
            io.printStackTrace();
        }

        // shutdown so it can update number of threads
        PDF_Executor.getInstance().shutdown();
    }



    private boolean login_credentials(){
        String username = emailField.getText() + emailExtension.getValue().toString();
        String password = passwordField.getText();

        System.out.println("E-mail:   " + username);
        System.out.println("Password: " + password);

        if (rememberMeCheckBox.isSelected() || !rememberMeCheckBox.isSelected()) save_login_creds(username, password); // TODO just testing, not final

        try {
            emailConfig = new EmailConfig(username, password);
        } catch (MessagingException e){
            if (e instanceof AuthenticationFailedException)
                System.out.println("BRUH, wrongsies entered!");
            errorLabel.setVisible(true);
            return false;
        }
        return true;
    }

    public void login_button() {
        System.err.println("LOGIN CLICKED!");
//        emailField.setText("LOGIN CLICKED!");
        errorLabel.setVisible(false);
        if (login_credentials()){
            show_email_prompt_screen();
        } else {
            System.out.println("Wrong credentials!");
        }
    }

    public EmailPromptController getEmailPromptController() {
        return emailPromptController;
    }

    public void show_email_prompt_screen() {
        emailPromptController = new EmailPromptController(this.stage, emailConfig);
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/com/sudo/gehaltor/login/email_prompt.fxml"));
            loader.setController(emailPromptController);
            root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(AppConfiguration.PROGRAM_TITLE.getValue());
//            stage.setOnCloseRequest(actionEvent -> config.updateFile());
//            stage.show();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Stage getStage() {
        return stage;
    }




}
