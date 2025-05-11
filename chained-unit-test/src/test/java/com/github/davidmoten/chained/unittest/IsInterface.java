package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.Preconditions;
import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.api.annotation.Check;
import com.github.davidmoten.chained.unittest.builder.IsInterfaceBuilder;

@Builder
public interface IsInterface {
    
    public static IsInterfaceBuilder builder() {
        return IsInterfaceBuilder.builder();
    }

    String name();
    
    int yearOfBirth();
    
    Optional<String> description();
    
    default String summary() {
        return name() + description().orElse("");
    }
    
    @Check
    default void check() {
        Preconditions.checkArgument(name().trim().length() > 0, "name must not be blank");
        Preconditions.checkArgument(yearOfBirth() >= 1900, "yearOfBirth must be >= 1900");
    }
}
