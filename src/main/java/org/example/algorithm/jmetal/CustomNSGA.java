package org.example.algorithm.jmetal;

import org.example.model.OptimizedFunction;
import org.example.model.Solution;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.*;
import java.util.stream.Stream;

public class CustomNSGA<S> extends NSGAII {

    public CustomNSGA(Problem problem, int maxEvaluations, int populationSize, int matingPoolSize, int offspringPopulationSize, CrossoverOperator crossoverOperator, MutationOperator mutationOperator, SelectionOperator selectionOperator, SolutionListEvaluator evaluator, DominanceComparator dominanceComparator) {
        super(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator);
        this.dominanceComparator = dominanceComparator;
    }

    @Override
    public void run() {
        this.population = this.createInitialPopulation();
        this.population = this.evaluatePopulation(this.population);
        this.normalizeFunctions(this.population);
        this.initProgress();

        while(!this.isStoppingConditionReached()) {
            List<S> matingPopulation = this.selection(this.population);
            List<S> offspringPopulation = this.reproduction(matingPopulation);
            offspringPopulation = this.evaluatePopulation(offspringPopulation);

            List<RouteSolution> newList = Stream.concat(this.population.stream(), offspringPopulation.stream()).toList();
            this.normalizeFunctions(newList);
            this.population = this.replacement(this.population, offspringPopulation);
            this.updateProgress();
        }
    }

    private void normalizeFunctions(List<RouteSolution> population) {
        OptimizedFunction[] functions = {OptimizedFunction.TravelTime, OptimizedFunction.FuelUsed, OptimizedFunction.Danger};
        int numObjectives = population.get(0).getSolution().getFunctionValues().size();

        // Calculate means and standard deviations
        double[] means = new double[numObjectives];
        double[] stdDevs = new double[numObjectives];

        double[] sums = new double[numObjectives];
        double[] sumsOfSquares = new double[numObjectives];

        double[] z_mins = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] z_maxs = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

        // Compute sums for mean and variance
        for (RouteSolution solution : population) {
            for (int i = 0; i < numObjectives; i++) {
                OptimizedFunction function = functions[i];
                double value = solution.getSolution().getFunctionValues().get(function);
                sums[i] += value;
                sumsOfSquares[i] += value * value;
            }
        }

        // Calculate means
        for (int i = 0; i < numObjectives; i++) {
            means[i] = sums[i] / population.size();
        }

        // Calculate standard deviations
        for (int i = 0; i < numObjectives; i++) {
            double variance = (sumsOfSquares[i] / population.size()) - (means[i] * means[i]);
            stdDevs[i] = Math.sqrt(variance);
        }

        // Find min/max z values
        for (RouteSolution solution : population) {
            for (int i = 0; i < numObjectives; i++) {
                OptimizedFunction function = functions[i];
                double value = solution.getSolution().getFunctionValues().get(function);
                double normalizedValue = stdDevs[i] != 0 ? (value - means[i]) / stdDevs[i] : 0;
                if (normalizedValue < z_mins[i]) {
                    z_mins[i] = normalizedValue;
                }
                if (normalizedValue > z_maxs[i]) {
                    z_maxs[i] = normalizedValue;
                }
            }
        }
        // Normalize objectives
        for (RouteSolution solution : population) {
            for (int i = 0; i < numObjectives; i++) {
                OptimizedFunction function = functions[i];
                double value = solution.getSolution().getFunctionValues().get(function);
                double normalizedValue = stdDevs[i] != 0 ? (value - means[i]) / stdDevs[i] : 0;
                double newValue = (normalizedValue - z_mins[i]) / (z_maxs[i] - z_mins[i]);
                solution.setObjective(i, newValue);
            }
        }
    }
}
