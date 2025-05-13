package com.github.davidmoten.chained.example;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.api.BuilderIgnore;

@Builder("${pkg}.TestBeanBuilder")
public final class TestBean {

    private final long longField;
    private final String stringField;
    
    @BuilderIgnore
    private final long calculated;

    // this works because generation of TestBeanFactory happens in the first
    // annotation processing round of the java compiler
    public static TestBeanBuilder.Builder builder() {
        return TestBeanBuilder.create();
    }

    public TestBean(long longField, String stringField) {
        this.longField = longField;
        this.stringField = stringField;
        this.calculated = longField + 1;
    }

    public long longField() {
        return longField;
    }

    public String stringField() {
        return stringField;
    }

    public long calculated() {
        return calculated;
    }
}
