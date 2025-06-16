package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.MixedBuilder;
import com.github.davidmoten.chained.unittest.builder.MixedBuilder.BuilderWithName;
import com.github.davidmoten.chained.unittest.builder.MixedBuilder.CopyBuilder;

@Builder
public record Mixed(String name, Optional<Integer> age, String city, Optional<String> description) {
    public static BuilderWithName name(String name) {
        return MixedBuilder.builder().name(name);
    }
    
    public static CopyBuilder from(Mixed mixed) {
        return MixedBuilder.from(mixed);
    }
}
