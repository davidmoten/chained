package com.github.davidmoten.chained.unittest;

import javax.annotation.Nullable;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.OptionalAsNullableBuilder;
import com.github.davidmoten.chained.unittest.builder.OptionalAsNullableBuilder.BuilderWithName;

@Builder
public record OptionalAsNullable(String name, @Nullable Integer age) {

    public static BuilderWithName name(String name) {
        return OptionalAsNullableBuilder.builder().name(name);
    }

}
