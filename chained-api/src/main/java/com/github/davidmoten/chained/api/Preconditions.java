package com.github.davidmoten.chained.api;

/**
 * Static utility methods that help a method or constructor check whether its
 * preconditions have been met.
 */
public final class Preconditions {

    private Preconditions() {
        // prevent instantiation
    }

    /**
     * Checks that the specified object reference is not null and throws a
     * customized IllegalArgumentException if it is.
     * 
     * @param <T>  type of object
     * @param o    the object reference to check for nullity
     * @param name name of the object to include in the exception message
     * @return the non-null reference that was validated
     */
    public static <T> T checkNotNull(T o, String name) {
        if (o == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
        return o;
    }

    /**
     * Checks the truth of the given expression and throws an
     * IllegalArgumentException if it is false.
     * 
     * @param expression a boolean expression
     * @param message    the exception message to use if the check fails; will be
     *                   converted to a string using String.valueOf
     */
    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }
}
