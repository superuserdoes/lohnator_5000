package com.sudo.gehaltor.controller;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.data.Employee;
import com.sudo.gehaltor.pdf.utilities.PDF_File;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomTreeView {

    private final String FILE_TYPE = ".pdf";
    private String EMPLOYEE_PATH;
    private ObservableList<File> downloads;
    private CheckBoxTreeItem<File> rootItem;
    private File employee_root_file;
    private ObservableSet<TreeItem<File>> checkedItems;
    private Employee employee;
    private SimpleBooleanProperty filesAdded = new SimpleBooleanProperty(false);

    public ObservableSet<TreeItem<File>> getCheckedItems() {
        return checkedItems;
    }

    public CustomTreeView(Employee employee) {
        this.employee = employee;
        employee_root_file = new File(AppConfiguration.EMPLOYEE_SAVE_PATH.getValue() + employee.getFullName());
        this.EMPLOYEE_PATH = employee_root_file.getAbsolutePath();
        this.downloads = FXCollections.observableArrayList();
        this.rootItem = new CheckBoxTreeItem<>(employee_root_file);
        this.checkedItems = FXCollections.observableSet();
        setup_listeners();
        setup_tree_view();
        setup_event_handlers();
    }

    public CheckBoxTreeItem<File> getRootItem() {
        return rootItem;
    }

    private void setup_tree_view() {
        rootItem.setExpanded(true);
        deep_search(employee_root_file);
        FXCollections.sort(downloads, PDF_File.getFileComparator(FILE_TYPE));
        downloads.forEach(this::createTreeOutOfList);
    }

    private void setup_event_handlers() {
        rootItem.addEventHandler(CheckBoxTreeItem.childrenModificationEvent(), objectTreeModificationEvent -> {
            if (objectTreeModificationEvent.wasAdded()) {
                for (TreeItem<Object> added : objectTreeModificationEvent.getAddedChildren()) {
                    addSubtree(checkedItems, (CheckBoxTreeItem) added);
                }
            }
            if (objectTreeModificationEvent.wasRemoved()) {
                for (TreeItem<Object> removed : objectTreeModificationEvent.getRemovedChildren()) {
                    removeSubtree(checkedItems, (CheckBoxTreeItem) removed);
                }
            }
        });

        // listen for selection change
        rootItem.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), (CheckBoxTreeItem.TreeModificationEvent<File> evt) -> {
            CheckBoxTreeItem<File> item = evt.getTreeItem();
            if (evt.wasIndeterminateChanged()) {
                if (item.isIndeterminate()) {
                    checkedItems.remove(item);
                }
                else if (item.isSelected() &&
                        !item.getValue().equals(rootItem.getValue()) &&
                        item.getValue().isFile()
                ) {
                    checkedItems.add(item);
                }
            } else if (evt.wasSelectionChanged()) {
                if (item.isSelected() &&
                        !item.getValue().equals(rootItem.getValue()) &&
                        item.getValue().isFile()
                ) {
                    checkedItems.add(item);
//                    btn_send_pdf.setDisable(false);
                } else {
                    checkedItems.remove(item);
//                    if (checkedItems.isEmpty()) btn_send_pdf.setDisable(true);
                }
            }
        });
    }

    private void setup_listeners() {
        employee.getFiles().addListener((ListChangeListener<? super File>) change -> {
            while (change.next()) {
                if (change.wasAdded()){
                    change.getAddedSubList().forEach(file -> {
                        createTreeOutOfList(file);
                        rootItem.getChildren().sort(Comparator.comparing(TreeItem::getValue, PDF_File.getFileComparator(PDF_File.FILE_TYPE)));
                    });
                }
            }
        });
    }

    private void deep_search(File file){
        if (!downloads.contains(file) && file.isFile() && file.getName().endsWith(this.FILE_TYPE))
            downloads.add(file);

        if (!downloads.contains(file) && file.isDirectory() && file.listFiles().length > 0){
            downloads.add(file);
            for (File listFile : Objects.requireNonNull(file.listFiles()))
                deep_search(listFile);
        }
    }

    private static <T> void addSubtree(ObservableSet<TreeItem<T>> collection, CheckBoxTreeItem<T> item) {
        if (item.isSelected()) {
            collection.add(item);
        } else if (!item.isIndeterminate() && !item.isIndependent()) {
            return;
        }
        for (TreeItem<T> child : item.getChildren()) {
            addSubtree(collection, (CheckBoxTreeItem<T>) child);
        }
    }

    private static <T> void removeSubtree(ObservableSet<TreeItem<T>> collection, CheckBoxTreeItem<T> item) {
        if (item.isSelected()) {
            collection.remove(item);
        } else if (!item.isIndeterminate() && !item.isIndependent()) {
            return;
        }
        for (TreeItem<T> child : item.getChildren()) {
            removeSubtree(collection, (CheckBoxTreeItem<T>) child);
        }
    }

    public void createTreeOutOfList(File file){
        if (file.getName().endsWith(FILE_TYPE) && file.getAbsolutePath().contains(EMPLOYEE_PATH)) {
            String existing_parent = file.getParent().substring(file.getParent().lastIndexOf("\\") + 1);

            check_parent_dir_existance(file);

            rootItem.getChildren().forEach(fileTreeItem -> {
                if (fileTreeItem.getValue().isDirectory() && fileTreeItem.getValue().getName().equalsIgnoreCase(existing_parent)) {
                    fileTreeItem.getChildren().add(new CheckBoxTreeItem<File>(file));
                }
            });
        }
    }

    private void check_parent_dir_existance(File file) {
        String existing_parent = file.getParent().substring(file.getParent().lastIndexOf("\\") + 1);
        SimpleBooleanProperty dir_exists = new SimpleBooleanProperty(false);
        File dir_file = new File(file.getParent());
        // Check if parent dir exists
        rootItem.getChildren().forEach(fileTreeItem -> {
            if (fileTreeItem.getValue().isDirectory() && fileTreeItem.getValue().getName().equals(existing_parent)){
                dir_exists.set(true);
            }
        });
        // Create non-existent folder
        if (!dir_exists.get())
            create_dir(dir_file);
    }

    private void create_dir(File file) {
        CheckBoxTreeItem<File> treeItem = new CheckBoxTreeItem<File>(file);
        treeItem.setExpanded(true);
        rootItem.getChildren().add(treeItem);
    }

}

/**
 * Utility to run the provided runnable in each scene pulse.
 */
final class PulseUtil extends AnimationTimer {

    /**
     * Specifies the count of current pulse.
     */
    private static final int CURRENT_PULSE = 0;

    /**
     * Specifies the count of the timer once it is started.
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * Specifies the order at which the runnable need to be executed.
     */
    private final int order;

    /**
     * Runnable to execute in each frame.
     */
    private final Runnable callback;

    /**
     * Constructor.
     *
     * @param aCallBack runnable to execute
     * @param aOrder    order at which the runnable need to be executed
     */
    private PulseUtil(final Runnable aCallBack, final int aOrder) {
        callback = aCallBack;
        order = aOrder;
    }

    /**
     * Executes the provided runnable at the end of the current pulse.
     *
     * @param callback runnable to execute
     */
    static void doEndOfPulse(final Runnable callback) {
        new PulseUtil(callback, CURRENT_PULSE).start();
    }

    @Override
    public void handle(final long now) {
        if (count.getAndIncrement() == order) {
            try {
                callback.run();
            } finally {
                stop();
            }
        }
    }
}
