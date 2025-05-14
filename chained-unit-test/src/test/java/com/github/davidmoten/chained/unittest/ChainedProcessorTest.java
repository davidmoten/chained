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

}
