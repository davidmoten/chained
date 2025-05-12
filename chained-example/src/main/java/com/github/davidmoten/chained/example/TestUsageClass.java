package com.github.davidmoten.chained.example;

public class TestUsageClass {

    public static void main(String[] args) {

        TestBean instance = TestBeanBuilder.builder().longField(123L).stringField("abc");

        System.out.println(instance.getClass().getCanonicalName());

    }


}
