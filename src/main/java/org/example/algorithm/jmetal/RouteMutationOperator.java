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
        do {
            solution = EMASSolutionGenerator.mutateSolution(routeSolution.getSolution(), mutationProbability());
            solution.calculateRouteValues();
            System.out.println("TOOOOOO DANGEROUS");
        } while (solution.isTooDangerous());
        routeSolution.setSolution(solution);
        return routeSolution;
    }

    @Override
    public double mutationProbability() {
        return simulationData.mutationProbability;
    }
}
