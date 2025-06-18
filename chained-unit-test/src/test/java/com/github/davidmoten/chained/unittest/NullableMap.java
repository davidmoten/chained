package com.github.davidmoten.chained.unittest;

import java.util.Map;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.NullableMapBuilder;
import com.github.davidmoten.chained.unittest.builder.NullableMapBuilder.BuilderWithName;

import jakarta.annotation.Nullable;

@Builder
public record NullableMap(String name, @Nullable Map<String, Integer> map) {
    
       public static BuilderWithName name(String name) {
            return NullableMapBuilder.builder().name(name);
        }
    
}
