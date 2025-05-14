package com.github.davidmoten.chained.processor;

import java.util.Optional;

import org.junit.Test;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.processor.Generator.Output;

public class ImportsTest {
    
    @Test
    public void testImports() {
        Imports imports = new Imports();
        imports.add(Optional.class);
        imports.add(String.class);
        imports.add(Builder.class);
    }
    
    @Test
    public void testOutput() {
        Output o = new Output();
        o.line("package boo");
        o.line();
        o.importsHere();
        o.line("public class Foo extends %s {}", Exception.class);
        System.out.println(o.toString());
    }

}
