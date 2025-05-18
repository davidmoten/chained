package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.IsInterfaceBuilder;

@Builder
public interface IsInterface {
    
    public static IsInterfaceBuilder builder() {
        return IsInterfaceBuilder.builder_();
    }

    String name();
    int yearOfBirth();
    Optional<String> description();
    
}
