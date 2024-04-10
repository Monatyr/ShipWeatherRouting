package org.example.model;

import java.util.List;
import java.util.Map;

import static org.example.model.OptimizedFunction.*;

public class Solution {
    private List<Point> routePoints;
    private Map<OptimizedFunction, Float> functionValues;


    public Solution(List<Point> points) {
        this.routePoints = points;
        this.calculateFunctionValues();
    };

    private void calculateFunctionValues() {
        float fuelUsed = 0;
        float travelTime = 0;
        float routeAvgSafety = 0;

        for (Point point : routePoints) {
            Map<OptimizedFunction, Double> pointFunctions = point.getFunctions();
            fuelUsed += pointFunctions.get(FuelUsed);
            travelTime += pointFunctions.get(TravelTime);
            routeAvgSafety += pointFunctions.get(Safety);
        }

        routeAvgSafety /= routePoints.size();

        this.functionValues = Map.of(
                FuelUsed, fuelUsed,
                TravelTime, travelTime,
                Safety, routeAvgSafety
        );
    }

    public boolean checkIfDominates(Solution other) {
        for (Map.Entry<OptimizedFunction, Float> entry : functionValues.entrySet()) {
            if (entry.getValue() < other.functionValues.get(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    public Map<OptimizedFunction, Float> getFunctionValues() {
        return functionValues;
    }

    public List<Point> getRoutePoints() {
        return routePoints;
    }
}
