package com.github.davidmoten.chained.unittest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.chained.unittest.builder.SingleMandatoryBuilder;
import com.github.davidmoten.chained.unittest.builder.SingleOptionalBuilder;

public class ChainedProcessorTest{

    @Test
    public void testValidUsage() {
        TestcaseValidUsageBuilder.of("thing");
    }
    
    @Test
    public void testSingleMandatory() {
        SingleMandatory s = SingleMandatoryBuilder.of("thing");
        assertEquals("thing", s.name());
    }
    
    @Test
    public void testSingleOptional() {
        SingleOptional s = SingleOptionalBuilder.builder().name("thing").build();
        assertEquals("thing", s.name().get());
    }
    
    @Test
    public void testTwoMandatory() {
        TwoMandatory s = TwoMandatory.name("fred").age(10);
        assertEquals("fred", s.name());
        assertEquals(10, s.age());
    }
    
    @Test
    public void testMixed() {
        Mixed a = Mixed.name("fred").city("London").age(10).description("someone").build();
        assertEquals("fred", a.name());
        assertEquals("London", a.city());
        assertEquals("someone", a.description().get());
        assertEquals(10, (long) a.age().get());
    }
}
