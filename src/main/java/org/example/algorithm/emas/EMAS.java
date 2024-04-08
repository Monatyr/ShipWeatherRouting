package org.example.algorithm.emas;

import org.example.algorithm.Algorithm;
import org.example.model.Agent;
import org.example.model.Island;
import org.example.model.Solution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EMAS extends Algorithm {
    private int populationSize = 30;
    private static final int islandsNumber = 5;
    private static final List<Island> islands = new ArrayList<>();


    public EMAS() {
        population = generateInitialPopulation();
        createIslands();
    }

    /**
     * Creates evolutionary islands with their own population and add them to the list.
     * The first island (index 0) is treated as an elite island.
     */
    private void createIslands() {
        List<Set<Agent>> agentSets = dividePopulationToSets();
        islands.add(new Island(agentSets.get(0), true));

        for (int i = 1; i < islandsNumber; i++) {
            islands.add(new Island(agentSets.get(i), false));
        }
    }

    private List<Set<Agent>> dividePopulationToSets() {
        List<Agent> allAgents = population.stream().toList();
        List<Set<Agent>> agentSets = new ArrayList<>();
        int elementsPerEachSet = population.size() / islandsNumber;
        int extraElements = population.size() % islandsNumber;

        for (int i = 0; i < islandsNumber; i++) {
            Set<Agent> agentsBatch = new HashSet<>();
            int batchStart = i < extraElements ? i * (elementsPerEachSet + 1) : i * elementsPerEachSet;
            int batchEnd = i < extraElements ? (i + 1) * (elementsPerEachSet + 1) : (i + 1) * elementsPerEachSet;

            for (int j = batchStart; j < batchEnd; j++) {
                agentsBatch.add(allAgents.get(j));
            }

            agentSets.add(agentsBatch);
        }

        return agentSets;
    }

    public List<Island> getIslands() {
        return islands;
    }

    @Override
    protected Set<Agent> generateInitialPopulation() {
        Set<Agent> agents = new HashSet<>();

        for (int i = 0; i < populationSize; i++) {
            Solution solution = EMASSolutionGenerator.generateSolution(null);
            agents.add(new Agent(solution, 50, 0));
        }

        return agents;
    }

    @Override
    protected Set<Agent> generateIterationPopulation() {
        for (int i = 0; i < populationSize-1; i++) {

        }

        /** TODO: is this a necessary function?
         * Shouldn't the agents be responsible for creating new solutions? They themselves know when and with whom they might
         * want to reproduce.
         */
    }

    @Override
    protected boolean checkStopCondition() {
        return iterations >= 10;
    }
}
