package org.example.algorithm.emas;

import org.example.algorithm.Algorithm;
import org.example.model.*;
import org.example.util.UtilFunctions;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.util.UtilFunctions.redistributeEnergyLeft;

/**
 * Exemplary ships:
 *         - [1] http://www.simman2008.dk/kvlcc/kvlcc2/kvlcc2_geometry.html a theoretical VLCC tanker model for testing purposes developed by the Maritime and Ocean Engineering Research Institute
 *         - [2] https://en.wikipedia.org/wiki/Batillus-class_supertanker
 *         - [3] tanker-data.pdf int the physical-model directory.
 */

public class EMAS extends Algorithm {
    private final int islandsNumber = simulationData.numberOfIslands;
    private final List<Island> islands = new ArrayList<>();
    private final static Random random = new Random();

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
//        for (int i = 0; i < allAgents.size(); i++) {
//            Island island = islands.get(i % (islandsNumber - 1) + 1);
//            Agent agent = allAgents.get(i);
//            island.addAgent(agent);
//            agent.setIsland(island);
//        }
        int agentIndex = 0;
        int batchSize = (int) ((double) population.size() / (islandsNumber - 1));
        for (int i = 1; i < islandsNumber; i++) {
            Island island = islands.get(i);
            if (i == islandsNumber - 1) {
                batchSize = population.size() - (i - 1) * batchSize;
            }
            for (int j = 0; j < batchSize; j++) {
                Agent agent = allAgents.get(agentIndex);
                island.addAgent(agent);
                agent.setIsland(island);
                agentIndex++;
                System.out.println(agentIndex + " " + island);
            }
        }
    }

    @Override
    protected boolean checkStopCondition() {
        boolean emptyAgentList = islands.stream()
                .filter(i -> !i.isElite())
                .map(Island::getAgents)
                .flatMap(Set::stream)
                .collect(Collectors.toSet()).size() == 0;
        boolean iterationsLimit = iterations > simulationData.maxIterations;
        return iterationsLimit || emptyAgentList;
    }

    @Override
    protected void runIteration() throws Exception {
        for (Island island : islands) {
            Set<Agent> agentsToAdd = new HashSet<>();
            Set<Agent> agentsToRemove = new HashSet<>();

            // shuffle agents to further randomize the evolution process
            List<Agent> shuffledAgents = new ArrayList<>(island.getAgents().stream().toList());
            Collections.shuffle(shuffledAgents);

            for (Agent agent : shuffledAgents) {
                /* If agent did not make an action this iteration (e.g. took part in
                reproduction or migrated to a subsequent island */
                if (!agent.getMadeAction()) {
                    agent.setAge(agent.getAge() + 1);
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

//        updateCrowdingDistance();

        // Compare agents - energy distribution, prestige gains
        evaluateAgents();
        pruneDominatedEliteAgents();
        population = islands.stream()
                .flatMap(island -> island.getAgents().stream())
                .collect(Collectors.toSet());

        long nonElitePopulationSize = population.stream()
                .filter(agent -> !agent.getIsland().isElite())
                .count();

        if (simulationData.populationSize != nonElitePopulationSize) {
            String err = "\nITER: " + iterations + "\nSIM-DATA-POP: " + simulationData.populationSize + "\nPOP: " + nonElitePopulationSize;
            throw new Exception(err);
        }
        simulationData.paretoEpsilon -= 0.000001;
    }

    private void updateCrowdingDistance() {
        List<Agent> agents = islands.stream().filter(i -> !i.isElite()).map(Island::getAgents).flatMap(Set::stream).collect(Collectors.toList());
        agents.forEach(agent -> agent.setCrowdingFactor(0));
        List<Agent> timeSorted = sortByFunction(agents, OptimizedFunction.TravelTime);
        List<Agent> fuelSorted = sortByFunction(agents, OptimizedFunction.FuelUsed);
        List<Agent> safetySorted = sortByFunction(agents, OptimizedFunction.Danger);

        if (agents.size() == 0) {
            System.out.println("EMPTY");
            return;
        }
        timeSorted.get(0).setCrowdingFactor(Double.POSITIVE_INFINITY);
        timeSorted.get(agents.size() - 1).setCrowdingFactor(Double.POSITIVE_INFINITY);
        fuelSorted.get(0).setCrowdingFactor(Double.POSITIVE_INFINITY);
        fuelSorted.get(agents.size() - 1).setCrowdingFactor(Double.POSITIVE_INFINITY);
        safetySorted.get(0).setCrowdingFactor(Double.POSITIVE_INFINITY);
        safetySorted.get(agents.size() - 1).setCrowdingFactor(Double.POSITIVE_INFINITY);

        calculateCrowdingDistanceByFunction(timeSorted, OptimizedFunction.TravelTime);
        calculateCrowdingDistanceByFunction(fuelSorted, OptimizedFunction.FuelUsed);
        calculateCrowdingDistanceByFunction(safetySorted, OptimizedFunction.Danger);

//        System.out.println(agents.get(0).getCrowdingFactor());
    }

    private List<Agent> sortByFunction(List<Agent> agents, OptimizedFunction function) {
        return agents.stream().sorted(Comparator.comparing(a -> a.getSolution().getFunctionValues().get(function))).toList();
    }

    private void calculateCrowdingDistanceByFunction(List<Agent> agents, OptimizedFunction function) {
        double minValue = agents.get(0).getSolution().getFunctionValues().get(function);
        double maxValue = agents.get(agents.size() - 1).getSolution().getFunctionValues().get(function);

        for (int i = 1; i < agents.size() - 1; i++) {
            Agent currAgent = agents.get(i);
            double currValue = currAgent.getSolution().getFunctionValues().get(function);
            double nextValue = agents.get(i+1).getSolution().getFunctionValues().get(function);
            double prevValue = agents.get(i-1).getSolution().getFunctionValues().get(function);
            double crowdingFactorConstituent = (nextValue - prevValue) / (maxValue - minValue);
            currAgent.setCrowdingFactor(currAgent.getCrowdingFactor() + crowdingFactorConstituent);
        }
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
                if (otherAgent.getSolution().checkIfDominates(currAgent.getSolution(), false) == 1) {
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

//    private void pruneDangerousAgents() {
//        List<Island> nonEliteIslands = islands.stream()
//                .filter(i -> !i.isElite())
//                .toList();
//        for (Island island : nonEliteIslands) { // elite agents check the danger level of the new solution themselves
//            Set<Agent> dangerousAgents = island.getAgents().stream()
//                    .filter(agent -> agent.getSolution().isTooDangerous())
//                    .collect(Collectors.toSet());
//            for (Agent agent : dangerousAgents) {
//                redistributeEnergyLeft(agent, island);
//            }
//        }
//    }

    private void pruneDominatedEliteAgents() {
        Set<Agent> eliteAgents = islands.get(0).getAgents();
        Set<Agent> nonDominatedAgents = getNonDominatedAgents(eliteAgents);
        Set<Agent> dominatedAgents = new HashSet<>(eliteAgents);
        dominatedAgents.removeAll(nonDominatedAgents);
        islands.get(0).setAgents(nonDominatedAgents); // 0 is the elite island
        for (Agent agent : dominatedAgents) {
            redistributeEnergyLeft(agent, agent.getPreviousIsland());
        }
    }

    private void evaluateAgents() {
        List<Island> nonEliteIslands = islands.stream() // agents on the elite island do not compare themselves to each other
                .filter(i -> !i.isElite())
                .toList();
        for (Island island: nonEliteIslands) {
            island.evaluateAgents();
        }
    }

    public List<Island> getIslands() {
        return islands;
    }

    public Set<Agent> getPopulation() { return population; }
}
