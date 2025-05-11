package com.github.davidmoten.chained.api;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * List builder used by generated code.
 * 
 * @param <T> list element type
 * @param <S> the type of the continuation object being returned by buildList
 *            method
 */
public final class ListBuilder<T, S> {

    private final Supplier<S> returnObject;
    private final List<T> list;

    /**
     * Constructor.
     * 
     * @param returnObject provides the continuation object to be returned by
     *                     buildList method
     * @param list         the list to which elements are added
     */
    public ListBuilder(Supplier<S> returnObject, List<T> list) {
        this.returnObject = returnObject;
        this.list = list;
    }

    /**
     * Adds values to the list.
     * 
     * @param values values to add
     * @return this
     */
    public ListBuilder<T, S> add(@SuppressWarnings("unchecked") T... values) {
        for (T v : values) {
            list.add(v);
        }
        return this;
    }

    /**
     * Adds all values in the collection to the list.
     * 
     * @param values values to add
     * @return this
     */
    public ListBuilder<T, S> addAll(Collection<? extends T> values) {
        list.addAll(values);
        return this;
    }

    /**
     * Sets the list to contain only the values in the collection.
     * 
     * @param values values to set
     * @return this
     */
    public ListBuilder<T, S> set(Collection<? extends T> values) {
        list.clear();
        return addAll(values);
    }

    /**
     * Builds the list and returns the continuation object.
     * 
     * @return the continuation object
     */
    public S buildList() {
        return returnObject.get();
    }
}
