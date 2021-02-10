package com.vova_cons.java_http_server_test.module.logic;

import com.vova_cons.java_http_server_test.request.Connection;
import com.vova_cons.java_http_server_test.utils.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by anbu on 10.02.2021.
 **/
public abstract class MultiThreadLogicModule<REQUEST, RESPONSE> extends LogicModule<REQUEST, RESPONSE> {
    private final int threadsCount;
    private List<Thread> threads = new ArrayList<>();
    private List<ModuleWorker<REQUEST, RESPONSE>> workers = new ArrayList<>();

    public MultiThreadLogicModule(String id, int threadsCount) {
        super(id);
        this.threadsCount = threadsCount;
    }

    @Override
    public void start() {
        for(int i = 0; i < threadsCount; i++) {
            DelegateModuleWorker<REQUEST, RESPONSE> worker = new DelegateModuleWorker<>(new ConcurrentLinkedQueue<>(), this);
            workers.add(worker);
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }
        System.out.println("Logic module " + getId() + " started with " + threadsCount + " threads");
    }

    private int index = 0;
    @Override
    public void onOpenConnection(Connection<REQUEST, RESPONSE> connection) {
        super.onOpenConnection(connection);
        ModuleWorker<REQUEST, RESPONSE> worker = workers.get(index);
        worker.queue.add(connection);
        index++;
        if (index >= threadsCount) {
            index = 0;
        }
    }

    @Override
    public void stop() {
        for(Thread thread : threads) {
            thread.interrupt();
        }
    }
}
