package com.github.davidmoten.chained.api;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Builder for sets.
 * 
 * @param <T> type of elements in the set
 * @param <S> type of the object to be returned by buildList()
 */
public final class SetBuilder<T, S> {

    private final Supplier<S> returnObject;
    private final Set<T> set;

    /**
     * Constructor.
     * 
     * @param returnObject supplier of the object to be returned by buildList()
     * @param set          the set to which elements will be added
     */
    public SetBuilder(Supplier<S> returnObject, Set<T> set) {
        this.returnObject = returnObject;
        this.set = set;
    }

    /**
     * Adds values to the set.
     * 
     * @param values values to add
     * @return this
     */
    public SetBuilder<T, S> add(@SuppressWarnings("unchecked") T... values) {
        for (T value : values) {
            set.add(value);
        }
        return this;
    }

    /**
     * Adds all values in the collection to the set.
     * 
     * @param values collection of values to add
     * @return this
     */
    public SetBuilder<T, S> addAll(Collection<? extends T> values) {
        set.addAll(values);
        return this;
    }

    /**
     * Sets the contents of the set to be the values in the collection (clears any
     * existing values first).
     * 
     * @param values collection of values to set
     * @return this
     */
    public SetBuilder<T, S> set(Collection<? extends T> values) {
        set.clear();
        return addAll(values);
    }

    /**
     * Builds the set and returns the continuation object.
     * 
     * @return the continuation object
     */
    public S buildList() {
        return returnObject.get();
    }
}
