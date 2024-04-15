package org.example.model;

import org.example.algorithm.Algorithm;
import org.example.algorithm.emas.EMAS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A representation of an island in the elEMAS approach.
 */

public class Island {
    private final Set<Agent> agents = new HashSet<>();
    private boolean elite;
    private Set<Island> neighbouringIslands;

    public Island(boolean elite, Set<Island> neighbouringIslands) {
        this.elite = elite;
        this.neighbouringIslands = neighbouringIslands;
    }

    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
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
