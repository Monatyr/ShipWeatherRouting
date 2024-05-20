package org.example.model.action;

import org.example.model.Agent;
import org.example.util.SimulationData;

import java.util.Set;

public abstract class Action {
    protected Agent agent;
    protected SimulationData simulationData = SimulationData.getInstance();

    public Action(Agent agent) {
        this.agent = agent;
    }

    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) throws Exception {
        agent.setMadeAction(true);
    };
}
