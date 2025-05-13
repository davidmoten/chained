package com.github.davidmoten.chained.example;

public class TestUsageClass {

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        TestBean instance = TestBean.longField(123L).stringField("abc").description("hello").build();
    }
}
