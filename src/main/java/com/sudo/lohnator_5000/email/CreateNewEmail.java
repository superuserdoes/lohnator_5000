package com.sudo.lohnator_5000.email;

import com.sudo.lohnator_5000.config.AppConfiguration;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreateNewEmail {
    public CreateNewEmail(List<File> attachments) throws IOException {
        showWindow(attachments);
    }
    private void showWindow(List<File> attachments) throws IOException {
        Stage stage = new Stage();
        CreateNewEmailController createNewEmail = new CreateNewEmailController(attachments);
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/com/sudo/lohnator_5000/login/send_email.fxml"));
        fxmlLoader.setController(createNewEmail);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(AppConfiguration.PROGRAM_TITLE.getValue());
        stage.getIcons().add(new Image(String.valueOf(this.getClass().getResource("/com/sudo/lohnator_5000/logo/logo.png"))));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setAlwaysOnTop(false);
        stage.show();
        // Center it
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

}
