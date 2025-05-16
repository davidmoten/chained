package com.github.davidmoten.chained.processor;

import java.util.Optional;

import org.junit.Test;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.processor.Generator.Output;

public class ImportsTest {
    
    @Test
    public void testImports() {
        Imports imports = new Imports("blah");
        imports.add(Optional.class);
        imports.add(String.class);
        imports.add(Builder.class);
        System.out.println(imports.add("java.util.Map<java.util.List<? extends java.lang.String>, java.util.Optional<java.lang.Integer>>"));
        
    }
    
    @Test
    public void testOutput() {
        Output o = new Output("blah");
        o.line("package boo");
        o.line();
        o.importsHere();
        o.line("public class Foo extends %s {}", Exception.class);
        System.out.println(o.toString());
    }

}
