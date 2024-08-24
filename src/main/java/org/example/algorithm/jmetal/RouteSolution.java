package org.example.algorithm.jmetal;

import org.example.model.OptimizedFunction;
import org.example.model.RoutePoint;
import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteSolution implements Solution<RoutePoint> {
    private List<RoutePoint> variables;
    private List<Double> objectives;
    private Map<Object, Object> attributes;

    private org.example.model.Solution solution;

    public static double minTime = Double.POSITIVE_INFINITY;
    public static double minFuel = Double.POSITIVE_INFINITY;
    public static double minSafety = Double.POSITIVE_INFINITY;
    public static double maxTime = Double.NEGATIVE_INFINITY;
    public static double maxFuel = Double.NEGATIVE_INFINITY;
    public static double maxSafety = Double.NEGATIVE_INFINITY;


    public RouteSolution(org.example.model.Solution solution, int numberOfVariables, int numberOfObjectives) {
        this.solution = solution;
        variables = new ArrayList<>(numberOfVariables);
        objectives = new ArrayList<>(numberOfObjectives);
        attributes = new HashMap<>();
    }

    @Override
    public List<RoutePoint> variables() {
        return variables;
    }

    @Override
    public double[] objectives() {
        Map<OptimizedFunction, Float> functionValues = solution.getFunctionValues();

        double timeNormalized = (functionValues.get(OptimizedFunction.TravelTime).doubleValue() - RouteSolution.minTime) / (RouteSolution.maxTime - RouteSolution.minTime);
        double fuelNormalized = (functionValues.get(OptimizedFunction.FuelUsed).doubleValue() - RouteSolution.minFuel) / (RouteSolution.maxFuel - RouteSolution.minFuel);
        double safetyNormalized = (functionValues.get(OptimizedFunction.Danger).doubleValue() - RouteSolution.minSafety) / (RouteSolution.maxSafety - RouteSolution.minSafety);


        double[] res = {
                timeNormalized,
                fuelNormalized,
                safetyNormalized
        };
//        System.out.println("TIME: " + res[0] + "\tFUEL: " + res[1] + "\tSAFETY: " + res[2]);
        return res;
    }

    @Override
    public double[] constraints() {
        return new double[0];
    }

    @Override
    public Map<Object, Object> attributes() {
        return attributes;
    }

    @Override
    public Solution copy() {
        org.example.model.Solution copiedSolution = new org.example.model.Solution(solution);
        return new RouteSolution(copiedSolution, variables.size(), objectives.size());
    }

    public org.example.model.Solution getSolution() {
        return solution;
    }

    public void setSolution(org.example.model.Solution sol) {
        this.solution = sol;
    }
}
