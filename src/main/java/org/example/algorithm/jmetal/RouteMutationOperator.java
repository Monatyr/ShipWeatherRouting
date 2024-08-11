package org.example.algorithm.jmetal;

import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.Solution;
import org.example.util.SimulationData;
import org.uma.jmetal.operator.mutation.MutationOperator;

public class RouteMutationOperator implements MutationOperator<RouteSolution> {
    SimulationData simulationData = SimulationData.getInstance();

    @Override
    public RouteSolution execute(RouteSolution routeSolution) {
        Solution solution;
        int counter = 0;
        do {
//            solution = EMASSolutionGenerator.mutateSolution(routeSolution.getSolution(), mutationProbability());
            solution = EMASSolutionGenerator.mutateSolution(routeSolution.getSolution(), simulationData.mutationRate);
            solution.calculateRouteValues();
            counter++;
        } while (solution.isTooDangerous() && counter < 10);
        if (counter < 10) {
            routeSolution.setSolution(solution);
        }
        return routeSolution;
    }

    @Override
    public double mutationProbability() {
        return 1;
//        return simulationData.mutationProbability;
    }
}
