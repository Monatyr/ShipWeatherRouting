package org.example.model;


import org.example.util.Coordinates;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Random;

import static org.example.model.OptimizedFunction.*;

/**
 * A single point on the grid. It consists of several attributes:
 *
 * - geographical coordinates
 * - the arrival of the ship at the point
 * - a set of weather conditions at the specific time at the point's location (wind, waves, unusual conditions)
 * - a set of optimized metrics and their values in the previous leg of the journey (fuel consumption, ship speed, danger)
 */
public class RoutePoint {
    private Point2D gridCoordinates;
    private Coordinates coordinates;
    private int arrivalTime; // epoch time; not UTC!
    private WeatherConditions conditions;
    private Map<OptimizedFunction, Double> functions;
    private static Random random = new Random();


    public RoutePoint(Point2D gridCoordinates, Coordinates coordinates, int arrivalTime) {
        this.gridCoordinates = gridCoordinates;
        this.coordinates = coordinates;

        this.arrivalTime = arrivalTime;

        // TODO: read the weather conditions from file
        this.conditions = null;

        // TODO: read the function values from actual data / calculate them
        this.functions = Map.of(Danger, random.nextDouble(), FuelUsed, random.nextDouble(), TravelTime, random.nextDouble());
    }

    public RoutePoint(RoutePoint other) {
        this.gridCoordinates = other.getGridCoordinates();
        this.coordinates = other.getCoordinates();
        this.functions = Map.of(Danger, random.nextDouble(), FuelUsed, random.nextDouble(), TravelTime, random.nextDouble());
    }

    public Point2D getGridCoordinates() {
        return gridCoordinates;
    }

    public void setGridCoordinates(Point2D gridCoordinates) {
        this.gridCoordinates = gridCoordinates;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public WeatherConditions getConditions() {
        return conditions;
    }

    public void setConditions(WeatherConditions conditions) {
        this.conditions = conditions;
    }

    public Map<OptimizedFunction, Double> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<OptimizedFunction, Double> functions) {
        this.functions = functions;
    }
}
