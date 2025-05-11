package com.github.davidmoten.chained.unittest;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.TwoOptionalBuilder;
import com.github.davidmoten.chained.unittest.builder.TwoOptionalBuilder.CopyBuilder;

@Builder
public record TwoOptional(Optional<String> name, Optional<Integer> age) {
    public static TwoOptionalBuilder builder() {
        return TwoOptionalBuilder.builder();
    }

    public CopyBuilder copy() {
        return TwoOptionalBuilder.copy(this);
    }
}
