package com.sudo.lohnator_5000.data;

import com.sudo.lohnator_5000.pdf.utilities.PDF_File;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class Employee implements Person {

    private static final long serialVersionUID = 321954984569L;

    private transient LongProperty sv_number = new SimpleLongProperty(this, "sv_number",0L);
    private transient StringProperty name = new SimpleStringProperty(this, "name","");
    private transient StringProperty surname = new SimpleStringProperty(this, "surname", "");
    private transient StringProperty e_mail = new SimpleStringProperty(this, "e-mail", "");
    private transient ObservableList<File> files = FXCollections.observableArrayList();
    private transient ObservableList<String> emails = FXCollections.observableArrayList();

    public Employee(long sv_number, String name, String surname) {
        this.sv_number.set(sv_number);
        this.name.set(name);
        this.surname.set(surname);
    }

    public Employee(long sv_number, String name, String surname, String e_mail) {
        this.sv_number.set(sv_number);
        this.name.set(name);
        this.surname.set(surname);
        this.e_mail.set(e_mail);
    }

    public long getSv_number() { return sv_number.get(); }

    public LongProperty sv_numberProperty() { return sv_number; }

    public void setSv_number(long sv_number) { this.sv_number.set(sv_number); }

    public String getFullName(){ return  name.get() + " " + surname.get();}

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getSurname() {
        return surname.get();
    }

    public StringProperty surnameProperty() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname.set(surname);
    }

    public String getEmail() {
        return e_mail.get();
    }

    public StringProperty emailProperty() {
        return e_mail;
    }

    public void setEmail(String e_mail) {
        this.e_mail.set(e_mail);
    }

    public ObservableList<File> getFiles() {
        FXCollections.sort(this.files, PDF_File.getFileComparator(PDF_File.FILE_TYPE));
        return files;
    }

    public void setFiles(ObservableList<File> files) {
        this.files = files;
    }

    public void add_file(File file){
        if (!this.files.contains(file))
            this.files.add(file);
    }

    public void add_all_files(ObservableList<File> files_to_add){
        files_to_add.forEach(this::add_file);
    }

    public boolean delete_file(File file){
        if (PDF_File.delete_file(file))
            return this.files.remove(file);
        return false;
    }

    public void deleteAllFiles(){
        this.files.clear();
    }

    public void add_email(String email){
        this.emails.add(email);
    }

    public ObservableList<String> getEmails() {
        return emails;
    }

    //-----------------------------------------------------------
    //-----------------------------------------------------------

    /**
     * Need for calling by readObject;
     */
    private void initInstance() {
        sv_number = new SimpleLongProperty();
        name = new SimpleStringProperty();
        surname = new SimpleStringProperty();
        e_mail = new SimpleStringProperty();
        emails = FXCollections.observableArrayList();
        files = FXCollections.observableArrayList();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        WriteObjectsHelper.writeAllProp(s,emails,files,name,surname,e_mail, sv_number);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        initInstance();
        ReadObjectsHelper.readAllProp(s,emails,files,name,surname,e_mail, sv_number);
    }


    //-----------------------------------------------------------
    //-----------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sv_n = new StringBuilder();

        for (char c : String.valueOf(getSv_number()).toCharArray()){
            sv_n.append(c);
            if (sv_n.length() == 4){
                sv_n.append('-');
            }
        }
        return "Name: " + getName() + " " + getSurname() +
                "\nSV: " + sv_n +
                "\nE-mail: " + getEmail() +
                "\nFiles: " + files.size(); // +
//                "\nImages: " + images.size();
    }


    // TODO just check SV_number, because "unique"
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Employee employee = (Employee) o;
        return  Objects.equals(getSv_number(), employee.getSv_number()) &&
                Objects.equals(getSv_number(), employee.getSv_number());
    }

    // TODO just hash the SV-number, cuz' unique
    @Override
    public int hashCode() {
        return Objects.hash(this.sv_number);
    }

}
