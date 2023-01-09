package com.sudo.lohnator_5000.controller;

import com.sudo.lohnator_5000.config.AppConfiguration;
import com.sudo.lohnator_5000.data.Employee;
import com.sudo.lohnator_5000.email.CreateNewEmail;
import com.sudo.lohnator_5000.model.Employees;
import com.sudo.lohnator_5000.pdf.utilities.PDF_File;
import com.sudo.lohnator_5000.printer.CustomPrinterSettings;
import com.sudo.lohnator_5000.services.LoadPDFs;
import com.sudo.lohnator_5000.services.PDF_Executor;
import com.sudo.lohnator_5000.view.TopMenuBarController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MWController {

    private Map<Integer, File> fileTreeViewMap;
    private ObservableSet<TreeItem<File>> checkedItems;
    private final SimpleBooleanProperty hide_loading_pdfs = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty tree_view_cell_height = new SimpleIntegerProperty(25);
    private final int deductible_list_view_height = 46;
    @FXML private TopMenuBarController menuBarController;
    @FXML private BorderPane mainWindowPane;
    //--------------------------------
    @FXML private ScrollPane scroll_pane_left;
    @FXML private ScrollPane sp_files;
    @FXML private VBox vbox_left;
    @FXML private VBox vbox_center;
    @FXML private ImageView PDFImageView;
    @FXML private Pagination PDFImageViewPagination;
    //--------------------------------
    @FXML private TitledPane tp_employees;
    @FXML private ListView<Employee> lv_employees;
    @FXML private TitledPane tp_files;
    @FXML private TreeView<File> tv_files;
    @FXML private Button btn_send_pdf;
    @FXML private Button btn_print_pdf;
    @FXML private HBox hbox_btns;
    //--------------------------------
    @FXML private AnchorPane ac_pane_employee;
    @FXML private TextField employee_svn;
    @FXML private TextField employee_name;
    @FXML private TextField employee_surname;
    @FXML private TextField employee_email;
    @FXML private ToggleButton btn_employee_edit;
    @FXML private Button btn_employee_update;
    //--------------------------------
    @FXML private Label label_time;
    @FXML private ProgressBar progressbar_loading;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initialize(){
        progressbar_loading.setVisible(false);
        menuBarController.setProgressbar_loading(progressbar_loading, label_time);

        setup_lv_listeners();
        setup_tv_listeners();
        setup_lv_employees();
        apply_css();

        btn_sendPDF();
        btn_printPDF();
        default_empty();
    }
 
    private void default_empty() {
        if (Employees.getEmployees().isEmpty() || Employees.getCurrentEmployee() == null){
            tp_files.setExpanded(false);
            mainWindowPane.getRight().setManaged(false);
            mainWindowPane.getRight().setVisible(false);
        }
    }

    private void apply_css() {
        mainWindowPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/sudo/lohnator_5000/css/styles.css")).toString());
    }

    private void hide_loading_border_pane(){
        try {
            // loading screen
            Stage stage = new Stage();
//            VBox old_temp = (VBox) mainWindowPane.getCenter();
            VBox old_temp = (VBox) mainWindowPane.getCenter();


            mainWindowPane.setCenter(null);
            mainWindowPane.setCenter(show_loading_pdfs(stage));

            hide_loading_pdfs.addListener((observableValue, aBoolean, t1) -> {
                if (t1){
                    load_employees();
                    mainWindowPane.setCenter(null);
                    mainWindowPane.setCenter(old_temp);
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void load_employees() {
        if (!Employees.getEmployees().isEmpty()){
            lv_employees.setItems(Employees.getEmployees());
            tp_files.setExpanded(false); // workaround for first run, otherwise tree view has wrong height
        } else {
            hide_loading_border_pane();
        }
        // for first run, select first employee
        lv_employees.getSelectionModel().selectFirst();
    }

    private void setup_lv_listeners(){
        tp_employees.setOnScroll(scrollEvent -> {
            tp_files.expandedProperty().unbind();
            tp_files.setExpanded(false);
        });
        tp_employees.expandedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1){
                if (tp_files.isExpanded()) {
                    tv_files.prefHeightProperty().bind(tv_files.expandedItemCountProperty().multiply(tree_view_cell_height));
                    // make it expand to the max height
                    lv_employees.prefHeightProperty().bind(mainWindowPane.heightProperty().subtract((tp_files.heightProperty()).add(hbox_btns.heightProperty()).add(deductible_list_view_height)));
                }
            } else {
                if (tp_files.isExpanded()){
                    tv_files.prefHeightProperty().bind(mainWindowPane.heightProperty().subtract((tp_employees.heightProperty()).add(hbox_btns.heightProperty()).add(deductible_list_view_height)));
//                    tv_files.prefHeightProperty().bind(tv_files.expandedItemCountProperty().multiply(tree_view_cell_height));
                }
            }
        });


        // new click (selected new employee)
        lv_employees.getSelectionModel().selectedItemProperty().addListener((observableValue, old_employee, new_employee) -> {
                    // new employee selected
                    if (new_employee != null){
                        Employees.setCurrentEmployee(new_employee);
                        hide_buttons(false);
                        setup_tv_files();
                        setup_employee_info();
                        ac_pane_employee.setVisible(false);
                    } else {
                        tp_files.setExpanded(false);
                    }
                });
        // scroll to selected employee when listview resizes due to other bindings
        lv_employees.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        lv_employees.prefHeightProperty().bind(mainWindowPane.heightProperty().subtract((tp_files.heightProperty()).add(hbox_btns.heightProperty()).add(deductible_list_view_height)));
    }
    private void setup_tv_listeners(){
        tv_files.getSelectionModel().selectedItemProperty().addListener(observable -> tv_files.scrollTo(tv_files.getSelectionModel().getSelectedIndex()));
        tp_files.expandedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1){
                if (tp_employees.isExpanded()){
                    tv_files.prefHeightProperty().bind(tv_files.expandedItemCountProperty().multiply(tree_view_cell_height));
                    // enforce "half-half" view
                    sp_files.minHeightProperty().bind(scroll_pane_left.heightProperty().multiply(0.67));
                } else {
                    tv_files.prefHeightProperty().bind(mainWindowPane.heightProperty().subtract((tp_employees.heightProperty()).add(hbox_btns.heightProperty()).add(deductible_list_view_height)));
                }
            } else {
                if (tp_employees.isExpanded()){
                    lv_employees.prefHeightProperty().bind(mainWindowPane.heightProperty().subtract((tp_files.heightProperty()).add(hbox_btns.heightProperty()).add(deductible_list_view_height)));
                }
            }
        });
    }
    private void setup_tv_files(){
        tv_files.getSelectionModel().selectedItemProperty().addListener(observable -> tv_files.scrollTo(tv_files.getSelectionModel().getSelectedIndex()));
        tp_files.setExpanded(true);
        tv_files.setCellFactory(treeView -> {
            CheckBoxTreeCell<File> checkBoxTreeCell = new CheckBoxTreeCell<>() {
                @Override
                public void updateItem(File file, boolean empty) {
                    super.updateItem(file, empty);
                    if (empty || file == null) {
                        setGraphic(null);
                        setText(null);
                        setTooltip(null);
                        setContextMenu(null);
                    } else {
                        setText(file.getName());
                        final Tooltip tooltip = new Tooltip(file.getName() + "\nRight click for more!");
                        tooltip.setStyle("-fx-font-size: 13");
                        setTooltip(tooltip);
                        if (file.getName().endsWith(PDF_File.FILE_TYPE))
                            setContextMenu(setup_contex_menu_tv_files(file));
                    }
                }
            };
            checkBoxTreeCell.prefWidthProperty().bind(tv_files.widthProperty().subtract(ac_pane_employee.widthProperty()).subtract(15));
            return checkBoxTreeCell;
        });

        setup_tree_view();
    }
    private void setup_lv_employees(){
        // for wrapping text
        lv_employees.setCellFactory(treeView -> {
            ListCell<Employee> employeeListCell = new ListCell<>() {
                @Override
                protected void updateItem(Employee employee, boolean b) {
                    super.updateItem(employee, b);
                    if (employee != null && !b) {
                        setText(employee.toString());
                        setContextMenu(setup_contex_menu_lv_employees());
                    } else setText("");
                }
            };
            employeeListCell.prefWidthProperty().bind(lv_employees.widthProperty().subtract(ac_pane_employee.widthProperty()).subtract(15));   // hide horizontal scrollbar
            return employeeListCell;
        });
        // scroll to selected employee when listview resizes due to other bindings
        lv_employees.heightProperty().addListener(observable -> lv_employees.scrollTo(lv_employees.getSelectionModel().getSelectedIndex()));
        load_employees();
    }
    private ContextMenu setup_contex_menu_lv_employees() {
        ac_pane_employee.setVisible(false);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Update info");
        menuItem.setOnAction(event -> ac_pane_employee.setVisible(true));
        contextMenu.getItems().add(menuItem);
        return contextMenu;
    }
    private ContextMenu setup_contex_menu_tv_files(File file){
        // create a menu
        ContextMenu contextMenu = new ContextMenu();
        // create menuitems
        MenuItem menuItemShow = new MenuItem("Show in Explorer");
        menuItemShow.setOnAction(event -> {
            try { Runtime.getRuntime().exec("explorer /select, " + file.getAbsolutePath().replaceAll("/", "\\\\")); }
            catch (Exception e) { e.printStackTrace(); }
        });
        MenuItem menuItemPrint = new MenuItem("Print");
        menuItemPrint.setOnAction(event -> {
            CustomPrinterSettings customPrinterSettings = new CustomPrinterSettings(file);
        });
        MenuItem menuItemDelete = new MenuItem("Delete");
        menuItemDelete.setOnAction(event -> {
            if (Employees.currentEmployeeProperty().get().delete_file(file)){
                try {
                    TreeItem<File> selectedItem = tv_files.getSelectionModel().getSelectedItem();
                    TreeItem<File> parentOfSelectedItem = selectedItem.getParent();
                    // remove tree item
                    if(parentOfSelectedItem.getChildren().remove(selectedItem)){
                        // check if parent is empty
                        if (parentOfSelectedItem.getChildren().isEmpty()){
                            //  delete parent too
                            PDF_File.delete_file(parentOfSelectedItem.getValue());
                            parentOfSelectedItem.getParent().getChildren().remove(parentOfSelectedItem);
                        }
                        // reload the file tree view map
                        loadFileTreeViewMap();
                        // save changes
                        PDF_File.auto_save();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        // add menu items to menu
        contextMenu.getItems().add(menuItemShow);
        contextMenu.getItems().add(menuItemPrint);
        contextMenu.getItems().add(menuItemDelete);
        return contextMenu;
    }
    private void setup_employee_info() {
        edit_person(true);
        ac_pane_employee.managedProperty().bind(ac_pane_employee.visibleProperty());

        employee_svn.setText(String.valueOf(Employees.getCurrentEmployee().getSv_number()));
        employee_name.setText(Employees.getCurrentEmployee().getName());
        employee_surname.setText(Employees.getCurrentEmployee().getSurname());
        employee_email.setText(Employees.getCurrentEmployee().getEmail());
        employee_email.prefWidthProperty().bind(Employees.getCurrentEmployee().emailProperty().length() .multiply(7));

        btn_employee_edit.selectedProperty().addListener((observableValue, aBoolean, editable) -> edit_person(!editable));

        btn_employee_update.setOnAction(event -> {
            update_current_employee();
            edit_person(true);
            event.consume();
        });
    }
    private void update_current_employee() {
        Employee employee = Employees.getCurrentEmployee();
        employee.setName(employee_name.getText());
        employee.setSurname(employee_surname.getText());
        employee.setEmail(employee_email.getText());
        PDF_File.auto_save();
    }
    private void edit_person(Boolean disabled){
        employee_name.setDisable(disabled);
        employee_surname.setDisable(disabled);
        employee_email.setDisable(disabled);

        btn_employee_edit.setSelected(!disabled);
        btn_employee_update.disableProperty().bind(btn_employee_edit.selectedProperty().not());
    }
    private void hide_buttons(boolean visible){
        btn_send_pdf.visibleProperty().unbind();
        btn_print_pdf.visibleProperty().unbind();
        btn_send_pdf.setVisible(visible);
        btn_print_pdf.setVisible(visible);
    }
    private void setup_tree_view(){
//        setup_pagination();
        CustomTreeView customTreeView = new CustomTreeView(Employees.getCurrentEmployee());
        checkedItems = customTreeView.getCheckedItems();
        checkedItems.addListener((SetChangeListener<? super TreeItem<File>>) change -> {
            if (checkedItems.isEmpty())
                hide_buttons(false);
             else
                hide_buttons(true);
        });

        tv_files.setRoot(customTreeView.getRootItem());
        tv_files.setShowRoot(true);

        loadFileTreeViewMap();
        setup_pagination();

        tv_files.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> {
            if (t1 != null){
                File file = t1.getValue();

                if (file.isDirectory() && file.exists()){
                    load_image(-1); // show "no data" image
                }
                else if (file.isFile() && file.exists()) {
                    int file_index = Employees.getCurrentEmployee().getFiles().indexOf(file);
                    if (PDFImageViewPagination.getCurrentPageIndex() == file_index)
                        load_image(file_index);  // reload the image
                    PDFImageViewPagination.setCurrentPageIndex(file_index);
                }
            }
        });

    }
    private void setup_pagination(){
        SimpleIntegerProperty files_count = new SimpleIntegerProperty(Employees.getCurrentEmployee().getFiles().size());

        if (files_count.intValue() == 0) tp_files.setExpanded(false);

        Employees.getCurrentEmployee().getFiles().addListener((ListChangeListener<? super File>) change -> {
            while (change.next()){
                if (change.wasRemoved() && change.getList().isEmpty()) tp_files.setExpanded(false);
            }
        });

        PDFImageViewPagination.pageCountProperty().bind( Bindings
                                                        .when(files_count.isEqualTo(0))
                                                        .then(1)
                                                        .otherwise(files_count));
        PDFImageViewPagination.disableProperty().bind(files_count.isEqualTo(0));
        PDFImageViewPagination.setMaxPageIndicatorCount(12);
        PDFImageViewPagination.setCache(true);
        PDFImageViewPagination.setPageFactory(this::load_image);
    }

    private void loadFileTreeViewMap(){
        // Selecting corresponding file in tree view
        fileTreeViewMap = new HashMap<>();
        // Get all tree items
        TreeIterator<File> iterator = new TreeIterator<>(tv_files.getRoot());
        iterator.setMap(fileTreeViewMap);
    }

    private void selectFileTreeIndex(File selected_file){
        // Select corresponding file in tree view
        fileTreeViewMap.forEach((key, file) -> {
            if (file.getName().equals(selected_file.getName()))
                tv_files.getSelectionModel().select(key);
        });
    }

    private ImageView load_image(Integer integer) {
        try {
            File selected_file = null;
            boolean empty = Employees.getCurrentEmployee().getFiles().isEmpty();

            if (empty) tv_files.getSelectionModel().selectFirst();
            if (integer >= 0 && !empty){
                selected_file = Employees.getCurrentEmployee().getFiles().get(integer);
                // Selecting corresponding file in tree view
                selectFileTreeIndex(selected_file);
            }

            File finalSelected_file = selected_file;
            Task<BufferedImage> image_task = new Task<>() {
                @Override
                protected BufferedImage call() {
                    if (integer < 0 || empty)
                        return PDF_File.create_noData_image(AppConfiguration.DUMMY_IMAGE_TEXT.getValue());
                    return PDF_File.create_image_for_pagination(finalSelected_file);
                }
            };
            // Display it on finish
            image_task.setOnSucceeded(workerStateEvent -> PDFImageView.setImage(SwingFXUtils.toFXImage(image_task.getValue(), null)) );
            // Load image in separate (new) thread
            PDF_Executor.getInstance().submit(image_task);
            PDFImageView.setPreserveRatio(true);
            PDFImageView.setSmooth(true);
            PDFImageView.setCache(true);
            PDFImageView.fitWidthProperty().bind(vbox_center.widthProperty());
            PDFImageView.fitHeightProperty().bind(vbox_center.heightProperty().subtract(44)); // make pagination fully visible
            return PDFImageView;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void btn_sendPDF(){
        btn_send_pdf.setOnAction(event -> {
            try {
                if (!checkedItems.isEmpty()) {
                    List<File> files = new ArrayList<>();
                    checkedItems.forEach(fileTreeItem -> files.add(fileTreeItem.getValue()));
                    CreateNewEmail newEmail = new CreateNewEmail(files);
                }
            } catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    private void btn_printPDF(){
        btn_print_pdf.setOnAction(actionEvent -> {
            if (!checkedItems.isEmpty()) {
                List<File> files = new ArrayList<>();
                checkedItems.forEach(fileTreeItem -> files.add(fileTreeItem.getValue()));
                CustomPrinterSettings printerSettings = new CustomPrinterSettings(files);
            }
        });
    }

    public BorderPane show_loading_pdfs(Stage primaryStage) {
        LoadPDFs loadPDFs = new LoadPDFs(this.hide_loading_pdfs, this.label_time, this.progressbar_loading);
        return loadPDFs.load(primaryStage);
    }

}



class TreeIterator<T> implements Iterator<TreeItem<T>> {
    private final Queue<TreeItem<T>> queue = new LinkedList<>();

    public TreeIterator(TreeItem<T> root) {
        queue.add(root);
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public TreeItem<T> next() {
//        TreeItem<T> nextItem = queue.remove();
        TreeItem<T> nextItem = queue.poll();

        Objects.requireNonNull(nextItem).getChildren().forEach(tTreeItem -> {
            if (!queue.contains(tTreeItem)){
                queue.add(tTreeItem);
                queue.addAll(tTreeItem.getChildren());
            }
        });
        return nextItem;
    }

    protected void setMap(Map<Integer, T> map){
        int index = 0;
        while (!queue.isEmpty()){
            map.put(index, next().getValue());
            index++;
        }
    }

    protected void setMap(Map<Integer, T> map, TreeIterator<T> iterator){
        int index = 0;
        while (iterator.hasNext()){
            map.put(index, iterator.next().getValue());
            index++;
        }
    }
}

