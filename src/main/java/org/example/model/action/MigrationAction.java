package org.example.model.action;

import org.example.model.Agent;
import org.example.model.Island;

import java.util.Set;

public class MigrationAction extends Action {
    private Island targetIsland;

    public MigrationAction(Agent agent, Island targetIsland) {
        super(agent);
        this.targetIsland = targetIsland;
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        // TODO: add agent to another island (agentsToAdd is related to the current island not the destined one).
        agentsToRemove.add(agent);
        targetIsland.addAgent(agent);
        super.perform(agentsToAdd, agentsToRemove);
    }
}
