package com.github.davidmoten.chained.processor;

final class Util {
    
    static String simpleClassName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot == -1) {
            return className;
        } else {
            return className.substring(lastDot + 1);
        }
    }

    static String pkg(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        } else {
            return className.substring(0, lastDot);
        }
    }
}
