package com.github.davidmoten.chained.unittest;

import java.util.Objects;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.unittest.builder.Point2Builder;
import com.github.davidmoten.chained.unittest.builder.Point2Builder.BuilderWithX;

@Builder
public final class Point2 { 
    
    private final int x;
    private final int y;

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public static BuilderWithX x(int x) {
        return Point2Builder.builder().x(x);
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Point2 other = (Point2) obj;
        return x == other.x && y == other.y;
    }
    
}
