package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.ClassWithNoArgsConstructorBuilder;
import com.github.davidmoten.chained.unittest.builder.ClassWithNoArgsConstructorBuilder.CopyBuilder;

@Builder
public class ClassWithNoArgsConstructor {

    private int x;
    private int y;
    
    public ClassWithNoArgsConstructor() {
        // no-args constructor
    }
    
    /**
     * Constructor.
     * 
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     */
    public ClassWithNoArgsConstructor(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public static ClassWithNoArgsConstructorBuilder builder() {
        return ClassWithNoArgsConstructorBuilder.builder();
    }
    
    public CopyBuilder copy() {
        return ClassWithNoArgsConstructorBuilder.copy(this);
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }
    
    public void setX(int x) {
        this.x = x;
    }   
    
    public void setY(int y) {
        this.y = y;
    }
}
