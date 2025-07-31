package com.github.davidmoten.chained.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.davidmoten.chained.processor.Generator.Construction;
import com.github.davidmoten.chained.processor.Generator.Parameter;
import com.github.davidmoten.chained.processor.Generator.TypeModel;

public class GeneratorTest {

    @Test
    public void test() {
        List<Parameter> list = new ArrayList<>();
        list.add(new Parameter("String", "name", false, Optional.empty()));
        list.add(new Parameter("java.util.Optional<Integer>", "age", false, Optional.empty()));
        String code = Generator.chainedBuilder("me.Thing", "me.builder.ThingBuilder", list, Construction.DIRECT, false,
                "blah.ThingImpl", true);
        System.out.println(code);
    }

    @Test
    public void testTypeModelParsingNoGenerics() {
        TypeModel a = Generator.typeModel("java.util.Optional");
        assertEquals("java.util.Optional", a.baseType);
        assertTrue(a.typeArguments.isEmpty());
    }

    @Test
    public void testTypeModelParsingOptional() {
        TypeModel a = Generator.typeModel("java.util.Optional<String>");
        assertEquals("java.util.Optional", a.baseType);
        assertEquals(1, a.typeArguments.size());
        TypeModel b = a.typeArguments.get(0);
        assertEquals("String", b.baseType);
    }

    @Test
    public void testTypeModelParsingComplex() {
        TypeModel a = Generator.typeModel("Optional<Map<String, Thing<Integer, Long>>>");
        assertEquals("Optional", a.baseType);
        assertEquals(1, a.typeArguments.size());
        TypeModel b = a.typeArguments.get(0);
        assertEquals("Map", b.baseType);
        assertEquals(2, b.typeArguments.size());
        TypeModel c = b.typeArguments.get(0);
        assertEquals("String", c.baseType);
        TypeModel d = b.typeArguments.get(1);
        assertEquals("Thing", d.baseType);
        assertEquals(2, d.typeArguments.size());
        TypeModel e = d.typeArguments.get(0);
        assertEquals("Integer", e.baseType);
        TypeModel f = d.typeArguments.get(1);
        assertEquals("Long", f.baseType);
    }

}
