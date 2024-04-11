package org.example.model.action;

import org.example.model.Agent;

import java.util.Set;

public abstract class Action {
    protected Agent agent;

    public Action(Agent agent) {
        this.agent = agent;
        System.out.println(this.getClass().getSimpleName());
    }

    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        agent.setMadeAction(true);
    };
}
