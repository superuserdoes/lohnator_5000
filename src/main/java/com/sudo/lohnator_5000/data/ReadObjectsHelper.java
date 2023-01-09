package com.sudo.lohnator_5000.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ReadObjectsHelper {
    // Read a ListProperty from ObjectInputStream (and return it)
    public static ListProperty readListProp(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ListProperty lst=new SimpleListProperty(FXCollections.observableArrayList());
        int loop=s.readInt();
        for(int i = 0;i<loop;i++) {
            lst.add(s.readObject());
        }

        return lst;
    }

    // Read a ObservableList from ObjectInputStream (and return it)
    public static ObservableList readObservableStringList(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObservableList<String> stringObservableList = FXCollections.observableArrayList();
        int loop=s.readInt();
        for(int i = 0;i<loop;i++) {
            stringObservableList.add(s.readObject().toString());
        }
        return stringObservableList;
    }

    // Read a ObservableList from ObjectInputStream (and return it)
    public static ObservableList readObservableFileList(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObservableList<File> fileObservableList = FXCollections.observableArrayList();
        int loop=s.readInt();
        for(int i = 0;i<loop;i++) {
            fileObservableList.add((File) s.readObject());
        }
        return fileObservableList;
    }

    // automatic fill a set of properties with values contained in ObjectInputStream
    public static void readAllProp(ObjectInputStream  s, ObservableList emails, ObservableList files, Property... properties) throws IOException, ClassNotFoundException {

        if (emails instanceof ObservableList){
            emails.setAll(readObservableStringList(s));
        }
        if (files instanceof ObservableList){
            files.setAll(readObservableFileList(s));
        }
        for(Property prop:properties) {
            if(prop instanceof IntegerProperty) ((IntegerProperty)prop).setValue(s.readInt());
            else if(prop instanceof LongProperty) ((LongProperty)prop).setValue(s.readLong());
            else if(prop instanceof StringProperty) ((StringProperty)prop).setValue(s.readUTF());
            else if(prop instanceof BooleanProperty) ((BooleanProperty)prop).setValue(s.readBoolean());
            else if(prop instanceof ListProperty) ((ListProperty)prop).setValue(readListProp(s));
            else if(prop instanceof ObjectProperty) ((ObjectProperty)prop).setValue(s.readObject());
            else throw new RuntimeException("Unsupported object type : " + prop==null?null:prop.toString());
        }
    }
}
