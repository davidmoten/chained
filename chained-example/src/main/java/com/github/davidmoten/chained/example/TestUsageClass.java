package com.github.davidmoten.chained.example;

public class TestUsageClass {

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        TestBean instance = TestBean.number(123L).name("abc").description("hello").build();
    }
}
