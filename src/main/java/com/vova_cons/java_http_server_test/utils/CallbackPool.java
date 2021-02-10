package com.vova_cons.java_http_server_test.utils;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by anbu on 10.02.2021.
 **/
public class CallbackPool<T> extends Pool<T> {
    private final Factory<T> factory;

    public CallbackPool(Factory<T> factory) {
        this.factory = factory;
    }

    public CallbackPool(int initialCapacity, Factory<T> factory) {
        super(initialCapacity);
        this.factory = factory;
    }

    public CallbackPool(int initialCapacity, int max, Factory<T> factory) {
        super(initialCapacity, max);
        this.factory = factory;
    }

    @Override
    protected T newObject() {
        return factory.build();
    }
}
