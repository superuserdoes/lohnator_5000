package com.sudo.gehaltor.data;

import javafx.beans.property.LongProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public interface Person extends Serializable {

    long getSv_number();
    LongProperty sv_numberProperty();
    void setSv_number(long sv_number);

    String getFullName();

    String getName();
    StringProperty nameProperty();
    void setName(String name);

    String getSurname();
    StringProperty surnameProperty();
    void setSurname(String surname);

    String getEmail();
    StringProperty emailProperty();
    void setEmail(String e_mail);

    void add_file(File file);
    void add_all_files(ObservableList<File> files_to_add);
    List<File> getFiles();
    void deleteAllFiles();

    void add_email(String email);
    ObservableList<String> getEmails();
}

