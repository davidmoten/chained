package com.github.davidmoten.chained.api;

public class Preconditions {

    private Preconditions() {
        // prevent instantiation
    }

    public static <T> T checkNotNull(T o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        return o;
    }
    
    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }
}
