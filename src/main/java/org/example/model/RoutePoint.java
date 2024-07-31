package org.example.model;

import org.example.util.Coordinates;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.time.ZonedDateTime;
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
    private ZonedDateTime arrivalTime;
    private WeatherConditions conditions;
    private Map<OptimizedFunction, Double> functions;
    private double shipSpeed;
    private static Random random = new Random();


    public RoutePoint(GridPoint gridCoordinates, Coordinates coordinates, ZonedDateTime arrivalTime) {
        this.gridCoordinates = gridCoordinates;
        this.coordinates = coordinates;

        this.arrivalTime = arrivalTime;
        this.shipSpeed = SimulationData.getInstance().shipSpeed;

        // TODO: read the weather conditions from file
        this.conditions = SimulationData.getInstance().getWeatherConditions(coordinates, arrivalTime);

        // TODO: read the function values from actual data / calculate them
        this.functions = Map.of(Danger, random.nextDouble(), FuelUsed, random.nextDouble(5, 10), TravelTime, random.nextDouble());
    }

    public RoutePoint(RoutePoint other) {
        this.gridCoordinates = other.getGridCoordinates();
        this.coordinates = other.getCoordinates();

        // TODO: read the weather conditions from file and base their value on the time of arrival in this particular solution
        this.conditions = other.conditions;

        // TODO: read from actual data
        this.functions = Map.of(
                Danger, other.functions.get(Danger),
                FuelUsed, other.functions.get(FuelUsed),
                TravelTime, other.functions.get(TravelTime)
        );
    }

    public void updateData(ZonedDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
        // TODO: read all of the values from the weather data and calculate resistances
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

    public ZonedDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(ZonedDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public WeatherConditions getWeatherConditions() {
        return conditions;
    }

    public void setWeatherConditions(WeatherConditions conditions) {
        this.conditions = conditions;
    }

    public Map<OptimizedFunction, Double> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<OptimizedFunction, Double> functions) {
        this.functions = functions;
    }

    public void setShipSpeed(double targetEndSpeed) {
        this.shipSpeed = targetEndSpeed;
    }
}
