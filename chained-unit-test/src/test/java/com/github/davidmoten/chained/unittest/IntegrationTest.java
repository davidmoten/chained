package com.github.davidmoten.chained.unittest;

import org.junit.Test;

import com.github.davidmoten.chained.unittest.TestcaseValidUsageBuilder;

public class IntegrationTest {

    @Test
    public void testValidUsage() {
        TestcaseValidUsageBuilder.of("thing");
    }

}
