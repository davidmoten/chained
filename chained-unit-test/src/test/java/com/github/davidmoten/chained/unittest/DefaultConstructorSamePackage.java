package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.DefaultConstructorSamePackageBuilder.BuilderWithName;

@Builder("${pkg}.DefaultConstructorSamePackageBuilder")
public final class DefaultConstructorSamePackage {

    private final String name;
    private final String city;

    DefaultConstructorSamePackage(String name, String city) {
        this.name = name;
        this.city = city;
    }
    
    public static BuilderWithName name(String name) {
        return DefaultConstructorSamePackageBuilder.builder().name(name);
    }

    public String name() {
        return name;
    }

    public String city() {
        return city;
    }
}
