package com.sudo.gehaltor.services;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.data.Employee;
import com.sudo.gehaltor.email.LoginController;
import com.sudo.gehaltor.model.Employees;
import com.sudo.gehaltor.pdf.utilities.PDF_File;
import com.sudo.gehaltor.pdf.utilities.PDF_Processor;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class LoadPDFs {
    private final SimpleBooleanProperty hide_loading_pdfs;
    private final Label label_time;
    private final ProgressBar progressbar_loading;
    private final ConcurrentHashMap<Long, Employee> employeeConcurrentHashMap = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    private BorderPane borderPane;

    public LoadPDFs(SimpleBooleanProperty hide_loading_pdfs, Label label_time, ProgressBar progressbar_loading) {
        this.hide_loading_pdfs = hide_loading_pdfs;
        this.label_time = label_time;
        this.progressbar_loading = progressbar_loading;
    }

    public BorderPane load(Stage primaryStage) {
        // table to display all tasks:
        TableView<PDF_Processor> table = new TableView<>();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<PDF_Processor, File> fileColumn = new TableColumn<>("File");
        fileColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getFile()));
        fileColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            public void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(file.getName());
                    setStyle("-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        fileColumn.setMinWidth(250);
        fileColumn.setMaxWidth(500);

        TableColumn<PDF_Processor, Worker.State> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().stateProperty());
        statusColumn.setCellFactory(cellData -> new TableCell<>() {
            @Override
            protected void updateItem(Worker.State state, boolean b) {
                super.updateItem(state, b);
                if (b) {
                    setText(null);
                } else {
                    setText(state.toString());
                    setStyle("-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        statusColumn.setMinWidth(80);
        statusColumn.setMaxWidth(100);
        statusColumn.setSortType(TableColumn.SortType.DESCENDING);

        //TODO TEST
        Callback<TableColumn<PDF_Processor, Double>, TableCell<PDF_Processor, Double>> cellFactory =
                new Callback<>() {
                    public TableCell call(TableColumn<PDF_Processor, Double> p) {
                        return new TableCell<PDF_Processor, Double>() {

                            private final ProgressBar pb = new ProgressBar();
                            private final Text txt = new Text();
                            private final HBox hBox = new HBox();

                            @Override
                            public void updateItem(Double item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    if (hBox.getChildren().isEmpty()) {

                                        HBox.setHgrow(pb, Priority.ALWAYS);
                                        HBox.setHgrow(txt, Priority.ALWAYS);

                                        hBox.setMinWidth(210);
                                        hBox.setMaxWidth(Double.MAX_VALUE);

                                        hBox.getChildren().addAll(pb, txt);
                                        hBox.setAlignment(Pos.CENTER);
                                    }

                                    pb.setMinWidth(250);
                                    pb.setPrefWidth(Double.MAX_VALUE);
                                    pb.setProgress(item);
                                    pb.autosize();

                                    if (item < 0)
                                        txt.setText("");
                                    else {
                                        if (item >= 0.55)
                                            txt.setFill(Color.WHITE);
                                        else
                                            txt.setFill(Color.BLACK);
                                        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
                                        defaultFormat.setMinimumFractionDigits(1);
                                        txt.setText(defaultFormat.format(item));
                                        txt.minWidth(99);
                                        txt.prefWidth(Double.MAX_VALUE);
                                    }
                                    setGraphic(hBox);
                                    setMinWidth(205);
                                    setMaxWidth(Double.MAX_VALUE);
                                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                }
                            }
                        };
                    }
                };

        TableColumn<PDF_Processor, Double> progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellValueFactory(cellData -> cellData.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        progressColumn.setCellFactory(cellFactory);
        progressColumn.setMinWidth(200);
        progressColumn.setMaxWidth(Double.MAX_VALUE);

        TableColumn<PDF_Processor, Worker.State> resultColumn = new TableColumn<>("Result");
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().stateProperty());
        resultColumn.setCellFactory(pdf_processorStringTableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(Worker.State s, boolean b) {
                super.updateItem(s, b);
                if (b) {
                    setText(null);
                } else {
                    if (s.compareTo(Worker.State.RUNNING) == 0)
                        setText("Processing...");
                    else if (s.compareTo(Worker.State.READY) == 0)
                        setText("Waiting...");
                    else if (s.compareTo(Worker.State.SUCCEEDED) == 0)
                        setText("Done!");
                    else if (s.compareTo(Worker.State.CANCELLED) == 0)
                        setText("Cancelled!");
                    setStyle("-fx-alignment: CENTER-LEFT;");
                }
            }
        });
        resultColumn.setMinWidth(100);
        resultColumn.setMaxWidth(120);

        TableColumn<PDF_Processor, PDF_Processor> cancelColumn = new TableColumn<>("Cancel");
        cancelColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        cancelColumn.setCellFactory(col -> {
            TableCell<PDF_Processor, PDF_Processor> cell = new TableCell<>();
            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle(
                    "-fx-background-color: linear-gradient(#eb1700 0%, #eb1700 38%, #f69090 100%);\n" +
                            "-fx-text-fill: white;\n" +
                            "-fx-font-size: 12px;");
            cancelButton.setOnAction(e -> cell.getItem().cancel());
            // listener for disabling button if task is not running:
            ChangeListener<Boolean> disableListener = (obs, wasRunning, isNowRunning) -> {
                cancelButton.setDisable(! isNowRunning);
            };

            cell.itemProperty().addListener((obs, oldTask, newTask) -> {
                if (oldTask != null) {
                    oldTask.runningProperty().removeListener(disableListener);
                }
                if (newTask == null) {
                    cell.setGraphic(null);
                } else {
                    cell.setStyle("-fx-alignment: CENTER-LEFT;");
                    cell.setGraphic(cancelButton);
                    cancelButton.setDisable(! newTask.isRunning());
                    newTask.runningProperty().addListener(disableListener);
                }
            });
            return cell ;
        });
        cancelColumn.setMinWidth(70);
        cancelColumn.setMaxWidth(71);

        table.getColumns().addAll(Arrays.asList(fileColumn, statusColumn, progressColumn, resultColumn, cancelColumn));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSortOrder().add(statusColumn);

        Button cancelAllButton = new Button("Cancel All");
        cancelAllButton.setDisable(true);
        cancelAllButton.setManaged(false);
        cancelAllButton.setVisible(false);
        cancelAllButton.setStyle(
                "-fx-background-color: linear-gradient(#eb1700 0%, #eb1700 38%, #f69090 100%);\n" +
                "-fx-text-fill: white;\n" +
                "-fx-font-size: 12px;"
                );
        cancelAllButton.setOnAction(e -> {
            table.getItems().stream().filter(pdf_processor -> {
                if (pdf_processor.stateProperty().getValue().compareTo(Worker.State.READY) == 0 ||
                    pdf_processor.stateProperty().getValue().compareTo(Worker.State.RUNNING) == 0 ||
                    pdf_processor.stateProperty().getValue().compareTo(Worker.State.SCHEDULED) == 0 )
                    return pdf_processor.cancel(true);
                return pdf_processor.isCancelled();
            }).forEach(Task::cancel);
            executorService.shutdownNow();
        });

        Button emailButton = new Button("Download and process PDF(s)");
        emailButton.setStyle(
//                "-fx-background-color: linear-gradient(#ffcd00 0%, #feff00 59%, #fff400 100%);\n" +
                "-fx-background-color: linear-gradient(#32ff00 0%, #b0ff00 59%, #ffde00 100%);\n" +
//                "-fx-text-fill: white;\n" +
                "-fx-text-fill: grey;\n" +
                "-fx-font-size: 14px;"
        );




        Button newTasksButton = new Button("Process local PDF(s)");
        newTasksButton.setStyle(
                "-fx-background-color: linear-gradient(#0032ff 0%, #1ca4cd 59%, #00c4ff 100%);\n" +
                "-fx-text-fill: white;\n" +
                "-fx-font-size: 14px;"
                );

        emailButton.setOnAction(actionEvent -> {
            Stage primary_stage  = new Stage();
            LoginController loginController = new LoginController(primary_stage);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/sudo/gehaltor/login/login.fxml"));
            fxmlLoader.setController(loginController);

            try {
                Scene scene = new Scene(fxmlLoader.load());
                primary_stage.setTitle(AppConfiguration.PROGRAM_TITLE.getValue());
                primary_stage.initModality(Modality.APPLICATION_MODAL);
                primary_stage.setResizable(false);
                primary_stage.setScene(scene);
                primary_stage.showAndWait();
                primary_stage.setOnCloseRequest(windowEvent -> {
//                    PDF_Executor.getInstance().shutdownNow();
                });
                if (loginController.getEmailPromptController().isDone()){
                    File save_dir = new File(AppConfiguration.DOWNLOADS_PATH.getValue());
                    List<File> files = new ArrayList<>(List.of(Objects.requireNonNull(save_dir.listFiles())));
                    process_selected_files(table, cancelAllButton, newTasksButton, files);
                }

            } catch (Exception e){ System.out.println("Couldn't load login screen: " + e); }
        });

        final VBox box = new VBox(newTasksButton, emailButton);
        box.setSpacing(20);

        table.setPlaceholder(box);
        table.getPlaceholder().setStyle("-fx-alignment: center;");

        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(Paths.get(System.getProperty("user.home")).toFile());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files","*.pdf", "*.PDF"));

        newTasksButton.setOnAction(e -> {
            List<File> files = chooser.showOpenMultipleDialog(primaryStage);
            process_selected_files(table, cancelAllButton, newTasksButton, files);
            primaryStage.setOnCloseRequest(windowEvent ->  PDF_File.onExit());
        });
        HBox controls = new HBox(10, cancelAllButton);
        HBox.setHgrow(controls, Priority.ALWAYS);
        controls.setAlignment(Pos.CENTER_RIGHT);
        controls.setPadding(new Insets(13));

        return new BorderPane(table, null, null, controls, null);
    }

    private void process_selected_files(TableView<PDF_Processor> table, Button cancelAllButton, Button newTasksButton, List<File> files) {
        executorService = PDF_Executor.getInstance();
        if (files != null) {
            Platform.runLater(() -> {
                newTasksButton.setDisable(true);
                newTasksButton.setManaged(false);
                newTasksButton.setVisible(false);

                cancelAllButton.setDisable(false);
                cancelAllButton.setManaged(true);
                cancelAllButton.setVisible(true);
            });

            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();

            HashSet<Integer> selectedRows = new HashSet<>();

            SimpleIntegerProperty filesDone = new SimpleIntegerProperty(0);
            SimpleDoubleProperty total_progress = new SimpleDoubleProperty(0);
            double adding_progress = 1.0/ files.size();

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
                table.getItems().add(pdf_processor);

                pdf_processor.stateProperty().addListener((observableValue, number, t1) -> {
                    Integer index = table.getItems().indexOf(pdf_processor);
                    if (t1.compareTo(Worker.State.RUNNING) == 0){
                        selectedRows.add(index);
                        for (Integer i : selectedRows)
                            table.getSelectionModel().select(i);
                    } else if (t1.compareTo(Worker.State.SUCCEEDED) == 0){
                        selectedRows.remove(index);
                        table.getSelectionModel().clearSelection();
                    }
                });

                pdf_processor.setOnSucceeded(workerStateEvent -> {
                    // update total progress
                    total_progress.setValue(total_progress.get() + adding_progress);
                    // increment total progress
                    filesDone.setValue(filesDone.getValue() + 1);

//                        table.sort();
//                        table.scrollTo(pdf_processor);

                    table.getSelectionModel().clearSelection();

                    save_processed_PDF_files(pdf_processor, employeeConcurrentHashMap);

                    hide_loading_pdfs.set(table.getItems().isEmpty());
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
                    cancelAllButton.setDisable(true);
                    hide_loading_pdfs.set(result);

                    progressbar_loading.setVisible(false);
                    label_time.textProperty().unbind();
                    label_time.setText( filesDone.get() + " file(s) done in " + stopwatch.getElapsedTime() + " ms!");
                });
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        Platform.runLater(() -> label_time.setText(""));
                    } catch (InterruptedException ex) { throw new RuntimeException(ex);}
                }).start();
            });
        }
    }

    public static void save_processed_PDF_files(PDF_Processor pdf_processor, ConcurrentHashMap<Long, Employee> employeeConcurrentHashMap) {
        employeeConcurrentHashMap.putAll(PDF_File.merge_maps(employeeConcurrentHashMap, pdf_processor.getEmployees_found_in_PDF()));

        ObservableList<Employee> employees = Employees.getEmployees();
        if (!employees.isEmpty()) {
            PDF_File.merge_files(employees, employeeConcurrentHashMap);
        } else {
            employees.setAll(employeeConcurrentHashMap.values());
        }
        PDF_File.auto_save();
    }

}
