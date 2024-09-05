package org.example.algorithm.jmetal;

import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.Solution;
import org.example.util.SimulationData;
import org.uma.jmetal.operator.mutation.MutationOperator;

public class RouteMutationOperator implements MutationOperator<RouteSolution> {
    @Override
    public RouteSolution execute(RouteSolution routeSolution) {
        Solution solution;
        int counter = 0;
        do {
            solution = EMASSolutionGenerator.mutateSolution(routeSolution.getSolution(), SimulationData.getInstance().mutationRate);
//            solution = EMASSolutionGenerator.mutateSolutionEta(routeSolution.getSolution(), SimulationData.getInstance().jmetalMutationEta);
            solution.calculateRouteValues();
            counter++;
        } while (solution.isTooDangerous() && counter < 10);
        if (counter < 10) {
            routeSolution.setSolution(solution);
        }
        return routeSolution;
    }

    public double mutationProbability() {
        return SimulationData.getInstance().jmetalMutationProbability;
    }
}
