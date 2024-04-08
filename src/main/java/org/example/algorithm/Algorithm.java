package org.example.algorithm;

import org.example.model.Agent;
import org.example.model.Solution;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class Algorithm {
    protected static int iterations = 0;
    protected static Set<Agent> population = null;

    public Set<Solution> run() {
        if (population == null) {
            population = generateInitialPopulation();
        }

        while (validState() && !checkStopCondition()) {
            population = generateIterationPopulation();
            iterations++;

            if (checkStopCondition()) {
                break;
            }
        }

        Set<Solution> solutions = population.stream().map(Agent::getSolution).collect(Collectors.toSet());
        return solutions;
    }

    private boolean validState() {
        return population != null;
    }

    protected boolean checkStopCondition() {
        return iterations > 5;
    }

    protected abstract Set<Agent> generateIterationPopulation();

    protected abstract Set<Agent> generateInitialPopulation();
}
