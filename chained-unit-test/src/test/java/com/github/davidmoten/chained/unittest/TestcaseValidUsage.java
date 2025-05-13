package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.TestcaseValidUsageBuilder")
public class TestcaseValidUsage {

    private String field;
    
    public TestcaseValidUsage(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}