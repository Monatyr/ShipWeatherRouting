package org.example.util;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Objects;

public final class Coordinates implements Serializable {
    @Expose
    public final double latitude;
    @Expose
    public final double longitude;

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Use the haversine formula to calculate the distance between two points on a sphere
     */
    public static double realDistance(Coordinates coords1, Coordinates coords2) {
        int radius = 6371; // [km]
        double latitude1 = Math.toRadians(coords1.latitude);
        double longitude1 = Math.toRadians(coords1.longitude);
        double latitude2 = Math.toRadians(coords2.latitude);
        double longitude2 = Math.toRadians(coords2.longitude);

        return 2 * radius * Math.asin(Math.sqrt((1 - Math.cos(latitude2 - latitude1)
                + Math.cos(latitude1) * Math.cos(latitude2) * (1 - Math.cos(longitude2 - longitude1))) / 2));
    }

    public static double realDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        int radius = 6371; // [km]
        latitude1 = Math.toRadians(latitude1);
        longitude1 = Math.toRadians(longitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude2 = Math.toRadians(longitude2);

        return 2 * radius * Math.asin(Math.sqrt((1 - Math.cos(latitude2 - latitude1)
                + Math.cos(latitude1) * Math.cos(latitude2) * (1 - Math.cos(longitude2 - longitude1))) / 2));
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Coordinates) obj;
        return Double.doubleToLongBits(this.latitude) == Double.doubleToLongBits(that.latitude) &&
                Double.doubleToLongBits(this.longitude) == Double.doubleToLongBits(that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

}
