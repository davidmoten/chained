package com.github.davidmoten.chained.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class Helpers {

    public static <K, V> Map<K, V> unmodifiable(Map<K, V> o) {
        return Collections.unmodifiableMap(o);
    }

    public static <T> List<T> unmodifiable(List<T> o) {
        return Collections.unmodifiableList(o);
    }

    public static <T> Set<T> unmodifiable(Set<T> o) {
        return Collections.unmodifiableSet(o);
    }

    public static <T> Collection<T> unmodifiable(Collection<T> o) {
        return Collections.unmodifiableCollection(o);
    }

    public static <T> T unmodifiable(T o) {
        return o;
    }

    public static <T> T createCollectionIfNotPresent(T currentValue, T newValue, Supplier<? extends T> factory, boolean isNullable) {
        if (isNullable) {
           if (newValue == null) {
               return newValue;
           } else if (currentValue == null) {
               return factory.get();
           } else {
               return currentValue;
           }
        } else {
            Preconditions.checkNotNull(newValue, "newValue");
            if (currentValue == null) {
                return factory.get();
            } else {
                return currentValue;
            }
        }
    }

}