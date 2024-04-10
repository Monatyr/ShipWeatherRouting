package org.example.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A representation of an island in the elEMAS approach.
 */

public class Island {
    private final Set<Agent> agents = new HashSet<>();
    private boolean elite;


    public Island(boolean elite) {
        this.elite = elite;
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
}
