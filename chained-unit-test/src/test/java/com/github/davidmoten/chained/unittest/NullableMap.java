package com.github.davidmoten.chained.unittest;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public record NullableMap(String name, @Nullable Map<String, Integer> map) {
    

}
