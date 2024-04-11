package org.example.model.action;

import org.example.model.Agent;

import java.util.Set;

public class MigrationAction extends Action {
    public MigrationAction(Agent agent) {
        super(agent);
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        // TODO: add agent to another island (agentsToAdd is related to the current island not the destined one).
        agentsToRemove.add(agent);
        super.perform(agentsToAdd, agentsToRemove);
    }
}
