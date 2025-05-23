package com.github.davidmoten.chained.api;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

public final class SetBuilder<T, S> {

    private final Supplier<S> returnObject;
    private final Set<T> set;

    public SetBuilder(Supplier<S> returnObject, Set<T> set) {
        this.returnObject = returnObject;
        this.set = set;
    }

    public SetBuilder<T, S> add(@SuppressWarnings("unchecked") T... values) {
        for (T value : values) {
            set.add(value);
        }
        return this;
    }

    public SetBuilder<T, S> addAll(Collection<? extends T> values) {
        set.addAll(values);
        return this;
    }

    public SetBuilder<T, S> set(Collection<? extends T> values) {
        set.clear();
        return addAll(values);
    }

    public S buildList() {
        return returnObject.get();
    }
}
