package com.sudo.gehaltor.controller;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.pdf.utilities.PDF_File;
import com.sudo.gehaltor.services.PDF_Executor;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.*;
import java.util.Properties;

public class Settings {
    @FXML Slider slider_performance;
    @FXML Label label_thread_count;
    @FXML Button btn_ok;
    @FXML Button btn_cancel;
    @FXML Button btn_apply;
    @FXML Button btn_reset_default;

    private Stage stage;
    private final int cores = Runtime.getRuntime().availableProcessors();
    private SimpleIntegerProperty threadCount;

    public Settings(Stage stage) {
        this.stage = stage;
    }

    public void initialize(){
        System.out.println("SETTINGS INITIALIZED!");
        buttons();
        performance_settings();
        File file = new File(AppConfiguration.PROGRAMS_PATH.getValue() + PDF_File.PROPERTIES_FILE_NAME);
        System.out.println(file);
        if (!file.exists()) {
            System.out.println("NO EXISTO!!!!!!!!!!!!!!!!!");
            // Create first if non-existent
            save_properties();
        }
        load_saved_properties();
    }

    private void buttons() {
        btn_ok.setOnAction(actionEvent -> {
            System.out.println("OK");
            save_properties();
            stage.close();
        });
        btn_apply.setOnAction(actionEvent -> {
            System.out.println("Apply");
            save_properties();
        });
        btn_cancel.setOnAction(actionEvent -> {
            System.out.println("Cancel");
            stage.close();
        });
        btn_reset_default.setOnAction(actionEvent -> {
            System.out.println("RESET!!!");
            slider_performance.setValue(cores >> 1);
            save_properties();
        });
    }

    private void performance_settings(){
        threadCount = new SimpleIntegerProperty(cores>>1);

        slider_performance.setMin(1);
        slider_performance.setMax(cores);
        slider_performance.setValue(cores >> 1);

        label_thread_count.setText("(Threads: " + cores/2 + ")");
        slider_performance.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n <= 1) return "Power Saver";
                if (n == cores/2) return "Balanced";
                if (n == cores) return "Performance";
                return "";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "Power Saver":
                        return 1d;
                    case "Balanced":
                        return (double) (cores/2);
                    case "Performance":
                        return (double) (cores);
                    default:
                        return null;
                }
            }
        });


        slider_performance.valueProperty().addListener((observableValue, aBoolean, t1) -> {
           label_thread_count.textProperty().unbind();
           label_thread_count.textProperty().bind(Bindings.format("(Threads: %d)", t1.intValue()));
           threadCount.setValue(t1.intValue());
        });
    }

    private void load_saved_properties(){
        Properties prop = new Properties();
        File file = new File(AppConfiguration.PROGRAMS_PATH.getValue() + PDF_File.PROPERTIES_FILE_NAME);
        Integer threads = cores/2;
        try (FileInputStream fis = new FileInputStream(file)) {
            prop.load(fis);
            threads = Integer.parseInt(prop.getProperty("threadCount"));
        } catch (FileNotFoundException ex) {
            // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) { }
        threadCount.setValue(threads);
        slider_performance.setValue(threads);
    }

    private void save_properties(){
        String file_path =  AppConfiguration.PROGRAMS_PATH.getValue() + PDF_File.PROPERTIES_FILE_NAME;
        File path = new File(file_path);
        if (!path.exists())
            path.getParentFile().mkdirs();

        try (OutputStream output = new FileOutputStream(path)) {
            Properties prop = new Properties();
            // set the properties value
            prop.setProperty("threadCount", String.valueOf(threadCount.getValue()));
            // save properties to project root folder
            prop.store(output, null);
            System.out.println(prop);
        } catch (IOException io) {
            io.printStackTrace();
        }

        // shutdown so it can update number of threads
        PDF_Executor.getInstance().shutdown();
    }


}
