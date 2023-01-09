package com.sudo.lohnator_5000.email;

import com.sudo.lohnator_5000.config.AppConfiguration;

import javax.mail.*;
import java.util.Properties;

public class EmailConfig {
    private final String host_outlook = AppConfiguration.EMAIL_HOST_OUTLOOK.getValue();
    private final String host_gmail = AppConfiguration.EMAIL_HOST_GMAIL.getValue();
    private Properties properties;
    private Session session;
    private Store store;
    private String user = null;

    public String getUser() {
        return user;
    }

    private String password = null;
    private String host = AppConfiguration.EMAIL_HOST_OUTLOOK.getValue();
    private String server_protocol = AppConfiguration.SERVER_PROTOCOL.getValue();


    public EmailConfig() throws MessagingException {
        setup();
    }

    public EmailConfig(String user, String password) throws MessagingException {
        this.user = user;
        this.password = password;
        setup();
    }

    private void setup() throws MessagingException {
        properties = get_server_properties(host);
        session = get_session();
        store = get_store();
    }

    // Returns a Properties object which is configured for a POP3/IMAP server
    public Properties get_server_properties(String host) {
        System.out.println("ENTERED: " + host);
        String host_selected = null;
        if (host.contains("outlook"))
            host_selected = host_outlook;
        else if (host.contains("gmail"))
            host_selected = host_gmail;

        System.out.println("HOST SELECTED: " + host_selected);
        // Get a Properties object
        properties = new Properties();
        // server settings
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host_selected);
        properties.put("mail.smtp.port", "587");
        // For faster download speeds of attachments
        properties.setProperty("mail.imaps.partialfetch","false");
        // SSL setting
        properties.setProperty("mail.imaps.ssl.enable", "true");
        return properties;
    }


    // Have to close session after usage
    public Session get_session(){
        session = Session.getInstance(properties,
                new javax.mail.Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);// Specify the Username and the PassWord
                    }
                });
//        session = Session.getDefaultInstance(properties);
        return session;
    }

    public Store get_store() throws MessagingException {
        //create the POP3 store object and connect with the pop server
        store = session.getStore(server_protocol);
        if (server_protocol != null)
            store = session.getStore(server_protocol);
        else
            store = session.getStore();
        // Connect
        if (host != null || user != null || password != null)
            store.connect(host, user, password);
        else
            store.connect();
        return store;
    }


}

