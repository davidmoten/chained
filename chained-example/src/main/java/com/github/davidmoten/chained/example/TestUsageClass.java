package com.github.davidmoten.chained.example;

public class TestUsageClass {

    public static void main(String[] args) {

        TestBean instance = ImmutableTestBean.builder().longField(123L).stringField("abc");

        System.out.println(instance.getClass().getCanonicalName());

    }


}
