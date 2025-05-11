package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.TransformFieldsBuilder;
import com.github.davidmoten.chained.unittest.builder.TransformFieldsBuilder.BuilderWithA;

@Builder
public record TransformFields(int a, int b) {
    
    public TransformFields {
        // override constructor parameters
        a = Math.max(0, a);
        b = Math.max(2, b);
    }

    public static BuilderWithA a(int a) {
        return TransformFieldsBuilder.builder().a(a);
    }
}
