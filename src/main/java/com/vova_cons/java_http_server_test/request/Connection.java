package com.vova_cons.java_http_server_test.request;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Created by anbu on 10.02.2021.
 **/
public class Connection<REQUEST, RESPONSE> {
    private final Json json = new Json(JsonWriter.OutputType.json);
    private final Class<REQUEST> requestType;
    private HttpRoute<REQUEST, RESPONSE> route;
    private HttpExchange http;
    private REQUEST request;

    public Connection(Class<REQUEST> requestType) {
        this.requestType = requestType;
    }

    public void start(HttpRoute<REQUEST, RESPONSE> route, HttpExchange http) {
        this.route = route;
        this.http = http;
        //request = readRequest();
    }

    private REQUEST readRequest() {
        StringBuilder buffer = new StringBuilder();
        InputStream is = http.getRequestBody();
        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            buffer.append(line).append("\r\n");
        }
        String requestBody = buffer.toString();
        return json.fromJson(requestType, requestBody);
    }

    public REQUEST getRequest() {
        return readRequest();
    }

    public void response(RESPONSE response) throws IOException {
        String body = json.toJson(response);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        http.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = http.getResponseBody()) {
            os.write(bytes);
            os.flush();
        }
        close();
    }

    public void close() {
        http.close();
        route.free(this);
    }
}
