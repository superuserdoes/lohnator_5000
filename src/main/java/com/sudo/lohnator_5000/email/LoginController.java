package com.sudo.lohnator_5000.email;

import com.sudo.lohnator_5000.config.AppConfiguration;
import com.sudo.lohnator_5000.config.AppProperties;
import com.sudo.lohnator_5000.data.AppSettings;
import com.sudo.lohnator_5000.services.PDF_Executor;
import com.sudo.lohnator_5000.view.TopMenuBarController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import java.io.IOException;

public class LoginController {
    @FXML private TopMenuBarController topMenuBarController;
    @FXML BorderPane loginPane;
    @FXML Button login_button;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox emailExtension;
    @FXML private ToggleButton tglBtn_show_password;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label errorLabel;
    private Scene scene;
    private Parent root;
    private Stage stage;
    private SimpleBooleanProperty showPassword  = new SimpleBooleanProperty();
    private Tooltip toolTip;
    private EmailPromptController emailPromptController;
    private EmailConfig emailConfig;

    public LoginController(Stage stage){
        this.stage = stage;
    }

    public void initialize(){
        setup_fields();
        errorLabel.setText("Password or e-mail entered not correct!");
        errorLabel.setVisible(false);
        // Disable/Enable login button if fields (not) empty
        login_button.disableProperty().bind(emailField.textProperty().isEmpty().or(passwordField.textProperty().isEmpty()).or(emailExtension.valueProperty().asString().isEmpty()));
//        login_button.setOnAction(event -> errorLabel.setVisible(false));
    }

    private void setup_fields() {
        setup_email_credentials(emailField, emailExtension);

        passwordField.setText(AppSettings.getInstance().getPassword());
        rememberMeCheckBox.setSelected(AppSettings.getInstance().isAuto_login());

        passwordField.setOnKeyTyped(keyEvent -> {
            if ( showPassword.get() ) {
                showPassword();
            }
        });

        toolTip = new Tooltip();
        showPassword.bind(tglBtn_show_password.selectedProperty());
        showPassword.addListener((observable, oldValue, newValue) -> {
            if (newValue) showPassword();
            else hidePassword();
        });
    }

    static void setup_email_credentials(TextField emailField, ComboBox emailExtension) {
        String email_first_part = "", email_second_part = "";
        if (!AppSettings.getInstance().getEmail().isEmpty() || !AppSettings.getInstance().getEmail().isBlank()){
            email_first_part = AppSettings.getInstance().getEmail().substring(0, AppSettings.getInstance().getEmail().indexOf('@'));
            email_second_part = AppSettings.getInstance().getEmail().substring(AppSettings.getInstance().getEmail().indexOf('@'));
        }
        emailField.setText(email_first_part);
        if (email_second_part.isEmpty() || email_second_part.isBlank())
            emailExtension.getSelectionModel().selectFirst();
        else
            emailExtension.setValue(email_second_part);
    }

    private void hidePassword() {
        toolTip.setText("");
        toolTip.hide();
    }

    private void showPassword() {
        toolTip.setShowDelay(Duration.ZERO);
        toolTip.setAutoHide(false);
        toolTip.setMinWidth(50);
        toolTip.setStyle("-fx-font-size: 13");
        Point2D p = passwordField.localToScene(passwordField.getBoundsInLocal().getMinX(), passwordField.getBoundsInLocal().getMinY());
        toolTip.setText(passwordField.getText());
        toolTip.show(passwordField, p.getX() + stage.getScene().getX() + stage.getX(), p.getY()+25 + stage.getScene().getY() + stage.getY());
    }

    private void save_login_creds(String username, String password){
        AppSettings.getInstance().setEmail(username);
        if (rememberMeCheckBox.isSelected())
            AppSettings.getInstance().setPassword(password);
        else
            AppSettings.getInstance().setPassword("");
        AppSettings.getInstance().setAuto_login(rememberMeCheckBox.isSelected());
        // save
        AppProperties.getInstance().save_properties();
    }

    private boolean login_credentials(){
        String username = emailField.getText() + emailExtension.getValue().toString();
        String password = passwordField.getText();

        save_login_creds(username, password);
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
        errorLabel.setVisible(false);
        if (login_credentials())
            show_email_prompt_screen();
        else
            System.out.println("Wrong credentials!");
    }

    public EmailPromptController getEmailPromptController() {
        return emailPromptController;
    }

    public void show_email_prompt_screen() {
        emailPromptController = new EmailPromptController(this.stage, emailConfig);
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/com/sudo/lohnator_5000/login/email_prompt.fxml"));
            loader.setController(emailPromptController);
            root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(AppConfiguration.PROGRAM_TITLE.getValue());
            stage.setOnCloseRequest(actionEvent -> {
                PDF_Executor.getInstance().shutdownNow();
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getStage() {
        return stage;
    }
}
