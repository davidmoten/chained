package com.github.davidmoten.chained.api;

import java.util.function.BiConsumer;

public final class MapBuilder<K, V, T> {

    private final T returnObject;
    private final BiConsumer<K, V> action;

    public MapBuilder(T returnObject, BiConsumer<K, V> action) {
        this.returnObject = returnObject;
        this.action = action;
    }

    public MapBuilder<K, V, T> put(K key, V value) {
        action.accept(key, value);
        return this;
    }

    public T buildMap() {
        return returnObject;
    }
}