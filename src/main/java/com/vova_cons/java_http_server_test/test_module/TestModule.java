package com.vova_cons.java_http_server_test.test_module;

import com.vova_cons.java_http_server_test.module.logic.LogicModule;
import com.vova_cons.java_http_server_test.module.logic.MultiThreadLogicModule;
import com.vova_cons.java_http_server_test.request.Connection;

/**
 * Created by anbu on 10.02.2021.
 **/
public class TestModule extends MultiThreadLogicModule<TestRequest, TestResponse> {
    public TestModule() {
        super("test", 8);
    }

    @Override
    public void process(Connection<TestRequest, TestResponse> connection) {
        try {
            TestRequest request = connection.getRequest();
            TestResponse response = new TestResponse();
            response.id = request.id;
            response.message = "Echo from server '" + request.message + "'";
            response.clientTime = request.time;
            response.serverTime = System.currentTimeMillis();
            response.delta = response.serverTime - response.clientTime;
            connection.response(response);
        } catch (Exception e) {
            e.printStackTrace();
            connection.close();
        }
    }
}
