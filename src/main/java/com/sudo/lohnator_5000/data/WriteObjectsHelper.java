package com.sudo.lohnator_5000.data;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class WriteObjectsHelper {
    // write a StringProperty to ObjectOutputStream
    public static void writeStringProp(ObjectOutputStream s, StringProperty strProp) throws IOException {
        s.writeUTF(strProp.getValueSafe());
    }

    // write a ListProperty to ObjectOutputStream
    public static void writeListProp(ObjectOutputStream s, ListProperty lstProp) throws IOException {
        if(lstProp==null || lstProp.getValue()==null) {
            s.writeInt(0);
            return;
        }
        s.writeInt(lstProp.size());
        for(Object elt:lstProp.getValue()) s.writeObject(elt);
    }

    // write a ObservableList to ObjectOutputStream
    public static void writeObservableList(ObjectOutputStream s, ObservableList lstProp) throws IOException {
        if(lstProp==null || lstProp.isEmpty()) {
            s.writeInt(0);
            return;
        }
        s.writeInt(lstProp.size());
        for(Object elt : lstProp) s.writeObject(elt);
    }

    // automatic write set of properties to ObjectOutputStream
    public static void writeAllProp(ObjectOutputStream s, ObservableList emails, ObservableList files, Property... properties) throws IOException {
        s.defaultWriteObject();

        if (emails instanceof ObservableList) writeObservableList(s,emails);
        if (files instanceof ObservableList) writeObservableList(s,files);

        for(Property prop:properties) {
            if(prop instanceof IntegerProperty) s.writeInt(((IntegerProperty) prop).intValue());
            else if(prop instanceof LongProperty) s.writeLong(((LongProperty) prop).longValue());
            else if(prop instanceof StringProperty) s.writeUTF(((StringProperty)prop).getValueSafe());
            else if(prop instanceof BooleanProperty) s.writeBoolean(((BooleanProperty)prop).get());
            else if(prop instanceof ListProperty) writeListProp(s,(ListProperty)prop);
            else throw new RuntimeException("Object incompatible: " + prop.toString());
        }
    }
}
