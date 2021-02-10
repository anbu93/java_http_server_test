package com.vova_cons.java_http_server_test.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anbu on 10.02.2021.
 **/
public class ModuleMap {
    private static List<Module> modules = new ArrayList<>();
    private static Map<Class<? extends Module>, Module> modulesMap = new HashMap<>();

    public static <T extends Module> void register(Class<T> type, T module) {
        modules.add(module);
        modulesMap.put(type, module);
    }

    public static <T extends Module> T getModule(Class<T> type) {
        try {
            Module module = modulesMap.get(type);
            return (T) module;
        } catch (Exception e) {
            throw new RuntimeException("get module " + type.getSimpleName() + " not found this module (unsupported cast)");
        }
    }

    public static void start() throws Exception {
        for(Module module : modules) {
            module.start();
        }
    }

    public static void stop() throws Exception {
        for(Module module : modules) {
            module.stop();
        }
    }
}
