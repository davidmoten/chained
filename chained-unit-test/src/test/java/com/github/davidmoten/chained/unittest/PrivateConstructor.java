package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.unittest.builder.PrivateConstructorBuilder;
import com.github.davidmoten.chained.unittest.builder.PrivateConstructorBuilder.BuilderWithName;

@Builder
public final class PrivateConstructor {

    private final String name;
    private final String city;

    private PrivateConstructor(String name, String city) {
        this.name = name;
        this.city = city;
    }
    
    public static BuilderWithName name(String name) {
        return PrivateConstructorBuilder.builder().name(name);
    }

    public String name() {
        return name;
    }

    public String city() {
        return city;
    }
}
