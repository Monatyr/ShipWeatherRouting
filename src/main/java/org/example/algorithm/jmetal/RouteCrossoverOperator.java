package org.example.algorithm.jmetal;

import org.example.util.SimulationData;
import org.uma.jmetal.operator.crossover.CrossoverOperator;

import java.util.List;

public class RouteCrossoverOperator implements CrossoverOperator<RouteSolution> {
    private SimulationData simulationData = SimulationData.getInstance();

    @Override
    public double crossoverProbability() {
        return simulationData.reproductionProbability;
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

        
    }
}
