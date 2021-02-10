package com.vova_cons.java_http_server_test.request;

/**
 * Created by anbu on 10.02.2021.
 **/
public interface RouteListener<REQUEST, RESPONSE> {
    void onOpenConnection(Connection<REQUEST, RESPONSE> connection);
}
