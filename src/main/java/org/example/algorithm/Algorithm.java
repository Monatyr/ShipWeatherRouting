package org.example.algorithm;

import org.example.model.Agent;
import org.example.model.Solution;
import org.example.util.SimulationData;

import java.util.HashSet;
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
        }


        Set<Solution> solutions = population.stream().map(Agent::getSolution).collect(Collectors.toSet());
        return solutions;
    }

    private boolean validState() {
        return population != null;
    }

    protected boolean checkStopCondition() {
        return iterations > simulationData.maxIterations;
    }

    protected abstract void generateInitialPopulation();

    protected abstract void runIteration() throws Exception;
}
