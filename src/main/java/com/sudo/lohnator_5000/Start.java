package com.sudo.lohnator_5000;

import com.sudo.lohnator_5000.pdf.utilities.PDF_File;
import com.sudo.lohnator_5000.view.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Start extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Try loading save file
        if (!PDF_File.load_save_file())
            System.err.println("SAVE-FILE NOT FOUND!");
        // Load or create default properties
        PDF_File.load_default_properties();
        // Show main window
        MainWindow mainWindow = new MainWindow();
        mainWindow.showMainWindow(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
