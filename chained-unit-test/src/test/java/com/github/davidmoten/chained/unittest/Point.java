package com.github.davidmoten.chained.unittest;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.PointBuilder;
import com.github.davidmoten.chained.unittest.builder.PointBuilder.BuilderWithX;

/**
 * Represents a point in 2D space.
 *
 * @param x the x-coordinate of the point
 * @param y the y-coordinate of the point
 */
@Builder
public record Point(int x, int y) {
    
    public static BuilderWithX x(int x) {
        return PointBuilder.builder().x(x);
    } 
}