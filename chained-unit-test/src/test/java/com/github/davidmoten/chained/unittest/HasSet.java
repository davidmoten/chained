package com.github.davidmoten.chained.unittest;

import java.util.Set;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.HasSetBuilder;
import com.github.davidmoten.chained.unittest.builder.HasSetBuilder.BuilderWithName;

@Builder
public record HasSet(String name, Set<Integer> numbers) {
    public static BuilderWithName name(String name) {
        return HasSetBuilder.builder().name(name);
    }
}
