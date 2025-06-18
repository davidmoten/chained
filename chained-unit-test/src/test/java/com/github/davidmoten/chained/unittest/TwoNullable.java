package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.TwoNullableBuilder;

import jakarta.annotation.Nullable;

@Builder
public record TwoNullable(@Nullable String name, @Nullable Integer age) {

    public static TwoNullableBuilder builder() {
        return TwoNullableBuilder.builder();
    }
}
