package org.example.model;

import org.example.physicalModel.PhysicalModel;
import org.example.util.Coordinates;
import org.example.util.SimulationData;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.model.OptimizedFunction.*;
import static org.example.physicalModel.PhysicalModel.*;

public class Solution implements Comparable<Solution> {
    private List<RoutePoint> routePoints = new ArrayList<>();
    private Map<OptimizedFunction, Float> functionValues;
    private SimulationData simulationData = SimulationData.getInstance();
    private boolean tooDangerous = false;

    //test variables TODO: delete later
    public static int below = 0;
    public static int above = 0;
    public static double fullNodePower = 0;
    public static double fullNodeSpeed = 0;
    public static double nodeNumber = 0;


    public Solution(List<RoutePoint> routePoints) {
        this.routePoints = routePoints;
        this.calculateRouteValues();
        this.calculateFunctionValues();
    }

    public Solution(Solution other) {
        for (RoutePoint otherPoint : other.routePoints) {
            this.routePoints.add(new RoutePoint(otherPoint));
        }
        this.calculateRouteValues();
        this.calculateFunctionValues();
    }

    //https://sci-hub.se/10.1145/1389095.1389224
    public int checkIfDominates(Solution other, boolean epsilonDominance) { // TODO: make sure the epsilon-Pareto function is correctly implemented
        double epsilon = simulationData.paretoEpsilon;
        if (!epsilonDominance) {
            epsilon = 0;
        }
        boolean dominatesOther = true;
        boolean otherDominates = true;

        for (Map.Entry<OptimizedFunction, Float> entry : functionValues.entrySet()) {
            Float value = entry.getValue();
            Float otherValue = other.functionValues.get(entry.getKey());

            if (value * (1 - epsilon) < otherValue) {
                otherDominates = false;
            }
            else if (otherValue * (1 - epsilon) < value) { // TODO: else if is a workaround. Other agent has a disadvantage, but this does not lead to both solutions epsilon-dominating eachother at the same time
                dominatesOther = false;
            }
            if (!otherDominates && !dominatesOther) {
                return 0;
            }
        }
        if (dominatesOther) {
            return 1;
        } else if (otherDominates) {
            return -1;
        }
        return 0;
    }

