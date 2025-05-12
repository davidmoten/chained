package com.github.davidmoten.chained.integrationtest;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.GeneratedClass")
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