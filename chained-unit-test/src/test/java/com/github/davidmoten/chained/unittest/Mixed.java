package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.unittest.builder.MixedBuilder;
import com.github.davidmoten.chained.unittest.builder.MixedBuilder.BuilderWithName;

@Builder
public record Mixed(String name, Optional<Integer> age, String city, Optional<String> description) {
    public static BuilderWithName name(String name) {
        return MixedBuilder.builder().name(name);
    }
}
