package com.vova_cons.java_http_server_test.module.logic;

import com.vova_cons.java_http_server_test.request.Connection;
import com.vova_cons.java_http_server_test.utils.Processor;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by anbu on 10.02.2021.
 **/
public class DelegateModuleWorker<REQUEST, RESPONSE> extends ModuleWorker<REQUEST, RESPONSE> {
    private final Processor<Connection<REQUEST, RESPONSE>> processor;

    public DelegateModuleWorker(ConcurrentLinkedQueue<Connection<REQUEST, RESPONSE>> queue, Processor<Connection<REQUEST, RESPONSE>> processor) {
        super(queue);
        this.processor = processor;
    }

    @Override
    public void process(Connection<REQUEST, RESPONSE> connection) {
        processor.process(connection);
    }
}
