package org.example.model;

import org.example.algorithm.Algorithm;
import org.example.algorithm.emas.EMAS;
import org.example.util.SimulationData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A representation of an island in the elEMAS approach.
 */

public class Island {
    private final Set<Agent> agents = new HashSet<>();
    private final boolean elite;
    private Set<Island> neighbouringIslands;
    private final Random random = new Random();
    private final SimulationData simulationData = SimulationData.getInstance();
    private final List<List<Agent>> lattice;

    public Island(boolean elite, Set<Island> neighbouringIslands) {
        this.elite = elite;
        this.neighbouringIslands = neighbouringIslands;
        lattice = new ArrayList<>();
        for (int i = 0; i < simulationData.latticeSize; i++) {
            List<Agent> row = new ArrayList<>();
            for (int j = 0; j < simulationData.latticeSize; j++) {
                row.add(null);
            }
            lattice.add(row);
        }
    }

    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    public void addAgent(Set<Agent> agents) { this.agents.addAll(agents); }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public void removeAgent(Set<Agent> agents) { this.agents.removeAll(agents); }

    public void evaluateAgents() {
        for (Agent agent: agents) {
            List<Agent> neighbours = getAgents().stream()
                    .filter(a -> !a.equals(agent))
                    .filter(a -> a.getEnergy() > 0)
                    .toList();
            if (neighbours.size() == 0) {
                return;
            }
            int index = random.nextInt(neighbours.size());
            agent.compareTo(neighbours.get(index));
        }
    }

    public boolean isElite() {
        return elite;
    }

    public Set<Agent> getAgents() {
        return agents;
    }

    public Set<Island> getNeighbouringIslands() { return  neighbouringIslands; }

    public void setNeighbouringIslands(Set<Island> neighbouringIslands) {
        this.neighbouringIslands = neighbouringIslands;
    }
}
