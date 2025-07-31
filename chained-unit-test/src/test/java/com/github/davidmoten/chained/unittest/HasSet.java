package com.github.davidmoten.chained.unittest;

import java.util.Set;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.HasSetBuilder;
import com.github.davidmoten.chained.unittest.builder.HasSetBuilder.BuilderWithName;

/**
 * A record with a set of integers and a name.
 * 
 *  @param name the name of the set of numbers
 *  @param numbers the set of numbers
 */
@Builder
public record HasSet(String name, Set<Integer> numbers) {
    public static BuilderWithName name(String name) {
        return HasSetBuilder.builder().name(name);
    }
}
