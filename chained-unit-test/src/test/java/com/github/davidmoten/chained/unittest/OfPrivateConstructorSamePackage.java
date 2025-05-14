package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.unittest.builder.OfPrivateConstructorSamePackageBuilder;

@Builder
public class OfPrivateConstructorSamePackage {

    private final String name;
    
    private OfPrivateConstructorSamePackage(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }
    
    public static OfPrivateConstructorSamePackage of(String name) {
        return OfPrivateConstructorSamePackageBuilder.of(name);
    }
}
