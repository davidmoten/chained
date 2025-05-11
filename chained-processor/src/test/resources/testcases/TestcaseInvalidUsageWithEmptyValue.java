package com.github.davidmoten.chained.processor.tests;

import com.github.davidmoten.chained.api.Builder;

@Builder("")
public class TestcaseInvalidUsageWithEmptyValue {

    private String field;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}