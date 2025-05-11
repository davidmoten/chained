package com.github.davidmoten.chained.api;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Map builder used by generated code.
 * 
 * @param <K> key type
 * @param <V> value type
 * @param <T> return type of buildMap()
 */
public final class MapBuilder<K, V, T> {

    private final Supplier<T> returnObject;
    private final Map<K, V> map;

    /**
     * Constructor.
     * 
     * @param returnObject supplier of object to return from buildMap()
     * @param map          map to build
     */
    public MapBuilder(Supplier<T> returnObject, Map<K, V> map) {
        this.returnObject = returnObject;
        this.map = map;
    }

    /**
     * Adds a key/value pair to the map.
     * 
     * @param key   the key
     * @param value the value
     * @return this
     */
    public MapBuilder<K, V, T> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Adds all entries from the specified map to the map being built.
     * 
     * @param m the map whose entries are to be added
     * @return this
     */
    public MapBuilder<K, V, T> putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
        return this;
    }

    /**
     * 
     * Clears the map being built and adds all entries from the specified map.
     * 
     * @param m the map whose entries are to be added
     * @return this
     */
    public MapBuilder<K, V, T> set(Map<? extends K, ? extends V> m) {
        map.clear();
        return putAll(m);
    }

    /**
     * Builds the map and returns the continuation object
     * 
     * @return the continuation object
     */
    public T buildMap() {
        return returnObject.get();
    }
}