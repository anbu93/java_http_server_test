package com.vova_cons.java_http_server_test.module.logic;

import com.vova_cons.java_http_server_test.request.Connection;
import com.vova_cons.java_http_server_test.utils.Processor;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by anbu on 10.02.2021.
 **/
public abstract class ModuleWorker<REQUEST, RESPONSE> implements Runnable, Processor<Connection<REQUEST, RESPONSE>> {
    protected final ConcurrentLinkedQueue<Connection<REQUEST, RESPONSE>> queue;

    public ModuleWorker(ConcurrentLinkedQueue<Connection<REQUEST, RESPONSE>> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Connection<REQUEST, RESPONSE> connection = queue.poll();
                if (connection != null) {
                    process(connection);
                }
            } catch (Exception e) {
                System.err.println("ModuleWorker error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
