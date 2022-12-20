package com.sudo.gehaltor.view;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.controller.MWController;
import com.sudo.gehaltor.pdf.utilities.PDF_File;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow {

    private Stage stage;
    private MWController mwController;

    public MainWindow(Stage stage) throws IOException {
        this.stage = stage;
        showMainWindow();
    }

    public void showMainWindow() throws IOException {
        mwController = new MWController();
        mwController.setStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/com/sudo/gehaltor/main_window/mainwindow.fxml"));
        fxmlLoader.setController(mwController);
        Scene scene = new Scene(fxmlLoader.load());
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
