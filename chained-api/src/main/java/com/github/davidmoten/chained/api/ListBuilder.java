package com.github.davidmoten.chained.api;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public final class ListBuilder<T, S> {

    private final Supplier<S> returnObject;
    private final List<T> list;

    public ListBuilder(Supplier<S> returnObject, List<T> list) {
        this.returnObject = returnObject;
        this.list = list;
    }

    public ListBuilder<T, S> add(@SuppressWarnings("unchecked") T... values) {
        for (T v : values) {
            list.add(v);
        }
        return this;
    }

    public ListBuilder<T, S> addAll(Collection<? extends T> values) {
        list.addAll(values);
        return this;
    }

    public ListBuilder<T, S> set(Collection<? extends T> values) {
        list.clear();
        return addAll(values);
    }

    public S buildList() {
        return returnObject.get();
    }
}
