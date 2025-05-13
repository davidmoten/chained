package com.github.davidmoten.chained.processor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.chained.processor.Generator.Parameter;

public class GeneratorTest {
    
    @Test
    public void test() {
        List<Parameter> list = new ArrayList<>();
        list.add(new Parameter("String", "name"));
        list.add(new Parameter("java.util.Optional<Integer>", "age"));
        String code = Generator.chainedBuilder("me.Thing", "me.builder.ThingBuilder", list, true);
        System.out.println(code);
    }

}
