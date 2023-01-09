
module com.sudo.gehaltor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires javafx.graphics;
    requires java.mail;
    requires tess4j;
    requires javafx.swing;
    requires javafx.web;
    requires org.apache.pdfbox;

    opens com.sudo.lohnator_5000.view to javafx.fxml;
    opens com.sudo.lohnator_5000.email to javafx.fxml;
    opens com.sudo.lohnator_5000.printer to javafx.fxml;
    opens com.sudo.lohnator_5000.controller to javafx.fxml;
    exports com.sudo.lohnator_5000;
    exports com.sudo.lohnator_5000.services;
}