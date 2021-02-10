# Тестирование модульного самописного сервера

## Идея модульного сервера

Разбить цепочку обработки запроса на простые изолированные шаги:

`Создание соединения -> Десериализация входящего Json -> Обработка логики -> Сериализация ответного Json -> Отправка ответа`

Затем распределить эти шаги по модулям. Каждый модуль работает в отдельном потоке.
Каждый модуль можно было бы распараллелить.

Например модуль входящих запросов, который создает соединения и отправляет созданные соединения следующему модулю.
Модуль логики делает все остальное. Но при этом модуль логики распараллеливается внутри себя.

## Пример теста

Пример одномодульного сервера, в 8 потоков логики и 1 поток создания соединений.
```java
public class TestRequest {
    public int id;
    public long time;
    public String message;
}
public class TestResponse {
    public int id;
    public String message;
    public long clientTime;
    public long serverTime;
    public long delta;
}
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
public class Launcher {
    public static void main(String[] args) throws Exception {
        ModuleMap.register(TestModule.class, new TestModule());
        RequestModule requestModule = new RequestModule();
        requestModule.registerRoute("/test", TestRequest.class, TestResponse.class, ModuleMap.getModule(TestModule.class));
        ModuleMap.register(RequestModule.class, requestModule);
        ModuleMap.start();
    }
}
```

## Тестирование и замеры

### Код тестового клиента
```java
private class Test {
    private Queue<TestResponse> responses = new ConcurrentLinkedDeque<>();
    @Override
    public void testRequests() {
        for (int i = 0; i < 1000; i++) {
            new Thread(new RequestTestRunnable(i)).run();
        }
        try {
            Thread.sleep(5000);
        } catch (Exception ignore) {
        }
        long max = 0;
        long min = Long.MAX_VALUE;
        long total = 0;
        int count = 0;
        for (TestResponse response : responses) {
            min = Math.min(response.delta, min);
            max = Math.max(response.delta, max);
            total += response.delta;
            count++;
        }
        long average = total / count;
        System.out.println("Network test results:");
        System.out.println("\tResponses " + count);
        System.out.println("\tAverage: " + average);
        System.out.println("\tMax: " + max);
        System.out.println("\tMin: " + min);
        Gdx.app.exit();
    }

    private class RequestTestRunnable implements Runnable {
        private Json json = new Json(JsonWriter.OutputType.json);
        private final int index;
        public RequestTestRunnable(int index) {
            this.index = index;
        }
        @Override
        public void run() {
            Net.HttpRequest request = new Net.HttpRequest();
            request.setMethod(Net.HttpMethods.POST);
            request.setUrl("http://localhost:8080/test");
            request.setContent("{" +
                    "\"id\":" + index + "," +
                    "\"time\":" + System.currentTimeMillis() + "," +
                    "\"message\":\"Hello server\"," +
                    "}");
            Gdx.net.sendHttpRequest(request, new HoHttpResponseListener(
                    this::request,
                    error -> System.err.println("Error #index: " + error)
            ));
        }
        private void request(String body) {
            //System.out.println("Answer #"+index+": "+body);
            TestResponse response = json.fromJson(TestResponse.class, body);
            responses.add(response);
        }
    }

    public static class TestResponse {
        public int id;
        public String message;
        public long clientTime;
        public long serverTime;
        public long delta;
    }
}
```


### Результаты замеров 

#### sparkjava
```text
Responses 9912
Average: 1302
Max: 7473
Min: 0
Responses 1000
Average: 11
Max: 282
Min: 0
Responses 1000
Average: 7
Max: 119
Min: 0
Responses 1000
Average: 4
Max: 134
Min: 0
```

#### threads: 2, request parse json
```text
Responses 1000
Average: 491
Max: 3063
Min: 2
Responses 1000
Average: 8
Max: 97
Min: 0
Responses 1000
Average: 11
Max: 136
Min: 0
Responses 1000
Average: 8
Max: 92
Min: 0
```
#### threads: 2, logic parse json
```text
Responses 1000
Average: 145
Max: 1036
Min: 1
Responses 1000
Average: 10
Max: 116
Min: 0
Responses 1000
Average: 9
Max: 124
Min: 0
```
#### threads: 8, logic parse json
```text
Responses 1000
Average: 568
Max: 1340
Min: 3
Responses 1000
Average: 17
Max: 92
Min: 0

Responses 1000
Average: 20
Max: 150
Min: 0
Responses 9861
Average: 513
Max: 4901
Min: 0
Responses 10000
Average: 316
Max: 3622
Min: 0
```
#### single thread, request parse json
```text
Responses 1000
Average: 174
Max: 1057
Min: 3
Responses 1000
Average: 7
Max: 86
Min: 0
Responses 1000
Average: 7
Max: 115
Min: 0
```
#### single thread, logic parse json
```text
Responses 1000
Average: 164
Max: 338
Min: 84
Responses 1000
Average: 8
Max: 110
Min: 0
Responses 1000
Average: 16
Max: 131
Min: 0
```


## Вывод

При 1000 одновременных мелких запросах **sparkjava** выигрывает с небольшим отрывом.

При 10 000 одновременных мелких запросах сервер выигрывает по среднему времени ожидания.

При этом необходимо правильно сбалансировать количество модулей и потоков внутри них. 
Узкое место (модуль) может замедлить все другие модули.