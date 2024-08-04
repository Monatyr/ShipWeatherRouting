package org.example.util;

import org.example.model.Agent;
import org.example.model.Island;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class UtilFunctions {
    private static SimulationData simulationData = SimulationData.getInstance();

    public static void redistributeEnergyLeft(Agent agent, Island island) {
        Set<Agent> neighbours = island
                .getAgents()
                .stream()
                .filter(other -> !agent.equals(other))
                .filter(other -> other.getEnergy() > simulationData.deathEnergyBound) // sanity check
                .collect(Collectors.toSet());
        if (neighbours.size() == 0) {
            neighbours = island.getNeighbouringIslands()
                    .stream()
                    .filter(neighborIsland -> !neighborIsland.isElite())
                    .map(Island::getAgents)
                    .flatMap(Set::stream)
                    .filter(other -> !agent.equals(other))
                    .filter(other -> other.getEnergy() > simulationData.deathEnergyBound)
                    .collect(Collectors.toSet());
        }

        double energyForEach = agent.getEnergy()/neighbours.size();

        for (Agent neighbour : neighbours) {
            neighbour.setEnergy(neighbour.getEnergy() + energyForEach);
        }
        agent.setEnergy(0.0);
    }
}
