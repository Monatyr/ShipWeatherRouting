package org.example.algorithm.jmetal;

import org.example.model.RoutePoint;
import org.example.util.Coordinates;
import org.example.util.UtilFunctions;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BestSolutionSelection;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.RandomSelection;
import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.EpsilonDominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.NullEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {

        int evaluations = 100;
        int populationSize = 100;
        int matingPoolSize = 100;
        int offspringSize = 100;
        boolean epsilonDominance = false;

        CrossoverOperator<RouteSolution> crossoverOperator = new RouteCrossoverOperator();
        MutationOperator<RouteSolution> mutationOperator = new RouteMutationOperator();
        SelectionOperator<List<RouteSolution>, RouteSolution> selectionOperator = new BinaryTournamentSelection<>();
        SolutionListEvaluator<RouteSolution> solutionListEvaluator = new RouteEvaluator<>();
        DominanceComparator<RouteSolution> dominanceComparator = new RouteDominanceComparator(epsilonDominance);
        DominanceComparator<RouteSolution> epsilonDominanceComparator = new EpsilonDominanceComparator<>();
        RouteProblem routeProblem = new RouteProblem();


        SPEA2<RouteSolution> algorithm = new SPEA2Builder<RouteSolution>(routeProblem, crossoverOperator, mutationOperator)
                .setMaxIterations(evaluations)
                .build();
//        NSGAII<RouteSolution> algorithm = new NSGAIIBuilder<RouteSolution>(routeProblem, crossoverOperator, mutationOperator, populationSize)
////                .setSolutionListEvaluator(new SequentialSolutionListEvaluator<>())
//                .setSelectionOperator(selectionOperator)
//                .setSolutionListEvaluator(solutionListEvaluator)
//                .setDominanceComparator(epsilonDominanceComparator)
//                .setMaxEvaluations(evaluations)
//                .build();

        // Run
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        System.out.println("Computing time: " + algorithmRunner.getComputingTime());

        // Initial population
        UtilFunctions.getBestPerCategory(routeProblem.getInitialSolutions());

        // Result population
        List<RouteSolution> population = algorithm.result();
        UtilFunctions.getBestPerCategory(population.stream().map(RouteSolution::getSolution).collect(Collectors.toSet()));

         List<Coordinates> route = population.get(0).getSolution().getRoutePoints().stream().map(RoutePoint::getCoordinates).toList();
         route.forEach(p -> System.out.println(p.longitude() + ", " + p.latitude()));
    }
}
