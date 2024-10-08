package org.example.model;

import com.google.gson.annotations.Expose;
import org.example.physicalModel.PhysicalModel;
import org.example.util.Coordinates;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.io.Serializable;
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
public class RoutePoint implements Serializable {
    @Expose
    private GridPoint gridCoordinates;
    @Expose
    private Coordinates coordinates;
    private ZonedDateTime arrivalTime = SimulationData.getInstance().startingTime;
    @Expose
    private String arrivalTimeStr = SimulationData.getInstance().startingTime.toString();
    @Expose
    private WeatherConditions conditions;
    @Expose
    private Map<OptimizedFunction, Double> functions;
    @Expose
    private double shipSpeed;
    private static Random random = new Random();


    public RoutePoint(GridPoint gridCoordinates, Coordinates coordinates) {
        this.gridCoordinates = gridCoordinates;
        this.coordinates = coordinates;
//        this.arrivalTime = arrivalTime;
//        this.shipSpeed = shipSpeed; // TODO: evolve speed

//        this.conditions = SimulationData.getInstance().getWeatherConditions(coordinates, arrivalTime);

        // TODO: read the function values from actual data / calculate them
//        this.functions = PhysicalModel.
//        this.functions = Map.of(Danger, random.nextDouble(), FuelUsed, random.nextDouble(5, 10), TravelTime, random.nextDouble());
    }

    public RoutePoint(RoutePoint other) {
        this.gridCoordinates = other.getGridCoordinates();
        this.coordinates = other.getCoordinates();
        this.shipSpeed = other.shipSpeed;

        // TODO: read the weather conditions from file and base their value on the time of arrival in this particular solution
//        this.conditions = other.conditions;

        // TODO: read from actual data
//        this.functions = Map.of(
//                Danger, other.functions.get(Danger),
//                FuelUsed, other.functions.get(FuelUsed),
//                TravelTime, other.functions.get(TravelTime)
//        );
    }

    public void updateData(ZonedDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
        this.arrivalTimeStr = arrivalTime.toString();
        this.conditions = SimulationData.getInstance().getWeatherConditions(coordinates, arrivalTime);
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
        this.arrivalTimeStr = arrivalTime.toString();
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

    public double getShipSpeed() { return shipSpeed; }

    @Override
    public String toString() {
        return "(" + coordinates.toString() + ")";
    }
}
