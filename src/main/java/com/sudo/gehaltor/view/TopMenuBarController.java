package com.sudo.gehaltor.view;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.config.Settings;
import com.sudo.gehaltor.data.Employee;
import com.sudo.gehaltor.pdf.utilities.PDF_File;
import com.sudo.gehaltor.pdf.utilities.PDF_Processor;
import com.sudo.gehaltor.services.LoadPDFs;
import com.sudo.gehaltor.services.PDF_Executor;
import com.sudo.gehaltor.services.Stopwatch;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class TopMenuBarController {

    @FXML Pane topMenuBarPane;
    @FXML MenuBar topMenuBarID;
    @FXML Menu menu_fileID;
    @FXML MenuItem menuBarAddFile;
    @FXML MenuItem menuBarSettings;
    @FXML MenuItem menuBarClose;
    @FXML MenuItem  menuBarAbout;

    private ProgressBar progressbar_loading;
    private Label label_time;

    public void setProgressbar_loading(ProgressBar progressbar_loading, Label label_time) {
        this.progressbar_loading = progressbar_loading;
        this.label_time = label_time;
    }

    public TopMenuBarController() {
    }

    public void initialize(){

        menuBarAddFile.setOnAction(event -> {
            onAddFile(event);
        });

        menuBarSettings.setOnAction(actionEvent -> {
            onSettings();
        });

        menuBarAbout.setOnAction(actionEvent -> {
            onAbout(actionEvent);
        });

        menuBarClose.setOnAction(actionEvent -> {
            onClose();
        });

    }

    public void disableFileMenu(){
        this.menu_fileID.setDisable(true);
    }

    public void disableMenuBarAddFile(){
        this.menuBarAddFile.setDisable(true);
    }

    protected void  onAddFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(AppConfiguration.PROGRAM_TITLE.getValue() + " - Process PDF(s)");
        chooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files","*.pdf", "*.PDF"));
        List<File> files = chooser.showOpenMultipleDialog(((MenuItem) event.getTarget()).getParentPopup().getOwnerWindow());
        if (files != null) {
            ExecutorService executorService = PDF_Executor.getInstance();
            ConcurrentHashMap<Long, Employee> employeeConcurrentHashMap = new ConcurrentHashMap<>();

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();

            SimpleIntegerProperty filesDone = new SimpleIntegerProperty(0);
            SimpleDoubleProperty total_progress = new SimpleDoubleProperty(0);
            double adding_progress = 1.0/files.size();

            progressbar_loading.setVisible(true);
            progressbar_loading.progressProperty().bind(total_progress);

            // update text
            label_time.textProperty().unbind();
            label_time.textProperty().bind(
                    Bindings.concat(filesDone)
                            .concat("/")
                            .concat(files.size())
                            .concat(" file(s) done in ")
                            .concat(stopwatch.elapsedTimeProperty())
                            .concat(" ms!")
            );

            files.stream().map(PDF_Processor::new).peek(executorService::submit).forEach(pdf_processor -> {
                pdf_processor.setOnSucceeded(workerStateEvent -> {
                    // update total progress
                    total_progress.setValue(total_progress.get() + adding_progress);
                    // increment total progress
                    filesDone.setValue(filesDone.getValue() + 1);
                    // process PDF(s)
                    LoadPDFs.save_processed_PDF_files(pdf_processor, employeeConcurrentHashMap);
                });
            });
            executorService.shutdown();

            CompletableFuture.supplyAsync(() -> {
                Boolean done = false;
                while (!done){
                    if (executorService.isTerminated()){
                        done = true;
                        stopwatch.stop();
                    }
                }
                return true;
            }).thenAccept(result -> {
                Platform.runLater(() -> {
                    progressbar_loading.setVisible(false);
                    label_time.textProperty().unbind();
                    label_time.setText( filesDone.get() + " file(s) done in " + stopwatch.getElapsedTime() + " ms!");
                });
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        Platform.runLater(() -> label_time.setText(""));
                    } catch (InterruptedException e) { throw new RuntimeException(e);}
                }).start();
            });
        }

    }

    private void onSettings(){
        Stage stage = new Stage();
        Settings settings = new Settings(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/com/sudo/gehaltor/settings/settings.fxml"));
        fxmlLoader.setController(settings);
        try {
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle(AppConfiguration.PROGRAM_TITLE.getValue() + " - Settings");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(windowEvent -> PDF_File.onExit());
            // Center it
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Couldn't load login screen: " + e);
        }

    }

    private void onAbout(ActionEvent event){
        System.out.println(getClass().getResource("/com/sudo/gehaltor/topmenu/about.fxml"));

        System.out.println("About clicked!");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(((MenuItem) event.getTarget()).getParentPopup().getOwnerWindow());
        dialog.setTitle(AppConfiguration.PROGRAM_TITLE.getValue() + " - About");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/com/sudo/gehaltor/topmenu/about.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load DialogPane!");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            System.out.println("OK");
        } else {
            System.out.println("Close");
        }
    }


    private void onClose(){
        System.out.println("Close clicked!");
        System.out.println("Exit clicked!");
        PDF_File.onExit();
        //config.updateFile();
        Platform.exit();
    }
}