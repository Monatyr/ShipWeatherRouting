package org.example.model.action;


import org.example.model.Agent;

import java.util.Set;
import java.util.stream.Collectors;

public class DeathAction extends Action{
    public DeathAction(Agent agent) {
        super(agent);
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        agentsToRemove.add(agent);
        redistributeEnergyLeft(); // TODO: make sure it does not mess up the logic (aren't some of the neighbours already dead?)
        super.perform(agentsToAdd, agentsToRemove);
    }

    public void redistributeEnergyLeft() {
        Set<Agent> neighbours = agent.getIsland()
                .getAgents()
                .stream()
                .filter(other -> !agent.equals(other))
                .collect(Collectors.toSet());

        double energyForEach = agent.getEnergy()/neighbours.size();

        for (Agent neighbour : neighbours) {
            neighbour.setEnergy(neighbour.getEnergy() + energyForEach);
        }
    }
}
