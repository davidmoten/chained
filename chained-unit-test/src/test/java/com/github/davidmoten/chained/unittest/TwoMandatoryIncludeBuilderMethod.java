package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.TwoMandatoryIncludeBuilderMethodBuilder;
import com.github.davidmoten.chained.unittest.builder.TwoMandatoryIncludeBuilderMethodBuilder.BuilderWithName;

@Builder(alwaysIncludeBuildMethod = true)
public record TwoMandatoryIncludeBuilderMethod(String name, int age) {
    public static BuilderWithName name(String name) {
        return TwoMandatoryIncludeBuilderMethodBuilder.builder().name(name);
    }
}
