package com.sudo.gehaltor.pdf.utilities;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.data.Employee;
import com.sudo.gehaltor.model.Employees;
import javafx.collections.ObservableList;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PDF_File {

    int DPI = 125;
    String FILE_TYPE = ".pdf";
    String PROPERTIES_FILE_NAME = "config.properties";


    static void auto_save(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(AppConfiguration.EMPLOYEE_SAVEFILE.getValue()))) {
            oos.writeObject(new ArrayList<>(Employees.getEmployees()));
            System.out.println("\n****************************************** AUTO-SAVE ******************************************************\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void onExit(){
        File temp = new File(AppConfiguration.TEMP_PATH.getValue());
        System.out.println("Trying to delete: " + temp);
        delete_directory(temp);
    }

    static boolean load_save_file(){
        File employee_save_file = new File(AppConfiguration.EMPLOYEE_SAVEFILE.getValue());
        if (!employee_save_file.exists()){
            System.out.println("Yooooooooooooooo, save file does NOT exist!");
            return false;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(employee_save_file))){
            List<Employee> loadedEmployees = (List<Employee>) in.readObject();
            Employees.getEmployees().setAll(loadedEmployees);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(AppConfiguration.EMPLOYEE_SAVEFILE.getValue() + " could NOT be found!");
        }
        return false;
    }

    static boolean create_directory(File dir) {
        return dir.mkdirs();
    }

    static boolean delete_directory(File dir) {
        if (!dir.exists() || !dir.isDirectory())
            return false;

        String[] files = dir.list();
        for (String file : Objects.requireNonNull(files)) {
            File f = new File(dir, file);
            if (f.isDirectory()) {
                delete_directory(f);
            } else {
                f.delete();
            }
        }
        return dir.delete();
    }

    static File save_PDDocument_as_PDF(File file, PDDocument pdDocument) {
        try {
            if (!file.exists())
                file.getParentFile().mkdirs();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            pdDocument.save(bufferedOutputStream);
            bufferedOutputStream.close();
            return file;
        } catch (IOException e) {
            System.err.println("COULDN'T SAVE PD_DOCUMENT AS A PDF FILE!");
            return null;
        }
    }

    static boolean delete_file(File file) {
        try { return file.delete(); }
        catch (Exception e) { System.err.println("COULDN'T DELETE " + file.getName() + " @ {" + file.getPath() + "}"); }
        return false;
    }

    static File convert_PDDocument_to_image(PDDocument pdDocument, int page_index) {
        try {
            PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
            File temp_dir = new File(AppConfiguration.TEMP_PATH.getValue());
            if (!temp_dir.exists())
                temp_dir.mkdirs();

            File image_file = File.createTempFile(
                    "temp-image-",
                    AppConfiguration.IMAGE_EXTENSION.getValue(),
                    temp_dir);
            // min working dpi: 94 (fastest) -> higher dpi impacts performance
            BufferedImage buffered_image = pdfRenderer.renderImageWithDPI(page_index, DPI, ImageType.RGB);
            ImageIO.write(buffered_image, AppConfiguration.IMAGE_EXTENSION.getValue().substring(1), image_file);
            temp_dir.deleteOnExit();
//            image_file.deleteOnExit();
            return image_file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static File convert_PDF_to_image(File file) {
        try {
            // Create a directory
            if (!file.exists())
                file.getParentFile().mkdirs();

            PDDocument pdDocument =  Loader.loadPDF(file);
            PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);

            File image_file = new File(file.getParent() + "\\" + file.getName().replace(AppConfiguration.FILE_EXTENSION.getValue(), AppConfiguration.IMAGE_EXTENSION.getValue()));
            BufferedImage buffered_image = pdfRenderer.renderImageWithDPI(0, DPI);

            pdDocument.close();
            return image_file;

        } catch (Exception e) {
            System.err.println("COULDN'T CONVERT " + file.getName() + " TO A IMAGE FILE! @ {" + file.getPath() + "}");
        }
        return null;
    }

    static BufferedImage create_image_for_pagination(File file) {
        try {
            PDDocument pdDocument =  Loader.loadPDF(file);
            PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
            BufferedImage buffered_image = pdfRenderer.renderImageWithDPI(0, DPI);
            pdDocument.close();
            return buffered_image;
        } catch (Exception e) {
            System.err.println("COULDN'T CONVERT " + file.getName() + " TO AN IMAGE FILE! @ {" + file.getPath() + "}");
        }
        return null;
    }

    static BufferedImage create_noData_image(String image_text){
        int width = 800;
        int height = 1000;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(new Font("Serif", Font.ITALIC, 96));
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.setPaint(new Color(150, 150, 150));
//        g2d.drawString(image_text, (width / 2) - 150, (height / 3));
        g2d.drawString(image_text, (width / 2) - 150, (height / 2));
//        g2d.drawString(image_text, (width / 2) - 150, (height / 3) * 2);

        return img;
    }

    /**
     * For encoding issue(s)
     * Important for PDF files with invalid/incomplete ToUnicode map (Character Map)
     * https://stackoverflow.com/a/45922162
     *
     */
    static void removeToUnicodeMaps(PDResources pdResources) throws IOException {
        COSDictionary resources = pdResources.getCOSObject();

        COSDictionary fonts = asDictionary(resources, COSName.FONT);
        if (fonts != null) {
            for (COSBase object : fonts.getValues()) {
                while (object instanceof COSObject)
                    object = ((COSObject) object).getObject();
                if (object instanceof COSDictionary) {
                    COSDictionary font = (COSDictionary) object;
                    font.removeItem(COSName.TO_UNICODE);
                }
            }
        }

        for (COSName name : pdResources.getXObjectNames()) {
            PDXObject xobject = pdResources.getXObject(name);
            if (xobject instanceof PDFormXObject) {
                PDResources xobjectPdResources = ((PDFormXObject) xobject).getResources();
                removeToUnicodeMaps(xobjectPdResources);
            }
        }
    }

    static private COSDictionary asDictionary(COSDictionary dictionary, COSName name) {
        COSBase object = dictionary.getDictionaryObject(name);
        return object instanceof COSDictionary ? (COSDictionary) object : null;
    }

    static Map<Long, Employee> merge_maps (Map<Long, Employee> mergeIntoMap, Map<Long, Employee> mergeFromMap){
        return Stream.of( mergeIntoMap,
                          mergeFromMap )
                .flatMap(longPersonMap -> longPersonMap.entrySet().stream())
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (employee, employee2) -> {
                                    employee.add_all_files(employee2.getFiles());
                                    return employee;
                                }
                        )
                );
    }

    static void merge_files(ObservableList<Employee> mergeIntoMap, Map<Long, Employee> mergeFromMap) {
        mergeFromMap.values().stream()
                .filter(distinctByKey(Employee::getSv_number))
                .forEach(employee -> {
                    if (!mergeIntoMap.contains(employee))
                        mergeIntoMap.add(employee);
                    else{
                        mergeIntoMap.get(mergeIntoMap.indexOf(employee)).add_all_files(employee.getFiles());
                    }
                });
    }

    static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    static Comparator<File> getFileComparator(String file_type) {
        final String file_prefix = "_Januar_";
        final String date_format = "MMM_yyyy";

        Comparator<File> fileComparator = (o1, o2) -> {
            Date date1 = null;
            Date date2 = null;
            File file1;
            File file2;

            if (o1.isDirectory() && o1.getName().contains(" ")) return -1;
            else if (o1.isDirectory()) file1 = new File( file_prefix +o1.getName() + file_type);
            else file1 = o1;

            if (o2.isDirectory() && o2.getName().contains(" ")) return -1;
            else if (o2.isDirectory()) file2 = new File(file_prefix + o2.getName() + file_type);
            else file2 = o2;

            try {
                date1 = new SimpleDateFormat(date_format, Locale.GERMAN).parse(get_it(file1));
                date2 = new SimpleDateFormat(date_format, Locale.GERMAN).parse(get_it(file2));
            } catch (ParseException e) { e.printStackTrace(); }
            if (o1.isDirectory() && o2.isFile())
                if (date1.compareTo(date2) == 0)
                    return -1;
            return date1.compareTo(date2);
        };
        return fileComparator;
    }

    private static String get_it(File name){
        return name.getName().substring(
                name.getName().indexOf('_') + 1,
                name.getName().length() - 4
        );
    }

}

