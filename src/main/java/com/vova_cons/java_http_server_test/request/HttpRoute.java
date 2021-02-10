package com.vova_cons.java_http_server_test.request;

import com.badlogic.gdx.utils.Pool;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vova_cons.java_http_server_test.utils.CallbackPool;
import com.vova_cons.java_http_server_test.utils.Factory;
import com.vova_cons.java_http_server_test.utils.Processor;

/**
 * Created by anbu on 10.02.2021.
 **/
public class HttpRoute <REQUEST, RESPONSE> implements HttpHandler {
    private final Pool<Connection<REQUEST, RESPONSE>> connectionPool;
    private final String route;
    private RouteListener<REQUEST, RESPONSE> processor;

    public HttpRoute(String route, Factory<Connection<REQUEST, RESPONSE>> connectionFactory) {
        this.route = route;
        this.connectionPool = new CallbackPool<>(5, connectionFactory);
    }

    public void setListener(RouteListener<REQUEST, RESPONSE> processor) {
        this.processor = processor;
    }

    public String getRoute() {
        return route;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        Connection<REQUEST, RESPONSE> connection = connectionPool.obtain();
        connection.start(this, httpExchange);
        processor.onOpenConnection(connection);
    }

    public void free(Connection<REQUEST, RESPONSE> connection) {
        connectionPool.free(connection);
    }
}
