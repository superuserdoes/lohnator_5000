package com.sudo.gehaltor.services;

import com.sudo.gehaltor.config.AppSettings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDF_Executor {

    private static volatile ExecutorService instance;

    private PDF_Executor(){}

    public static ExecutorService getInstance() {
        if (instance == null || instance.isTerminated() || instance.isShutdown()){
            synchronized (PDF_Executor.class) {
                if (instance == null || instance.isTerminated() || instance.isShutdown()){
                    instance = Executors.newFixedThreadPool(AppSettings.getInstance().getNum_of_threads(), runnable -> {
                        Thread t = new Thread(runnable);
                        t.setDaemon(true);
                        return t ;
                    });
                }
            }
        }
        return instance;
    }

}
