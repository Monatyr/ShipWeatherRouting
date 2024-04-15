package org.example.algorithm.emas;

import org.example.algorithm.Algorithm;
import org.example.model.Agent;
import org.example.model.Island;
import org.example.model.Solution;
import org.example.util.SimulationData;

import java.util.*;
import java.util.stream.Collectors;

public class EMAS extends Algorithm {
    private int populationSize = simulationData.populationSize;
    private final int islandsNumber = simulationData.numberOfIslands;
    private final List<Island> islands = new ArrayList<>();


    public EMAS() {
        createIslands(); // create empty islands
        generateInitialPopulation(); // create the initial population of agents without islands assigned
        dividePopulationBetweenIslands(); // assign agent to islands
    }

    /**
     * Creates evolutionary islands with their own population and add them to the list.
     * The first island (index 0) is treated as an elite island. Each island has its set
     * of neighbours that agents can migrate to.
     */
    private void createIslands() {
        islands.add(new Island(true, null));
        for (int i = 1; i < islandsNumber; i++) {
            islands.add(new Island(false, null));
        }

        for (Island   island : islands) {
            Set<Island> neighbouringIslands = islands.stream()
                    .filter(i -> !i.equals(island))
                    .collect(Collectors.toSet());

            island.setNeighbouringIslands(neighbouringIslands);
        }
    }

    private void dividePopulationBetweenIslands() {
        List<Agent> allAgents = population.stream().toList();
        int elementsPerEachSet = population.size() / islandsNumber;
        int extraElements = population.size() % islandsNumber;

        for (int i = 0; i < islandsNumber; i++) {
            int batchStart = i < extraElements ? i * (elementsPerEachSet + 1) : i * elementsPerEachSet;
            int batchEnd = i < extraElements ? (i + 1) * (elementsPerEachSet + 1) : (i + 1) * elementsPerEachSet;

            for (int j = batchStart; j < batchEnd; j++) {
                Island island = islands.get(i);
                Agent agent = allAgents.get(j);
                island.addAgent(agent);
                agent.setIsland(island);
            }
        }
    }

    @Override
    protected void generateInitialPopulation() {
        for (int i = 0; i < populationSize; i++) {
            Solution solution = EMASSolutionGenerator.generateSolution(null);
            population.add(new Agent(solution, simulationData.initialEnergy, 0, null));
        }
    }

    @Override
    protected void runIteration() {
        for (Island island : islands) {
            Set<Agent> agentsToAdd = new HashSet<>();
            Set<Agent> agentsToRemove = new HashSet<>();

            for (Agent agent : island.getAgents()) {
                agent.performAction(agentsToAdd, agentsToRemove);
            }

            System.out.println("TO REMOVEEEEEEEEEEEEEEEEEEEEE: " + agentsToRemove);
            island.getAgents().removeAll(agentsToRemove);
            island.getAgents().addAll(agentsToAdd);
        }
    }

    @Override
    protected boolean checkStopCondition() {
        return iterations >= 10;
    }

    public List<Island> getIslands() {
        return islands;
    }
}
