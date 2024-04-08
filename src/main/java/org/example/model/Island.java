package org.example.model;

import java.util.List;
import java.util.Set;

/**
 * A representation of an island in the elEMAS approach.
 */

public class Island {
    private Set<Agent> agents;
    private boolean elite;


    public Island(Set<Agent> agents, boolean elite) {
        this.agents = agents;
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
