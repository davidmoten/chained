package com.github.davidmoten.chained.api;

import java.util.Map;
import java.util.function.Supplier;

public final class MapBuilder<K, V, T> {

    private final Supplier<T> returnObject;
    private final Map<K, V> map;

    public MapBuilder(Supplier<T> returnObject, Map<K, V> map) {
        this.returnObject = returnObject;
        this.map = map;
    }
    
    public MapBuilder<K, V, T> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder<K, V, T> putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
        return this;
    }
    
    public MapBuilder<K, V, T> set(Map<? extends K, ? extends V> m) {
        map.clear();
        return putAll(m);
    }
    
    public T buildMap() {
        return returnObject.get();
    }
}