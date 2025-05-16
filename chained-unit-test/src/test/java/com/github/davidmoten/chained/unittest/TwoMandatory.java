package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.TwoMandatoryBuilder;
import com.github.davidmoten.chained.unittest.builder.TwoMandatoryBuilder.BuilderWithName;

@Builder
public record TwoMandatory(String name, int age) {
    
    public static BuilderWithName name(String name) {
        return TwoMandatoryBuilder.builder().name(name);
    }

}
