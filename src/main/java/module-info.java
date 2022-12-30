
module com.sudo.gehaltor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires pdfbox;

    requires javafx.graphics;
    requires java.mail;
    requires tess4j;
    requires javafx.swing;
    requires javafx.web;

    opens com.sudo.gehaltor.view to javafx.fxml;
    opens com.sudo.gehaltor.email to javafx.fxml;
    opens com.sudo.gehaltor.printer to javafx.fxml;
    opens com.sudo.gehaltor.controller to javafx.fxml;
    exports com.sudo.gehaltor;
    exports com.sudo.gehaltor.services;
}