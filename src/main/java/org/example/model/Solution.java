package org.example.model;

import org.example.util.Coordinates;
import org.example.util.SimulationData;

import java.util.List;
import java.util.Map;

import static org.example.model.OptimizedFunction.*;

public class Solution implements Comparable<Solution> {
    private List<RoutePoint> routePoints;
    private Map<OptimizedFunction, Float> functionValues;
    private SimulationData simulationData = SimulationData.getInstance();


    public Solution(List<RoutePoint> routePoints) {
        this.routePoints = routePoints;
        this.calculateFunctionValues();
    };

    private void calculateFunctionValues() {
        float fuelUsed = 0;
        float travelTime = 0;
        float routeAvgDanger = 0;

        for (RoutePoint routePoint : routePoints) {
            Map<OptimizedFunction, Double> pointFunctions = routePoint.getFunctions();
            fuelUsed += pointFunctions.get(FuelUsed);
            travelTime += pointFunctions.get(TravelTime);
            routeAvgDanger += pointFunctions.get(Danger);
        }

//        routeAvgDanger /= routePoints.size();

        this.functionValues = Map.of(
                FuelUsed, fuelUsed,
                TravelTime, travelTime,
                Danger, routeAvgDanger
        );
    }

    // -1 if is dominated by other, 0 if none dominates, 1 if dominates other
    public int checkIfDominates(Solution other) {
        boolean canDominateOther = true;
        boolean otherCanDominate = true;

        for (Map.Entry<OptimizedFunction, Float> entry : functionValues.entrySet()) {
            Float value = entry.getValue();
            Float otherValue = other.functionValues.get(entry.getKey());

            if (value > otherValue) {
                otherCanDominate = false;
            } else if (value < otherValue) {
                canDominateOther = false;
            }
        }

        // if both cannot dominate each other or both can (have the same function values)
        if (canDominateOther == otherCanDominate) {
            return 0;
        } else if (canDominateOther) {
            return 1;
        } else {
            return -1;
        }
    }

    // Go through the RoutePoints and calculate their arrival time, set weather conditions and calculate function values
    public void calculateRouteValues() {
        int currTime = simulationData.startingTime;
        routePoints.get(0).setArrivalTime(currTime);

        Coordinates temp1;
        Coordinates temp2 = new Coordinates(40, -9);

        for (int i = 1; i < routePoints.size(); i++) {
            RoutePoint prevPoint = routePoints.get(i-1);
            RoutePoint currPoint = routePoints.get(i);

            // TODO: set geocoords for each route point (null at the moment)
//            double distance = Coordinates.realDistance(currPoint.getCoordinates(), prevPoint.getCoordinates());

            temp1 = temp2;
            temp2 = new Coordinates(40, temp2.longitude() - 1);
            double distance = Coordinates.realDistance(temp1, temp2);

            int travelTimeSeconds = (int) (distance / simulationData.shipSpeed * 3600);
            currTime += travelTimeSeconds;
            currPoint.setArrivalTime(currTime);

            System.out.println(temp1 + " " + temp2 + " " + distance + " " + currTime);
        }
    }

    public Map<OptimizedFunction, Float> getFunctionValues() {
        return functionValues;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
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
