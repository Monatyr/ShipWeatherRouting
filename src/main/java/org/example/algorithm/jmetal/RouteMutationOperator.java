package org.example.algorithm.jmetal;

import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.Solution;
import org.uma.jmetal.operator.mutation.MutationOperator;

public record RouteMutationOperator(double mutationProbability) implements MutationOperator<RouteSolution> {
    @Override
    public RouteSolution execute(RouteSolution routeSolution) {
        Solution solution;
        int counter = 0;
        do {
            solution = EMASSolutionGenerator.mutateSolution(routeSolution.getSolution(), mutationProbability());
            solution.calculateRouteValues();
            counter++;
        } while (solution.isTooDangerous() && counter < 10);
        if (counter < 10) {
            routeSolution.setSolution(solution);
        }
        return routeSolution;
    }
}
