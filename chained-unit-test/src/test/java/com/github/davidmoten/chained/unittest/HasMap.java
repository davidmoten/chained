package com.github.davidmoten.chained.unittest;

import java.util.Map;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.HasMapBuilder;
import com.github.davidmoten.chained.unittest.builder.HasMapBuilder.BuilderWithName;

@Builder
public record HasMap(String name, Map<String, Integer> map) {
    public static BuilderWithName name(String name) {
        return HasMapBuilder.builder().name(name);
    }
}
