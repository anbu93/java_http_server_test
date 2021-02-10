package com.vova_cons.java_http_server_test;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vova_cons.java_http_server_test.module.ModuleMap;
import com.vova_cons.java_http_server_test.request.RequestModule;
import com.vova_cons.java_http_server_test.test_module.TestModule;
import com.vova_cons.java_http_server_test.test_module.TestRequest;
import com.vova_cons.java_http_server_test.test_module.TestResponse;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by anbu on 09.02.2021.
 **/
public class Launcher {

    public static void main(String[] args) throws Exception {
        if (true) {
            Spark.port(8080);
            Spark.post("/test", (req, res) -> {
                Json json = new Json(JsonWriter.OutputType.json);
                TestRequest requestJson = json.fromJson(TestRequest.class, req.body());
                TestResponse responseJson = new TestResponse();
                responseJson.id = requestJson.id;
                responseJson.message = requestJson.message;
                responseJson.delta = System.currentTimeMillis() - requestJson.time;
                return json.toJson(responseJson);
            });
        } else if (true) {
            ModuleMap.register(TestModule.class, new TestModule());
            RequestModule requestModule = new RequestModule();
            requestModule.registerRoute("/test", TestRequest.class, TestResponse.class, ModuleMap.getModule(TestModule.class));
            ModuleMap.register(RequestModule.class, requestModule);
            ModuleMap.start();
        } else {
            new Launcher().run();
        }
    }

    private AtomicBoolean process = new AtomicBoolean(false);

    public void run() throws Exception {
        System.out.println("Hello world");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/test1", new MyHandler(1));
        server.createContext("/test2", new MyHandler(2));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    class MyHandler implements HttpHandler {
        private final int id;

        public MyHandler(int id) {
            this.id = id;
        }

        @Override
        public void handle(HttpExchange http) throws IOException {
            boolean isAsyncProcess = process.getAndSet(true);
            StringBuilder sb = new StringBuilder();
            InputStream is = http.getRequestBody();
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sb.append(line).append("\r\n");
            }
            String requestBody = sb.toString();

            Json json = new Json(JsonWriter.OutputType.json);
            TestRequest requestJson = json.fromJson(TestRequest.class, requestBody);
            if (isAsyncProcess) {
                System.out.println(id + " is async request: " + requestJson.id + "> " + requestJson.message);
            } else {
                System.out.println(id + " is sync request: " + requestJson.id + "> " + requestJson.message);
            }

            TestResponse responseJson = new TestResponse();
            responseJson.id = requestJson.id;
            responseJson.message = id + " " + requestJson.message;
            responseJson.delta = System.currentTimeMillis() - requestJson.time;
            String response = json.toJson(responseJson);

            http.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = http.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
            http.close();
            process.set(false);
        }
    }
}
