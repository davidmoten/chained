package com.github.davidmoten.chained.example;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.TestBeanFactory")
public final class TestBean {
    
    private long longField;
    private String stringField;

    // this works because generation of TestBeanFactory happens in the first 
    // annotation processing round of the java compiler
    public static TestBeanFactory.Builder builder() {
        return TestBeanFactory.builder();
    }

    public TestBean(long longField, String stringField) {
        this.longField = longField;
        this.stringField = stringField;
    }
    
    public long longField() {
        return longField;
    }
    
    public String stringField() {
        return stringField;
    }
}
