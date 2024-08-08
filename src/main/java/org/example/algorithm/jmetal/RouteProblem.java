package org.example.algorithm.jmetal;

import org.example.algorithm.Algorithm;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.uma.jmetal.problem.Problem;

import java.util.List;
import java.util.Random;

public class RouteProblem implements Problem<RouteSolution> {
    private Random random = new Random();
    private String[] routeFiles = {
            "src/main/resources/initial-routes/great_circle_route.txt",
            "src/main/resources/initial-routes/rhumb_line_route.txt"
    };

    public RouteProblem() {

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
        return routeSolution;
    }

    @Override
    public RouteSolution createSolution() {
        List<RoutePoint> route = EMASSolutionGenerator.getRouteFromFile(routeFiles[random.nextInt(0, 2)]);
        Solution solution = Algorithm.generateInitialSolution(route);
        return new RouteSolution(solution, 1, 3);
    }
}
