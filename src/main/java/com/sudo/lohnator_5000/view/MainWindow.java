package com.sudo.lohnator_5000.view;

import com.sudo.lohnator_5000.config.AppConfiguration;
import com.sudo.lohnator_5000.controller.MWController;
import com.sudo.lohnator_5000.pdf.utilities.PDF_File;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow {
    private MWController mwController;

    public void showMainWindow(Stage stage) throws IOException {
        mwController = new MWController();
        mwController.setStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/com/sudo/lohnator_5000/main_window/mainwindow.fxml"));
        fxmlLoader.setController(mwController);
        Scene scene = new Scene(fxmlLoader.load());

        stage.getIcons().add(new Image(String.valueOf(this.getClass().getResource("/com/sudo/lohnator_5000/logo/logo.png"))));
        stage.setTitle(AppConfiguration.PROGRAM_TITLE.getValue());
        stage.setScene(scene);
        stage.setAlwaysOnTop(false);
        stage.setOnCloseRequest(windowEvent -> PDF_File.onExit());
        stage.show();

        // Center it
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
}
