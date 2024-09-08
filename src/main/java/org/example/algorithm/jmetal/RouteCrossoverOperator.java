package org.example.algorithm.jmetal;

import org.apache.commons.math3.util.Pair;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.GridPoint;
import org.example.util.SimulationData;
import org.uma.jmetal.operator.crossover.CrossoverOperator;

import java.util.ArrayList;
import java.util.List;

public class RouteCrossoverOperator implements CrossoverOperator<RouteSolution> {
    private final SimulationData simulationData = SimulationData.getInstance();

    @Override
    public double crossoverProbability() {
        return 0.5;
//        return simulationData.reproductionProbability;
    }

    @Override
    public int numberOfRequiredParents() {
        return 2;
    }

    @Override
    public int numberOfGeneratedChildren() {
        return 1;
    }

    @Override
    public List<RouteSolution> execute(List<RouteSolution> routeSolutions) {
        RouteSolution p1 = routeSolutions.get(0);
        RouteSolution p2 = routeSolutions.get(1);
        Solution newSolution;

        List<RoutePoint> routePoints = p1.getSolution().getRoutePoints();
        List<RoutePoint> partnerRoutePoints = p2.getSolution().getRoutePoints();
        List<GridPoint> commonGridPoints = new ArrayList<>();

        for (int i = 0; i < routePoints.size(); i++) {
            GridPoint currRoutePoint = routePoints.get(i).getGridCoordinates();
            GridPoint currPartnerRoutePoint = partnerRoutePoints.get(i).getGridCoordinates();
            if (currRoutePoint.equals(currPartnerRoutePoint)) {
                commonGridPoints.add(currRoutePoint);
            }
        }
        if (!commonGridPoints.isEmpty()) {
            newSolution = new Solution(p1.getSolution());
        } else {
            newSolution = EMASSolutionGenerator.crossoverSolutions(p1.getSolution(), p2.getSolution(), commonGridPoints, simulationData.routeSwitches);
        }
        return List.of(new RouteSolution(newSolution, p1.variables().size(), p1.objectives().length));
    }
}
