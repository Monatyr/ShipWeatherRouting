package org.example.model.action;


import org.example.model.Agent;

import java.util.Set;

public class DeathAction extends Action{
    public DeathAction(Agent agent) {
        super(agent);
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        agent.die();
        super.perform( agentsToAdd, agentsToRemove);
    }
}
