package com.github.davidmoten.chained.unittest;

import java.util.SortedSet;

public record SetSorted(String name, SortedSet<Integer> numbers) {
//    public static BuilderWithName name(String name) {
//        return SetSortedBuilder.builder().name(name);
//    }
}
