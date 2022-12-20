package com.sudo.gehaltor;

import com.sudo.gehaltor.pdf.utilities.PDF_File;
import com.sudo.gehaltor.view.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Start extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        if (!PDF_File.load_save_file()){
            System.out.println("COULDN'T LOAD SAVEFILE!");
        }
        MainWindow mainWindow = new MainWindow(stage);
    }
 

    public static void main(String[] args) {
        launch(args);
    }
}
