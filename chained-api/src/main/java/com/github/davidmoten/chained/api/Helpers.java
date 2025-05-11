package com.github.davidmoten.chained.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Helper methods for use by generated code.
 */
public final class Helpers {

    private Helpers() {
        // prevent instantiation
    }

    /**
     * Returns an unmodifiable view of the specified map. If the specified map is
     * null, returns null.
     * 
     * @param <K> map key type
     * @param <V> map value type
     * @param o   input map
     * @return unmodifiable view of the specified map, or null if the input map is
     *         null
     */
    public static <K, V> Map<K, V> unmodifiable(Map<K, V> o) {
        if (o == null) {
            return null;
        }
        return Collections.unmodifiableMap(o);
    }

    /**
     * Returns an unmodifiable view of the specified list. If the specified list is
     * null, returns null.
     * 
     * @param <T> list element type
     * @param o   input list
     * @return unmodifiable view of the specified list, or null if the input list is
     *         null
     */
    public static <T> List<T> unmodifiable(List<T> o) {
        if (o == null) {
            return null;
        }
        return Collections.unmodifiableList(o);
    }

    /**
     * Returns an unmodifiable view of the specified set. If the specified set is
     * null, returns null.
     * 
     * @param <T> set element type
     * @param o   input set
     * @return unmodifiable view of the specified set, or null if the input set is
     *         null
     */
    public static <T> Set<T> unmodifiable(Set<T> o) {
        if (o == null) {
            return null;
        }
        return Collections.unmodifiableSet(o);
    }

    /**
     * Returns an unmodifiable view of the specified collection. If the specified
     * collection is null, returns null.
     * 
     * @param <T> collection element type
     * @param o   input collection
     * @return unmodifiable view of the specified collection, or null if the input
     *         collection is null
     */
    public static <T> Collection<T> unmodifiable(Collection<T> o) {
        if (o == null) {
            return null;
        }
        return Collections.unmodifiableCollection(o);
    }

    /**
     * Returns the object as is. This method exists as a fallback for types that
     * don't have convenient unmodifiable wrappers in the JDK.
     * 
     * @param <T> input object type
     * @param o   input object
     * @return the input object
     */
    public static <T> T unmodifiable(T o) {
        return o;
    }

    /**
     * 
     * Adds all elements from newValue to currentValue, creating currentValue using
     * the supplied factory if currentValue is null. If isNullable is true and
     * newValue is null then null is returned. If isNullable is false and newValue
     * is null then a NullPointerException is thrown. If currentValue is null and
     * newValue is non-null then a new collection is created using the factory. If
     * currentValue is non-null and newValue is non-null then all elements from
     * newValue are added to currentValue. Note that in all cases the contents of
     * currentValue are cleared before adding the elements of newValue.
     *
     * @param <T>          type of collection
     * @param currentValue current value (may be null)
     * @param newValue     new value (may be null if isNullable is true)
     * @param factory      factory to create new collection if currentValue is null
     * @param isNullable   true if newValue may be null
     * @return collection containing all elements of newValue, or null if isNullable
     *         is true and newValue is null
     */
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