    //TODO: Go through the RoutePoints and calculate their arrival time, set weather conditions and calculate function values
    public void calculateRouteValues() {
        ZonedDateTime currTime = simulationData.startingTime;
        routePoints.get(0).updateData(currTime);
        for (int i = 1; i < routePoints.size(); i++) {
            RoutePoint prevPoint = routePoints.get(i-1);
            RoutePoint currPoint = routePoints.get(i);

            // From the previous point all the weather data will be collected and used to calculate the speed and resistances.
            // Based on the travel time data for the current point will be found out.
            double shipHeadingAngle = getShipHeadingAngle(prevPoint.getCoordinates(), currPoint.getCoordinates());
            double windAngle = prevPoint.getWeatherConditions().windAngle(); // TODO: make sure the wind angle is the angle FROM which the wind is blowing
            double waveHeight = prevPoint.getWeatherConditions().waveHeight();
            double windSpeed = prevPoint.getWeatherConditions().windSpeed();
            int BN = PhysicalModel.convertWindSpeedToBeaufort(windSpeed);

            // speed in each leg of the journey should also be a variable in the optimization process // TODO: don't read it from the config file but actually adapt the value
            double targetEndSpeed = currPoint.getShipSpeed(); // TODO: evolve the speed during mutation
            if (targetEndSpeed < 0.1) {
                System.out.println("SPEED TOO SMALL: " + targetEndSpeed);
            }

            // get calmWaterSpeed necessary for the targetEndSpeed
            double calmWaterSpeed = getCalmWaterSpeed(targetEndSpeed, 1e-2, 10, BN, shipHeadingAngle, windAngle);
            calmWaterSpeed = adjustSpeedForWaveHeight(calmWaterSpeed, waveHeight);
            double totalResistance = getTotalCalmWaterResistance(calmWaterSpeed, 1.19 * Math.pow(10, -6)); // TODO: change viscosity to actual value dependent on temp?
            double totalPower = getBrakePower(totalResistance, calmWaterSpeed) / 1000; // in kW

            // TODO: delete later
            if (totalPower < simulationData.minOutput) {
                below++;
            }
            if (totalPower > simulationData.maxOutput) {
                above++;
            }
            //

            int counter = 0;
            // adjust speed if the engine does not operate in the forces required by the targetSpeed
            while (totalPower < simulationData.minOutput || totalPower > simulationData.maxOutput) {
                if (counter > 1000) {
                    System.out.println(counter);
                }
                if (totalPower < simulationData.minOutput) {
//                    System.out.println("SMALL: " + calmWaterSpeed);
                    calmWaterSpeed += 0.2;
                } else {
//                    System.out.println("BIG: " + calmWaterSpeed);
                    calmWaterSpeed -= 0.2;
                }
                totalResistance = getTotalCalmWaterResistance(calmWaterSpeed, 1.19 * Math.pow(10, -6));
                totalPower = getBrakePower(totalResistance, calmWaterSpeed) / 1000;
                counter++;
            }
            // calculate end speed again in case the engine cannot perform under previous conditions
            targetEndSpeed = getEndSpeed(calmWaterSpeed, shipHeadingAngle, windAngle, BN);

            fullNodePower += totalPower;
            fullNodeSpeed += targetEndSpeed;
            nodeNumber++;


            double distance = Coordinates.realDistance(currPoint.getCoordinates(), prevPoint.getCoordinates()); // [km]
            int travelTimeSeconds = (int) (distance * 1000 / targetEndSpeed);
            double fuelUsed = getFuelUsed(totalPower, (double) travelTimeSeconds / 3600); // TODO: is fuel calculated correctly?
            double danger = getFractionalSafetyCoefficient( // TODO: MUST BE THE SAME UNIT. IN THE PAPER IT'S IN KNOTS!
                    prevPoint.getWeatherConditions().windSpeed(),
                    simulationData.thresholdWindSpeed,
                    simulationData.thresholdWindSpeedMargin,
                    windAngle
            );
            if (danger == 0) {
                tooDangerous = true; // if a solution has a segment that is too dangerous mark it as dangerous and delete it
            }
            currTime = currTime.plusSeconds(travelTimeSeconds);

            Map<OptimizedFunction, Double> newOptimizedFunctions = Map.of(
                    FuelUsed, fuelUsed,
                    TravelTime, (double) travelTimeSeconds,
                    Danger, danger
            );
            currPoint.setFunctions(newOptimizedFunctions);
            currPoint.setShipSpeed(targetEndSpeed);
            currPoint.updateData(currTime);
        }
    }

    public void calculateFunctionValues() {
        float fuelUsed = 0;
        float travelTime = 0;
        float routeAvgDanger = 0;
        for (int i = 1; i < routePoints.size(); i++) {  // the first point does not hold any information required for the result
            RoutePoint routePoint = routePoints.get(i);
            Map<OptimizedFunction, Double> pointFunctions = routePoint.getFunctions();
            fuelUsed += pointFunctions.get(FuelUsed);
            travelTime += pointFunctions.get(TravelTime);
            routeAvgDanger += Math.pow(1 - pointFunctions.get(Danger), 2);
        }
        this.functionValues = Map.of(
                FuelUsed, fuelUsed,
                TravelTime, travelTime,
                Danger, routeAvgDanger / routePoints.size()
        );
    }

    public double similarityBetweenSolutions(Solution other) {
        int counter = 0;
        List<RoutePoint> otherRoutePoints = other.routePoints;
        for (int i = 0; i < routePoints.size(); i++) {
            if (routePoints.get(i).getGridCoordinates().equals(otherRoutePoints.get(i).getGridCoordinates())) {
                counter++;
            }
        }
        return (double) counter / routePoints.size();
    }

    public void printCoordinates() {
        for (RoutePoint r : routePoints) {
            System.out.println(r.getCoordinates().longitude() + ", " + r.getCoordinates().latitude());
        }
    }

    public Map<OptimizedFunction, Float> getFunctionValues() {
        return functionValues;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public boolean isTooDangerous() {
        return tooDangerous;
    }

    @Override
    public int compareTo(Solution o) {
        double sum = functionValues.values().stream().reduce(0.0F, Float::sum);
        double otherSum = o.functionValues.values().stream().reduce(0.0F, Float::sum);
        if (sum > otherSum) {
            return 1;
        } else if (sum < otherSum) {
            return -1;
        }
        return 0;
    }
}
