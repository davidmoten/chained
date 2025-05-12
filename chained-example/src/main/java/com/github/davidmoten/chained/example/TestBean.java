package com.github.davidmoten.chained.example;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.GeneratedClass")
public class TestBean {

    private Long longField;
    private String stringField;

    public TestBean(Long longField, String stringField) {
        this.longField = longField;
        this.stringField = stringField;
    }
}
