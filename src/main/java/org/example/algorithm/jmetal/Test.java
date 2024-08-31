package org.example.algorithm.jmetal;

import org.example.algorithm.Algorithm;
import org.example.model.Agent;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.SimulationData;
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
import org.uma.jmetal.problem.Problem;

import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;
import org.uma.jmetal.util.comparator.dominanceComparator.impl.EpsilonDominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.example.Main.runPythonScript;
import static org.example.util.UtilFunctions.saveToJson;

public class Test {
    public static void main(String[] args) {

        int evaluations = 100000;
        int populationSize = 100;
        int matingPoolSize = 100; // By default, matingPoolSize and offspringSize are to populationSize by the NSGAII-Builder
        int offspringSize = 100;
        double mutationProbability = SimulationData.getInstance().mutationProbability;
        double epsilon = 0.0;

        CrossoverOperator<RouteSolution> crossoverOperator = new RouteCrossoverOperator();
        MutationOperator<RouteSolution> mutationOperator = new RouteMutationOperator();
        RouteProblem routeProblem = new RouteProblem();

        NSGAII<RouteSolution> algorithm = new NSGAIIBuilder<>(routeProblem, crossoverOperator, mutationOperator, populationSize)
                .setMaxEvaluations(evaluations)
                .setMatingPoolSize(matingPoolSize)
                .setOffspringPopulationSize(offspringSize)
                .build();

        // Run
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
        System.out.println("Computing time: " + (double) algorithmRunner.getComputingTime() / 1000 + "s");

        // Initial population
        UtilFunctions.getBestPerCategory(routeProblem.getInitialSolutions());

        // Result population
        List<RouteSolution> population = algorithm.result();
        UtilFunctions.getBestPerCategory(population.stream().map(RouteSolution::getSolution).collect(Collectors.toSet()));


        Set<Solution> resultSolutions = Algorithm.getNonDominatedSolutions(population.stream().map(RouteSolution::getSolution).toList());
        System.out.println(resultSolutions.size());
        saveToJson(resultSolutions, "results/comparisonSolutions.json");
        runPythonScript("scripts/plot_pareto_front.py", List.of("--routes", "results/comparisonSolutions.json", "--resultFile", "pareto_front.png"));
//         List<Coordinates> route = population.get(0).getSolution().getRoutePoints().stream().map(RoutePoint::getCoordinates).toList();
//         route.forEach(p -> System.out.println(p.longitude() + ", " + p.latitude()));
    }
}
