package com.github.davidmoten.chained.unittest;

import java.util.List;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.HasListBuilder;
import com.github.davidmoten.chained.unittest.builder.HasListBuilder.BuilderWithName;;

@Builder
public record HasList(String name, List<Integer> list) {
    public static BuilderWithName name(String name) {
        return HasListBuilder.builder().name(name);
    }
}
