package com.sudo.gehaltor.controller;

import com.sudo.gehaltor.config.AppProperties;
import com.sudo.gehaltor.data.AppSettings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class Settings {
    @FXML Slider slider_performance;
    @FXML Label label_thread_count;
    @FXML TextField textField_email;
    @FXML PasswordField passwordField;
    @FXML ToggleButton tglBtn_show_password;
    @FXML RadioButton rb_remember;
    @FXML RadioButton rb_dont_remember;
    @FXML TextField textField_advisor_name;
    @FXML TextField textField_advisor_email;
    @FXML Button btn_ok;
    @FXML Button btn_cancel;
    @FXML Button btn_apply;
    @FXML Button btn_reset_default;
    private SimpleBooleanProperty showPassword  = new SimpleBooleanProperty();
    private Tooltip toolTip;

    private Stage stage;
    private final int cores = Runtime.getRuntime().availableProcessors();
    private SimpleIntegerProperty threadCount;

    public Settings(Stage stage) {
        this.stage = stage;
    }

    public void initialize(){
        System.out.println("SETTINGS INITIALIZED!");
        buttons();
        setup_radio_buttons();
        setup_fields();
        performance_settings();
    }

    private void setup_fields() {
        toolTip = new Tooltip();

        textField_email.setText(AppSettings.getInstance().getEmail());
        passwordField.setText(AppSettings.getInstance().getPassword());
        passwordField.setOnKeyTyped(keyEvent -> {
            if ( showPassword.get() ) {
                showPassword();
            }
        });

        textField_advisor_email.setText(AppSettings.getInstance().getFinancial_advisor_email());
        textField_advisor_name.setText(AppSettings.getInstance().getFinancial_advisor_name());
    }

    private void setup_radio_buttons() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(rb_remember);
        toggleGroup.getToggles().add(rb_dont_remember);
        toggleGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            System.out.println(t1 + " selected!");
        });
        if (AppSettings.getInstance().auto_loginProperty().get())
            rb_remember.setSelected(true);
        else
            rb_dont_remember.setSelected(true);
    }

    private void buttons() {
        showPassword.bind(tglBtn_show_password.selectedProperty());
        showPassword.addListener((observable, oldValue, newValue) -> {
            if (newValue) showPassword();
            else hidePassword();
        });          
        btn_ok.setOnAction(actionEvent -> {
            threadCount.setValue(slider_performance.getValue());
            save_settings();
            stage.close();
        });
        btn_apply.setOnAction(actionEvent -> {
            threadCount.setValue(slider_performance.getValue());
            save_settings();
        });
        btn_cancel.setOnAction(actionEvent -> {
            stage.close();
        });
        btn_reset_default.setOnAction(actionEvent -> {
            slider_performance.setValue(cores >> 1);
            rb_remember.setSelected(true);
            textField_email.setText("");
            passwordField.setText("");
            textField_advisor_email.setText("");
            textField_advisor_name.setText("");
        });
    }

    private void save_settings(){
        AppSettings.getInstance().setNum_of_threads(threadCount.getValue());
        AppSettings.getInstance().setEmail(textField_email.getText());
        AppSettings.getInstance().setFinancial_advisor_email(textField_advisor_email.getText());
        AppSettings.getInstance().setFinancial_advisor_name(textField_advisor_name.getText());
        if (rb_remember.isSelected()) AppSettings.getInstance().setPassword(passwordField.getText());
        else AppSettings.getInstance().setPassword("");
        AppSettings.getInstance().setAuto_login(rb_remember.isSelected());
        // save
        AppProperties.getInstance().save_properties();
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

    private void performance_settings(){
//        threadCount = AppProperties.getInstance().num_of_threadsProperty();
        threadCount = AppSettings.getInstance().num_of_threadsProperty();


        slider_performance.setMin(1);
        slider_performance.setMax(cores);
        slider_performance.setValue(threadCount.doubleValue());

        label_thread_count.setText("(Threads: " + threadCount.intValue() + ")");
        slider_performance.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n <= 1) return "Power Saver";
                if (n == cores/2) return "Balanced";
                if (n == cores) return "Performance";
                return "";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "Power Saver":
                        return 1d;
                    case "Balanced":
                        return (double) (cores/2);
                    case "Performance":
                        return (double) (cores);
                    default:
                        return null;
                }
            }
        });


        slider_performance.valueProperty().addListener((observableValue, aBoolean, t1) -> {
           label_thread_count.textProperty().unbind();
           label_thread_count.textProperty().bind(Bindings.format("(Threads: %d)", t1.intValue()));
        });
    }

}
