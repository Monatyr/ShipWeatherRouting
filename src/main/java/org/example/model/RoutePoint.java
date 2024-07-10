package org.example.model;

import org.example.util.Coordinates;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

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
    private GridPoint gridCoordinates;
    private Coordinates coordinates;
    private int arrivalTime; // epoch time; not UTC!
    private WeatherConditions conditions;
    private Map<OptimizedFunction, Double> functions;
    private static Random random = new Random();


    public RoutePoint(GridPoint gridCoordinates, Coordinates coordinates, int arrivalTime) {
        this.gridCoordinates = gridCoordinates;
        this.coordinates = coordinates;

        this.arrivalTime = arrivalTime;

        // TODO: read the weather conditions from file
        this.conditions = null;

        // TODO: read the function values from actual data / calculate them
        Double danger = SimulationData.getInstance().gridForces.get(gridCoordinates.y()).get(gridCoordinates.x()).doubleValue();
        Double fuelUsed = SimulationData.getInstance().gridForces.get(gridCoordinates.y()).get(gridCoordinates.x()).doubleValue();
        this.functions = Map.of(Danger, danger, FuelUsed, fuelUsed, TravelTime, random.nextDouble());
        //        this.functions = Map.of(Danger, random.nextDouble(), FuelUsed, random.nextDouble(), TravelTime, random.nextDouble());

        calculateFunctionValues();
    }

    public RoutePoint(RoutePoint other) {
        this.gridCoordinates = other.getGridCoordinates();
        this.coordinates = other.getCoordinates();
        // TODO: read from actual data
        this.functions = Map.of(
                Danger, other.functions.get(Danger),
                FuelUsed, other.functions.get(FuelUsed),
                TravelTime, other.functions.get(TravelTime)
        );

        calculateFunctionValues();
    }

    private void calculateFunctionValues() {

    }

    public GridPoint getGridCoordinates() {
        return gridCoordinates;
    }

    public void setGridCoordinates(GridPoint gridCoordinates) {
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
