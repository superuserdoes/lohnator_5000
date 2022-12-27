package com.sudo.gehaltor;

import com.sudo.gehaltor.pdf.utilities.PDF_File;
import com.sudo.gehaltor.view.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Start extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Try loading save file
        if (!PDF_File.load_save_file())
            System.out.println("COULDN'T LOAD SAVEFILE!");
        // Load or create default properties
        PDF_File.load_default_properties();
        // Show main window
        MainWindow mainWindow = new MainWindow(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
