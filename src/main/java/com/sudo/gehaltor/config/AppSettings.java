package com.sudo.gehaltor.config;

import com.sudo.gehaltor.pdf.utilities.PDF_File;
import com.sudo.gehaltor.services.PDF_Executor;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AppSettings {
    private SimpleStringProperty property_file_path = new SimpleStringProperty(this, "property_file_path", AppConfiguration.PROGRAMS_PATH.getValue() + PDF_File.PROPERTIES_FILE_NAME);
    private SimpleStringProperty email = new SimpleStringProperty(this, "email", "");
    private SimpleStringProperty password = new SimpleStringProperty(this,"password", "");
    private SimpleBooleanProperty auto_login = new SimpleBooleanProperty(this, "auto_login" ,true);
    private SimpleStringProperty financial_advisor_name = new SimpleStringProperty(this, "berater_name","");
    private SimpleStringProperty financial_advisor_email = new SimpleStringProperty(this, "berater_email","");
    private SimpleIntegerProperty date_sync_emails = new SimpleIntegerProperty(this, "day_of_month_sync_emails", 15);
    private SimpleIntegerProperty date_send_emails = new SimpleIntegerProperty(this, "day_of_month_send_emails", 15);
    private SimpleIntegerProperty num_of_threads = new SimpleIntegerProperty(this, "number_of_threads", Runtime.getRuntime().availableProcessors() >> 1);

    private static volatile AppSettings instance;

    private AppSettings(){}

    public static AppSettings getInstance(){
        if (instance == null){
            synchronized (AppSettings.class){
                if (instance == null){
                    instance = new AppSettings();
                    AppProperties.getInstance().load_properties();
                }
            }
        }
        return instance;
    }

    public String getProperty_file_path() {
        return property_file_path.get();
    }

    public SimpleStringProperty property_file_pathProperty() {
        return property_file_path;
    }

    public void setProperty_file_path(String property_file_path) {
        this.property_file_path.set(property_file_path);
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public boolean isAuto_login() {
        return auto_login.get();
    }

    public SimpleBooleanProperty auto_loginProperty() {
        return auto_login;
    }

    public void setAuto_login(boolean auto_login) {
        this.auto_login.set(auto_login);
    }

    public String getFinancial_advisor_name() {
        return financial_advisor_name.get();
    }

    public SimpleStringProperty financial_advisor_nameProperty() {
        return financial_advisor_name;
    }

    public void setFinancial_advisor_name(String financial_advisor_name) {
        this.financial_advisor_name.set(financial_advisor_name);
    }

    public String getFinancial_advisor_email() {
        return financial_advisor_email.get();
    }

    public SimpleStringProperty financial_advisor_emailProperty() {
        return financial_advisor_email;
    }

    public void setFinancial_advisor_email(String financial_advisor_email) {
        this.financial_advisor_email.set(financial_advisor_email);
    }

    public int getDate_sync_emails() {
        return date_sync_emails.get();
    }

    public SimpleIntegerProperty date_sync_emailsProperty() {
        return date_sync_emails;
    }

    public void setDate_sync_emails(int date_sync_emails) {
        this.date_sync_emails.set(date_sync_emails);
    }

    public int getDate_send_emails() {
        return date_send_emails.get();
    }

    public SimpleIntegerProperty date_send_emailsProperty() {
        return date_send_emails;
    }

    public void setDate_send_emails(int date_send_emails) {
        this.date_send_emails.set(date_send_emails);
    }

    public int getNum_of_threads() {
        return num_of_threads.get();
    }

    public SimpleIntegerProperty num_of_threadsProperty() {
        return num_of_threads;
    }

    public void setNum_of_threads(int num_of_threads) {
        this.num_of_threads.set(num_of_threads);
        // shutdown
        PDF_Executor.getInstance().shutdown();
    }
}
