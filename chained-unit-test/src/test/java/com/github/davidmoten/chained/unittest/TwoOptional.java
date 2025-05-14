package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.unittest.builder.TwoOptionalBuilder;

@Builder
public record TwoOptional(Optional<String> name, Optional<Integer> age) {
    public static TwoOptionalBuilder builder() {
        return TwoOptionalBuilder.builder();
    }
}
