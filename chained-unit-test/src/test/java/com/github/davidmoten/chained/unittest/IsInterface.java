package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public interface IsInterface {

    String name();
    int yearOfBirth();
    Optional<String> description();
    
}
