package org.example.model;


import org.example.util.Coordinates;

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
public class Point {
    private Coordinates coordinates;
    private int arrivalTime; // epoch time; not UTC!
    private WeatherConditions conditions;
    private Map<OptimizedFunction, Double> functions;


    public Point(Coordinates coordinates, int arrivalTime, WeatherConditions conditions, Map<OptimizedFunction, Float> functions) {
        this.coordinates = coordinates;
        this.arrivalTime = arrivalTime;
        this.conditions = conditions;

        // TODO: read the function values from actual data / calculate them
        Random random = new Random();
        this.functions = Map.of(Danger, random.nextDouble(), FuelUsed, random.nextDouble(), TravelTime, random.nextDouble());
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public WeatherConditions getConditions() {
        return conditions;
    }

    public Map<OptimizedFunction, Double> getFunctions() {
        return functions;
    }
}
