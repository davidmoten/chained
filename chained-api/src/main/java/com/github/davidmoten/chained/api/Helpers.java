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

    @SuppressWarnings("unchecked")
    public static <T> T addToCollection(T currentValue, T newValue, Supplier<? extends T> factory, boolean isNullable) {
        T result;
        if (isNullable) {
            if (newValue == null) {
                result = null;
            } else if (currentValue == null) {
                result = factory.get();
            } else {
                result = currentValue;
            }
        } else {
            Preconditions.checkNotNull(newValue, "newValue");
            if (currentValue == null) {
                result = factory.get();
            } else {
                result = currentValue;
            }
        }
        if (result != null) {
            // note that newValue cannot be null here
            if (result instanceof Map) {
                Map<Object, Object> r = ((Map<Object, Object>) result);
                r.clear();
                r.putAll((Map<?, ?>) newValue);
            } else if (result instanceof List) {
                List<Object> r = ((List<Object>) result);
                r.clear();
                r.addAll((List<?>) newValue);
            } else if (result instanceof Set) {
                Set<Object> r = ((Set<Object>) result);
                r.clear();
                r.addAll((Set<?>) newValue);
            }
        }
        return result;
    }
}
