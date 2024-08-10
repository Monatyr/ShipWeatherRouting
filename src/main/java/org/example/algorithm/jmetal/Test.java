package org.example.algorithm.jmetal;

import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.SimulationData;
import org.example.util.UtilFunctions;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.EpsilonDominanceComparator;

import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {

        int evaluations = 100;
        int populationSize = 100;
        int matingPoolSize = 100;
        int offspringSize = 100;
        boolean epsilonDominance = true;

        CrossoverOperator<RouteSolution> crossoverOperator = new RouteCrossoverOperator();
        MutationOperator<RouteSolution> mutationOperator = new RouteMutationOperator();
        SelectionOperator<List<RouteSolution>, RouteSolution> selectionOperator = new BinaryTournamentSelection<>();
        DominanceComparator<RouteSolution> dominanceComparator = new RouteDominanceComparator(epsilonDominance);
        DominanceComparator<RouteSolution> epsilonDominanceComparator = new EpsilonDominanceComparator<>(0.01);
        RouteProblem routeProblem = new RouteProblem();

        NSGAII<RouteSolution> algorithm = new NSGAIIBuilder<RouteSolution>(routeProblem, crossoverOperator, mutationOperator, populationSize)
                .setSelectionOperator(selectionOperator)
                .setDominanceComparator(epsilonDominanceComparator)
                .build();

        // Run
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        System.out.println("Computing time: " + algorithmRunner.getComputingTime());

        // Initial population
        UtilFunctions.getBestPerCategory(routeProblem.getInitialSolutions());

        // Result population
        List<RouteSolution> population = algorithm.getPopulation();
        UtilFunctions.getBestPerCategory(population.stream().map(RouteSolution::getSolution).collect(Collectors.toSet()));

         List<Coordinates> route = population.get(0).getSolution().getRoutePoints().stream().map(RoutePoint::getCoordinates).toList();
         route.forEach(p -> System.out.println(p.longitude() + ", " + p.latitude()));
    }
}
