package com.github.davidmoten.chained.example;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.example.builders.Example2Builder;
import com.github.davidmoten.chained.example.builders.Example2Builder.BuilderWithName;

@Builder
// tests override of default builder class name with compile arg (see pom.xml
public class Example2 {

    private final String name;
    private final int age;

    public Example2(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static BuilderWithName name(String name) {
        return Example2Builder.builder().name(name);
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }

}
