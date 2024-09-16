package org.example.algorithm.jmetal;

import org.example.algorithm.Algorithm;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.OptimizedFunction;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.uma.jmetal.problem.Problem;

import java.util.*;

import static org.example.algorithm.emas.EMASSolutionGenerator.flattenRoute;
import static org.example.model.action.ActionFactory.simulationData;

public class RouteProblem implements Problem<RouteSolution> {
    private final List<RoutePoint> gcr = EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/great_circle_route.txt");
    private int creatorIndex = 0;
    private Random random = new Random();
    private String[] routeFiles = {
            "src/main/resources/initial-routes/great_circle_route.txt",
            "src/main/resources/initial-routes/rhumb_line_route.txt"
    };
    private Set<Solution> initialSolutions = new HashSet<>();
    private int populationSize;
    private int createdSolutions = 0;
    public long startTime;
    public long initPopulationEndTime;

    public RouteProblem(int populationSize) {
        this.populationSize = populationSize;
    }

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

//        Map<OptimizedFunction, Float> functions = routeSolution.getSolution().getFunctionValues();
//        if (functions.get(OptimizedFunction.TravelTime) < RouteSolution.minTime) {
//            RouteSolution.minTime = functions.get(OptimizedFunction.TravelTime);
//        }
//        if (functions.get(OptimizedFunction.FuelUsed) < RouteSolution.minFuel) {
//            RouteSolution.minFuel = functions.get(OptimizedFunction.FuelUsed);
//        }
//        if (functions.get(OptimizedFunction.Danger) < RouteSolution.minSafety) {
//            RouteSolution.minSafety = functions.get(OptimizedFunction.Danger);
//        }
//        if (functions.get(OptimizedFunction.TravelTime) > RouteSolution.maxTime) {
//            RouteSolution.maxTime = functions.get(OptimizedFunction.TravelTime);
//        }
//        if (functions.get(OptimizedFunction.FuelUsed) > RouteSolution.maxFuel) {
//            RouteSolution.maxFuel = functions.get(OptimizedFunction.FuelUsed);
//        }
//        if (functions.get(OptimizedFunction.Danger) > RouteSolution.maxSafety) {
//            RouteSolution.maxSafety = functions.get(OptimizedFunction.Danger);
//        }
        return routeSolution;
    }

    @Override
    public RouteSolution createSolution() {
        if (createdSolutions == 0) {
            startTime = System.nanoTime();
        }
        Solution solution = null;
        double[] factors = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        List<RoutePoint> route;
        while (solution == null) {
            if (creatorIndex >= factors.length) {
                route = EMASSolutionGenerator.getRouteFromFile(routeFiles[creatorIndex % 2]);
            } else {
                route = EMASSolutionGenerator.flattenRoute(this.gcr, factors[creatorIndex]);
            }
            double routeTargetSpeed = random.nextDouble(simulationData.minSpeed, simulationData.maxSpeed);
            solution = Algorithm.generateInitialSolution(route, routeTargetSpeed);
            creatorIndex++;
            creatorIndex = creatorIndex % (factors.length + 2);
        }
        createdSolutions++;
        if (createdSolutions == populationSize) {
            initPopulationEndTime = System.nanoTime();
        }
        initialSolutions.add(solution);
        if (initialSolutions.size() == 200) {
            System.out.println("Created solutions: " + createdSolutions);
        }
        return new RouteSolution(solution, 1, 3);
    }

    public Set<Solution> getInitialSolutions() {
        return initialSolutions;
    }
}
