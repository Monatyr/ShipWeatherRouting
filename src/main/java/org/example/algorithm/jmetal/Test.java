package org.example.algorithm.jmetal;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;

import java.util.List;

public class Test {
    public static void main(String[] args) {

        int evaluations = 100;
        int populationSize = 100;
        int matingPoolSize = 100;
        int offspringSize = 100;

//        CrossoverOperator<Solution<org.example.model.Solution>> crossoverOperator = new RouteCrossoverOperator();
//        MutationOperator<Solution<org.example.model.Solution>> mutationOperator = new RouteMutationOperator();
//        SelectionOperator<List<Solution<org.example.model.Solution>>, Solution<org.example.model.Solution>> selectionOperator = new BinaryTournamentSelection<>();
//        Problem<Solution<org.example.model.Solution>> routeProblem = new RouteProblem();
//        NSGAII<Solution<org.example.model.Solution>> nsga = new NSGAIIBuilder<Solution<org.example.model.Solution>>(routeProblem, crossoverOperator, mutationOperator, population)
//                .setSelectionOperator(selectionOperator)
//                .build();

        CrossoverOperator<RouteSolution> crossoverOperator = new RouteCrossoverOperator();
        MutationOperator<RouteSolution> mutationOperator = new RouteMutationOperator();
        SelectionOperator<List<RouteSolution>, RouteSolution> selectionOperator = new BinaryTournamentSelection<>();
        Problem<RouteSolution> routeProblem = new RouteProblem();

        NSGAII<RouteSolution> algorithm = new NSGAIIBuilder<RouteSolution>(routeProblem, crossoverOperator, mutationOperator, populationSize)
                .setSelectionOperator(selectionOperator)
                .build();

        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        List<RouteSolution> population = algorithm.getPopulation();
    }
}
