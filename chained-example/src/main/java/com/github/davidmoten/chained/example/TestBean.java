package com.github.davidmoten.chained.example;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.api.annotation.BuilderConstructor;

@Builder("${pkg}.${simpleName}Builder")
public final class TestBean {

    private final long longField;
    private final String stringField;
    private final Optional<String> description;
    private final long calculated;
    

    // this works because generation of TestBeanFactory happens in the first
    // annotation processing round of the java compiler
    public static TestBeanBuilder builder() {
        return TestBeanBuilder.create();
    }
    
    public static TestBeanBuilder.BuilderWithLongField longField(long longField) {
        return builder().longField(longField);
    }
    
    public TestBean(String longField, String stringField, Optional<String> description) {
        this(Long.parseLong(longField), stringField, description);
    }
    
    public TestBean(String longField, long stringField, Optional<String> description) {
        this(Long.parseLong(longField), stringField + "", description);
    }

    @BuilderConstructor
    private TestBean(long longField, String stringField, Optional<String> description) {
        this.longField = longField;
        this.stringField = stringField;
        this.calculated = longField + 1;
        this.description = description;
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
    
    public Optional<String> description() {
        return description;
    }
}
