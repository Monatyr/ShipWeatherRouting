package org.example.algorithm.jmetal;

import org.example.algorithm.Algorithm;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.OptimizedFunction;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.uma.jmetal.problem.Problem;

import java.util.*;

import static org.example.model.action.ActionFactory.simulationData;

public class RouteProblem implements Problem<RouteSolution> {
    private Random random = new Random();
    private String[] routeFiles = {
            "src/main/resources/initial-routes/great_circle_route.txt",
            "src/main/resources/initial-routes/rhumb_line_route.txt"
    };
    private Set<Solution> initialSolutions = new HashSet<>();

    // Getters
    @Override
    public int numberOfVariables() {
        return 1;
    }

    @Override
    public int numberOfObjectives() {
        return 3;
    }

    @Override
    public int numberOfConstraints() {
        return 0;
    }

    @Override
    public String name() {
        return "RouteOptimization";
    }

    // Methods
    @Override
    public RouteSolution evaluate(RouteSolution routeSolution) {
        routeSolution.getSolution().calculateRouteValues();
        routeSolution.getSolution().calculateFunctionValues();
        simulationData.paretoEpsilon = Math.max(simulationData.paretoEpsilon - 0.000001, 0);


        Map<OptimizedFunction, Float> functions = routeSolution.getSolution().getFunctionValues();
        if (functions.get(OptimizedFunction.TravelTime) < RouteSolution.minTime) {
            RouteSolution.minTime = functions.get(OptimizedFunction.TravelTime);
        }
        if (functions.get(OptimizedFunction.FuelUsed) < RouteSolution.minFuel) {
            RouteSolution.minFuel = functions.get(OptimizedFunction.FuelUsed);
        }
        if (functions.get(OptimizedFunction.Danger) < RouteSolution.minSafety) {
            RouteSolution.minSafety = functions.get(OptimizedFunction.Danger);
        }
        if (functions.get(OptimizedFunction.TravelTime) > RouteSolution.maxTime) {
            RouteSolution.maxTime = functions.get(OptimizedFunction.TravelTime);
        }
        if (functions.get(OptimizedFunction.FuelUsed) > RouteSolution.maxFuel) {
            RouteSolution.maxFuel = functions.get(OptimizedFunction.FuelUsed);
        }
        if (functions.get(OptimizedFunction.Danger) > RouteSolution.maxSafety) {
            RouteSolution.maxSafety = functions.get(OptimizedFunction.Danger);
        }
        return routeSolution;
    }

    @Override
    public RouteSolution createSolution() {
        List<RoutePoint> route = EMASSolutionGenerator.getRouteFromFile(routeFiles[random.nextInt(0, 2)]);
        Solution solution = Algorithm.generateInitialSolution(route);
        initialSolutions.add(solution);
        return new RouteSolution(solution, 1, 3);
    }

    public Set<Solution> getInitialSolutions() {
        return initialSolutions;
    }
}
