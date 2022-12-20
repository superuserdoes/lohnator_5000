package com.sudo.gehaltor.services;

import com.sudo.gehaltor.config.AppConfiguration;
import com.sudo.gehaltor.pdf.utilities.PDF_File;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDF_Executor {

//    private static final int MAX_THREADS = Integer.parseInt(AppConfiguration.THREAD_POOL_SIZE.getValue());
    private static int MAX_THREADS = 1;
    private static volatile ExecutorService instance;

    private PDF_Executor(){}

    public static ExecutorService getInstance() {
        if (instance == null || instance.isTerminated() || instance.isShutdown()){
            synchronized (PDF_Executor.class) {
                if (instance == null || instance.isTerminated() || instance.isShutdown()){
                    load_saved_properties(); // TODO fix me, not final, just for quick testing
                    instance = Executors.newFixedThreadPool(MAX_THREADS, runnable -> {
                        Thread t = new Thread(runnable);
                        t.setDaemon(true);
                        return t ;
                    });
                }
            }
        }
        return instance;
    }

    private static void load_saved_properties(){
        Properties prop = new Properties();
        File file = new File(AppConfiguration.PROGRAMS_PATH.getValue() + PDF_File.PROPERTIES_FILE_NAME);

        Integer threads_count = Runtime.getRuntime().availableProcessors()/2;
        try (FileInputStream fis = new FileInputStream(file)) {
            prop.load(fis);
            threads_count = Integer.parseInt(prop.getProperty("threadCount"));
        } catch (FileNotFoundException ex) {
            // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) { }
        MAX_THREADS = threads_count;
    }
}
