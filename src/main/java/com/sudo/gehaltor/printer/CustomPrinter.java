package com.sudo.gehaltor.printer;

import javafx.beans.property.SimpleStringProperty;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.swing.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class CustomPrinter {

    private SimpleStringProperty customPrinterName = new SimpleStringProperty();

    public CustomPrinter(String customPrinterName) {
        setCustomPrinterName(customPrinterName);
    }

    private void setSystemLaF(){
        try {
            String cn = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(cn); // Use the native L&F
        } catch (Exception cnf) {}
    }

    private PrintRequestAttributeSet setPrintRequestAttributeSet(File file){
        // Build a set of attributes
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(OrientationRequested.PORTRAIT);
//        attributes.add(new JobName("file_name", Locale.GERMAN));
        attributes.add(new JobName(file.getName(), Locale.GERMAN));
        attributes.add(MediaSizeName.ISO_A4);
        attributes.add(Chromaticity.MONOCHROME);
        attributes.add(new Copies(1));
        attributes.add(Sides.ONE_SIDED);
        attributes.add(PrintQuality.NORMAL);
        return attributes;
    }

    private DocPrintJob setCustomPrinter(){
        setSystemLaF();
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        DocPrintJob printJob = printServices[0].createPrintJob(); // set it to the first printer (avoiding null)
        System.err.println("ALL PRINTERS:");
        for (int i = 0; i < printServices.length; i++) {
            System.err.println("-> " + printServices[i].getName());
            if (printServices[i].getName().toLowerCase().contains(customPrinterName.get().toLowerCase())) {
                System.err.println("PRINTER FOUND: " + printServices[i].getName());
                printJob = printServices[i].createPrintJob(); // get the right/desired printer
            }
        }
        return printJob;
    }

    protected void print(File file) throws PrinterException, IOException {
        System.err.println("PRINTING: " + file.getName());
        PDDocument pdDocument = Loader.loadPDF(file);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintService(setCustomPrinter().getPrintService()); // set the right/desired printer
        job.setPageable(new PDFPageable(pdDocument));
        job.setJobName(file.getName());
        boolean ok = job.printDialog(setPrintRequestAttributeSet(file));

        System.out.println("PRINT REQUEST ATTRIBUTE SET:");
        Attribute[] attributeArray = setPrintRequestAttributeSet(file).toArray();
        for (Attribute a : attributeArray) {
            System.out.println(a.getName() + ": " + a);
        }

        if (ok)
            job.print();
    }




    public String getCustomPrinterName() {
        return customPrinterName.get();
    }

    public SimpleStringProperty customPrinterNameProperty() {
        return customPrinterName;
    }

    public void setCustomPrinterName(String customPrinterName) {
        this.customPrinterName.set(customPrinterName);
    }

}
