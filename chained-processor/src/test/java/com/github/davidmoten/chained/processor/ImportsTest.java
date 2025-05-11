package com.github.davidmoten.chained.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.processor.Generator.Output;

public class ImportsTest {

    @Test
    public void testImports() {
        Imports imports = new Imports("blah");
        imports.add(Optional.class);
        imports.add(String.class);
        imports.add(Builder.class);
        assertEquals("Map<List<? extends String>, Optional<Integer>>", //
                imports.add(
                        "java.util.Map<java.util.List<? extends java.lang.String>, java.util.Optional<java.lang.Integer>>"));
        assertEquals("import com.github.davidmoten.chained.api.annotation.Builder;\n" + "import java.lang.Integer;\n"
                + "import java.lang.String;\n" + "import java.util.List;\n" + "import java.util.Map;\n"
                + "import java.util.Optional;\n", imports.toCode());
    }

    @Test
    public void testOutput() {
        Output o = new Output("blah");
        o.line("package boo");
        o.importsHere();
        o.line("public class Foo extends %s {}", Exception.class);
        assertEquals("package boo\n"
                + "\n"
                + "import java.lang.Exception;\n"
                + "public class Foo extends Exception {}", o.toString());
    }

}
