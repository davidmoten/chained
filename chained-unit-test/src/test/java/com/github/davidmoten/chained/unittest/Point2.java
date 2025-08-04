package com.github.davidmoten.chained.unittest;

import java.util.Objects;

import com.github.davidmoten.chained.api.annotation.Builder;

@Builder
public final class Point2 { 
    
    private final int x;
    private final int y;

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
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
