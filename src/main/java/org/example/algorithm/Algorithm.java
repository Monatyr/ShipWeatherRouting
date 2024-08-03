package org.example.algorithm;

import org.example.model.Agent;
import org.example.model.Solution;
import org.example.util.SimulationData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Algorithm {
    protected static SimulationData simulationData = SimulationData.getInstance();
    protected static int iterations = 0;
    protected static Set<Agent> population = new HashSet<>();

    public Set<Solution> run() throws Exception {
        if (population.isEmpty()) {
            generateInitialPopulation();
        }

        while (validState() && !checkStopCondition()) {
            runIteration();
            iterations++;
            if (iterations % 500 == 0) {
                System.out.println("Iteration: " + iterations + (iterations < 10000 ? "\t" : "" ) + "\tPopulation: " + population.size() + "\t\tElite: " + population.stream().filter(o -> o.getIsland().isElite()).toList().size() + "\tEpsilon: " + simulationData.paretoEpsilon);
                System.out.println("Below: " + Solution.below + "\tAbove: " + Solution.above);
                System.out.println("Avg engine load: " + Solution.fullNodePower / Solution.nodeNumber / SimulationData.getInstance().maxOutput + "\tAvg speed: " + Solution.fullNodeSpeed / Solution.nodeNumber);
            }
        }

        Set<Solution> solutions = population.stream().map(Agent::getSolution).collect(Collectors.toSet());
        System.out.println("\n\nSOLUTIONS: " + solutions.size());
        Set<Solution> nonDominatedSolutions = getNonDominatedSolutions();
        System.out.println("NONDOMINATED SOLUTIONS: " + nonDominatedSolutions.size());
        return nonDominatedSolutions;
    }

    private boolean validState() {
        return population != null;
    }

    protected boolean checkStopCondition() {
        return iterations > simulationData.maxIterations;
    }

    protected Set<Solution> getNonDominatedSolutions() {
        List<Solution> solutions = population.stream().map(Agent::getSolution).toList();
        Set<Solution> nonDominatedSolutions = new HashSet<>();
        for (int i = 0; i < solutions.size(); i++) {
            Solution currSolution = solutions.get(i);
            boolean dominated = false;
            for (int j = 0; j < solutions.size(); j++) {
                if (i == j) {
                    continue;
                }
                Solution otherSolution = solutions.get(j);
                if (otherSolution.checkIfDominates(currSolution, false) == 1) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                nonDominatedSolutions.add(currSolution);
            }
        }
        return nonDominatedSolutions;
    }

    protected abstract void generateInitialPopulation();

    protected abstract void runIteration() throws Exception;
}
