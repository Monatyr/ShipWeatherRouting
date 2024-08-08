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
        double[] res = {
                functionValues.get(OptimizedFunction.TravelTime).doubleValue(),
                functionValues.get(OptimizedFunction.FuelUsed).doubleValue(),
                functionValues.get(OptimizedFunction.Danger).doubleValue()
        };
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
