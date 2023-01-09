package com.sudo.lohnator_5000.config;

import com.sudo.lohnator_5000.data.AppSettings;
import com.sudo.lohnator_5000.security.Encryptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppProperties {
    private static Properties properties;
    private static volatile AppProperties instance;

    private AppProperties() {}

    public static AppProperties getInstance() {
        if (instance == null){
            synchronized (AppProperties.class){
                if (instance == null){
                    instance = new AppProperties();
                }
            }
        }
        return instance;
    }

    public void save_properties(){
        try {
            OutputStream outputStream = new FileOutputStream(AppSettings.getInstance().getProperty_file_path());
            properties = new Properties();
            properties.setProperty(AppSettings.getInstance().num_of_threadsProperty().getName(), AppSettings.getInstance().num_of_threadsProperty().getValue().toString());
            properties.setProperty(AppSettings.getInstance().date_sync_emailsProperty().getName(), AppSettings.getInstance().date_sync_emailsProperty().getValue().toString());
            properties.setProperty(AppSettings.getInstance().date_send_emailsProperty().getName(), AppSettings.getInstance().date_send_emailsProperty().getValue().toString());
            properties.setProperty(AppSettings.getInstance().auto_loginProperty().getName(), AppSettings.getInstance().auto_loginProperty().getValue().toString());
            properties.setProperty(AppSettings.getInstance().property_file_pathProperty().getName(), AppSettings.getInstance().getProperty_file_path());
            properties.setProperty(AppSettings.getInstance().financial_advisor_nameProperty().getName(), AppSettings.getInstance().getFinancial_advisor_name());
            properties.setProperty(AppSettings.getInstance().financial_advisor_emailProperty().getName(), AppSettings.getInstance().getFinancial_advisor_email());
            properties.setProperty(AppSettings.getInstance().emailProperty().getName(), AppSettings.getInstance().emailProperty().getValue());
            properties.setProperty(AppSettings.getInstance().encryption_passwordProperty().getName(),
                                    AppSettings.getInstance().getEncryption_password());
            properties.setProperty(AppSettings.getInstance().passwordProperty().getName(),
                                    Encryptor.encrypt(AppSettings.getInstance().passwordProperty().getValue().getBytes(StandardCharsets.UTF_8),
                                    AppSettings.getInstance().getEncryption_password()));

            properties.store(outputStream, null);
            outputStream.close();
        } catch (Exception e) { /*Do nothing*/ }
    }

    public void load_properties(){
        try {
            FileInputStream fileInputStream = new FileInputStream(AppSettings.getInstance().getProperty_file_path());
            properties = new Properties();
            properties.load(fileInputStream);
            AppSettings.getInstance().setNum_of_threads(Integer.parseInt(properties.getProperty(AppSettings.getInstance().num_of_threadsProperty().getName())));
            AppSettings.getInstance().setAuto_login(Boolean.parseBoolean(properties.getProperty(AppSettings.getInstance().auto_loginProperty().getName())));
            AppSettings.getInstance().setProperty_file_path(properties.getProperty(AppSettings.getInstance().property_file_pathProperty().getName()));
            AppSettings.getInstance().setDate_send_emails(Integer.parseInt(properties.getProperty(AppSettings.getInstance().date_send_emailsProperty().getName())));
            AppSettings.getInstance().setDate_sync_emails(Integer.parseInt(properties.getProperty(AppSettings.getInstance().date_sync_emailsProperty().getName())));
            AppSettings.getInstance().setFinancial_advisor_email(properties.getProperty(AppSettings.getInstance().financial_advisor_emailProperty().getName()));
            AppSettings.getInstance().setFinancial_advisor_name(properties.getProperty(AppSettings.getInstance().financial_advisor_nameProperty().getName()));
            AppSettings.getInstance().setEmail(properties.getProperty(AppSettings.getInstance().emailProperty().getName()));
            AppSettings.getInstance().setEncryption_password(properties.getProperty(AppSettings.getInstance().encryption_passwordProperty().getName()));
            AppSettings.getInstance().setPassword(Encryptor.decrypt(properties.getProperty(AppSettings.getInstance().passwordProperty().getName()), AppSettings.getInstance().getEncryption_password()));
        } catch (Exception e) { /*Do nothing*/ }
    }






}
