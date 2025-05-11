package com.github.davidmoten.chained.example;

public class ExampleMain {

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        Example instance = Example.number(123L).name("abc").description("hello").build();
    }
}
