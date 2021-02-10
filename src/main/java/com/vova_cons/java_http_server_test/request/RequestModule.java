package com.vova_cons.java_http_server_test.request;

import com.sun.net.httpserver.HttpServer;
import com.vova_cons.java_http_server_test.module.Module;
import com.vova_cons.java_http_server_test.module.ModuleMap;
import com.vova_cons.java_http_server_test.test_module.TestModule;
import com.vova_cons.java_http_server_test.test_module.TestRequest;
import com.vova_cons.java_http_server_test.test_module.TestResponse;
import com.vova_cons.java_http_server_test.utils.Processor;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by anbu on 10.02.2021.
 **/
public class RequestModule implements Module {
    private int port = 8080;
    private Executor executor = null; // creates a default executor
    private List<HttpRoute<?, ?>> routes = new ArrayList<>();
    private HttpServer server;

    public void port(int port) {
        this.port = port;
    }

    public void executor(Executor executor) {
        this.executor = executor;
    }

    public <REQUEST, RESPONSE> void registerRoute(String route, Class<REQUEST> request, Class<RESPONSE> response, RouteListener<REQUEST, RESPONSE> processor) {
        HttpRoute<REQUEST, RESPONSE> handler = new HttpRoute<>(route, () -> new Connection<>(request));
        handler.setListener(processor);
        routes.add(handler);
    }

    @Override
    public void start() throws Exception {
        System.out.println("Starting request module");
        server = HttpServer.create(new InetSocketAddress(port), 0);
        for(HttpRoute<?, ?> route : routes) {
            System.out.println("Register route " + route.getRoute());
            server.createContext(route.getRoute(), route);
        }
        server.setExecutor(executor);
        server.start();
        System.out.println("Request module started");
    }

    @Override
    public void stop() {
        server.stop(0);
    }
}
