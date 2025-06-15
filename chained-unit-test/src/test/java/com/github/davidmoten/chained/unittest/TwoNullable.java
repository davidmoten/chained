package com.github.davidmoten.chained.unittest;

import javax.annotation.Nullable;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.TwoNullableBuilder;

@Builder
public record TwoNullable(@Nullable String name, @Nullable Integer age) {

    public static TwoNullableBuilder builder() {
        return TwoNullableBuilder.builder();
    }
}
