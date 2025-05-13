package com.github.davidmoten.chained.example;

import com.github.davidmoten.chained.api.Builder;

@Builder("${pkg}.TestBeanFactory")
public class TestBean {
    
    public static TestBeanFactory.Builder builder() {
          return TestBeanFactory.builder();
    }

    private long longField;
    private String stringField;

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
