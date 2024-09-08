package org.example.util;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Objects;

public final class GridPoint implements Serializable {
    @Expose
    public final int y;
    @Expose
    public final int x;

    public GridPoint(int y, int x) {
        this.y = y;
        this.x = x;
    }

    public int y() {
        return y;
    }

    public int x() {
        return x;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GridPoint) obj;
        return this.y == that.y &&
                this.x == that.x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(y, x);
    }

    @Override
    public String toString() {
        return "GridPoint[" +
                "y=" + y + ", " +
                "x=" + x + ']';
    }
}
