package com.github.davidmoten.chained.example;

import java.util.Optional;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.api.annotation.BuilderConstructor;

@Builder("${pkg}.${simpleName}Builder")
public final class TestBean {

    private final long number;
    private final String name;
    private final Optional<String> description;
    private final long calculated;
    

    // this works because generation of TestBeanFactory happens in the first
    // annotation processing round of the java compiler
    public static TestBeanBuilder builder() {
        return TestBeanBuilder.create();
    }
    
    public static TestBeanBuilder.BuilderWithNumber number(long number) {
        return builder().number(number);
    }
    
    public TestBean(String number, String name, Optional<String> description) {
        this(Long.parseLong(number), name, description);
    }
    
    public TestBean(String number, long name, Optional<String> description) {
        this(Long.parseLong(number), name + "", description);
    }

    @BuilderConstructor
    private TestBean(long number, String name, Optional<String> description) {
        this.number = number;
        this.name = name;
        this.calculated = number + 1;
        this.description = description;
    }

    public long number() {
        return number;
    }

    public String name() {
        return name;
    }

    public long calculated() {
        return calculated;
    }
    
    public Optional<String> description() {
        return description;
    }
}
