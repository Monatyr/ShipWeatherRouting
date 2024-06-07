package org.example.algorithm.emas;

import org.example.algorithm.Algorithm;
import org.example.model.Agent;
import org.example.model.Island;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.model.action.Action;
import org.example.model.action.ActionType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Exemplary ships:
 *         - [1] http://www.simman2008.dk/kvlcc/kvlcc2/kvlcc2_geometry.html a theoretical VLCC tanker model for testing purposes developed by the Maritime and Ocean Engineering Research Institute
 *         - [2] https://en.wikipedia.org/wiki/Batillus-class_supertanker
 *         - [3] tanker-data.pdf int the physical-model directory.
 */

public class EMAS extends Algorithm {
    private final int islandsNumber = simulationData.numberOfIslands;
    private final List<Island> islands = new ArrayList<>();


    public EMAS() {
        createIslands(); // create empty islands
        generateInitialPopulation(); // create the initial population of agents without islands assigned
        dividePopulationBetweenIslandsWithoutElite(); // assign agent to islands
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

    private void dividePopulationBetweenIslandsWithoutElite() {
        List<Agent> allAgents = population.stream().toList();
        for (int i = 0; i < allAgents.size(); i++) {
            Island island = islands.get(i % (islandsNumber - 1) + 1);
            Agent agent = allAgents.get(i);
            island.addAgent(agent);
            agent.setIsland(island);
        }
    }

    @Override
    protected void generateInitialPopulation() {
        List<List<RoutePoint>> startingRoutes = new ArrayList<>();
        startingRoutes.add(EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/great_circle_route.txt"));
        startingRoutes.add(EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/rhumb_line_route.txt"));
        for (int i = 0; i < simulationData.populationSize; i++) {
            List<RoutePoint> route = startingRoutes.get(i % startingRoutes.size());
            Solution solution = EMASSolutionGenerator.generateSolution(route);
            solution = EMASSolutionGenerator.mutateSolution(solution, simulationData.mutationRate);
            population.add(new Agent(solution, simulationData.initialEnergy, 0, null, false));
        }
    }

    @Override
    protected void runIteration() throws Exception {
        for (Island island : islands) {
            Set<Agent> agentsToAdd = new HashSet<>();
            Set<Agent> agentsToRemove = new HashSet<>();

            for (Agent agent : island.getAgents()) {
                /* If agent did not make an action this iteration (e.g. took part in
                reproduction or migrated to a subsequent island */
                if (!agent.getMadeAction()) {
                    agent.performAction(agentsToAdd, agentsToRemove);
                }
            }
            // delete agents that died and emigrated and add those that immigrated
            island.removeAgent(agentsToRemove);
            island.addAgent(agentsToAdd);
        }
        // Make agent available to perform actions again
        for (Island island: islands) {
            for (Agent agent: island.getAgents()) {
                agent.setMadeAction(false);
            }
        }
        // Compare agents - energy distribution, prestige gains
        evaluateAgents();
        pruneDominatedEliteAgents();
        population = islands.stream()
                .flatMap(island -> island.getAgents().stream())
                .collect(Collectors.toSet());

        if (simulationData.populationSize != population.size()) {
            String err = "\nITER: " + iterations + "\nSIM-DATA-POP: " + simulationData.populationSize + "\nPOP: " + population.size();
            throw new Exception(err);
        }

    }

    @Override
    protected boolean checkStopCondition() {
        return iterations >= simulationData.maxIterations;
    }

    private Set<Agent> getNonDominatedAgents(Set<Agent> agents) {
        List<Agent> agentsList = agents.stream().toList();
        Set<Agent> nonDominatedAgents = new HashSet<>();
        for (int i = 0; i < agentsList.size(); i++) {
            boolean dominated = false;
            Agent currAgent = agentsList.get(i);
            for (int j = 0; j < agentsList.size(); j++) {
                if (i == j) {
                    continue;
                }
                Agent otherAgent = agentsList.get(j);
                if (otherAgent.getSolution().checkIfDominates(currAgent.getSolution()) == 1) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                nonDominatedAgents.add(currAgent);
            }
        }
        return nonDominatedAgents;
    }

    private void pruneDominatedEliteAgents() {
        Set<Agent> eliteAgents = islands.get(0).getAgents();
        Set<Agent> nonDominatedAgents = getNonDominatedAgents(eliteAgents);
        Set<Agent> dominatedAgents = new HashSet<>(eliteAgents);
        dominatedAgents.removeAll(nonDominatedAgents);
        islands.get(0).setAgents(nonDominatedAgents);
        double energyToDistribute = dominatedAgents.stream().map(Agent::getEnergy).reduce(0.0, Double::sum);
        redistributeEnergyToAgents(energyToDistribute, false);
        simulationData.populationSize -= dominatedAgents.size();
    }

    private void evaluateAgents() {
        for (Island island: islands) {
            island.evaluateAgents();
        }
    }

    private void redistributeEnergyToAgents(double energy, boolean includeElite) {
        List<Island> targetIslands;
        if (includeElite) {
            targetIslands = islands;
        } else {
            targetIslands = islands.stream().filter(i -> !i.isElite()).toList();
        }
        Set<Agent> nonEliteAgents = targetIslands.stream()
                .flatMap(i -> i.getAgents().stream())
                .filter(a -> a.getEnergy() > simulationData.deathEnergyBound)
                .collect(Collectors.toSet());
        double energyPerAgent = energy / nonEliteAgents.size();
        for (Agent agent : nonEliteAgents) {
            agent.setEnergy(agent.getEnergy() + energyPerAgent);
        }
    }

    public List<Island> getIslands() {
        return islands;
    }

    public Set<Agent> getPopulation() { return population; }
}
