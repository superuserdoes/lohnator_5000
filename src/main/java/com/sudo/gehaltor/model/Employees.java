package com.sudo.gehaltor.model;

import com.sudo.gehaltor.data.Employee;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Employees {

    private static volatile Employees instance;
    private static ObservableList<Employee> employees = FXCollections.observableArrayList(
            employee -> new Observable[]{
                    employee.sv_numberProperty(),
                    employee.nameProperty(),
                    employee.surnameProperty(),
                    employee.emailProperty(),
                    employee.getFiles()
//                    employee.getImages()
            }
    );
    private static ObjectProperty<Employee> currentEmployee = new SimpleObjectProperty<>(null);

    private Employees() {}

    public static Employees getInstance() {
        if (instance == null){
            synchronized (Employees.class){
                if (instance == null)
                    instance = new Employees();
            }
        }
        return instance;
    }

    public static ObservableList<Employee> getEmployees() {
        return employees;
    }

    public static void setEmployees(ObservableList<Employee> employees) {
        Employees.employees = employees;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee.get();
    }

    public static ObjectProperty<Employee> currentEmployeeProperty() {
        return currentEmployee;
    }

    public static void setCurrentEmployee(Employee currentEmployee) {
        Employees.currentEmployee.set(currentEmployee);
    }





}
