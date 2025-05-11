package com.github.davidmoten.chained.processor.tests;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.Xyz")
public class TestcaseInvalidUsageNoNoargConstructor {

    private String field;

    public  TestcaseInvalidUsageNoNoargConstructor (String arg) {
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}