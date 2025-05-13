package com.github.davidmoten.chained.example;

public class TestUsageClass {

    public static void main(String[] args) {

        TestBean instance = TestBean.builder().longField(123L).stringField("abc").build();

        System.out.println(instance.getClass().getCanonicalName());

    }


}
