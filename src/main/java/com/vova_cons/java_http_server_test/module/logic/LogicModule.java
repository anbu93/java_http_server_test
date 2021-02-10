package com.vova_cons.java_http_server_test.module.logic;

import com.vova_cons.java_http_server_test.module.Module;
import com.vova_cons.java_http_server_test.module.logic.ModuleWorker;
import com.vova_cons.java_http_server_test.request.Connection;
import com.vova_cons.java_http_server_test.request.RouteListener;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by anbu on 10.02.2021.
 **/
public abstract class LogicModule<REQUEST, RESPONSE> extends ModuleWorker<REQUEST, RESPONSE> implements Module, RouteListener<REQUEST, RESPONSE> {
    private final String id;
    private Thread thread;

    public LogicModule(String id) {
        super(new ConcurrentLinkedQueue<>());
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
        System.out.println("Logic module " + id + " started");
    }

    @Override
    public void onOpenConnection(Connection<REQUEST, RESPONSE> connection) {
        queue.add(connection);
    }

    @Override
    public void stop() {
        thread.interrupt();
    }
}
