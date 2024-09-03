package org.example.model.action;

import org.example.model.Agent;
import org.example.model.Island;

import java.util.Set;
import java.util.stream.Collectors;

public class MigrationAction extends Action {
    private Island targetIsland;

    public MigrationAction(Agent agent, Island targetIsland) {
        super(agent);
        this.targetIsland = targetIsland;
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) throws Exception {
        agentsToRemove.add(agent);
        giveAwayEnergy();
        agent.setPreviousIsland(agent.getIsland());
        agent.setIsland(targetIsland);
        targetIsland.addAgent(agent);
        Action.actionCount.put(ActionType.Migration, Action.actionCount.get(ActionType.Migration) + 1);
        super.perform(agentsToAdd, agentsToRemove);
        if (targetIsland.isElite()) { // if an agent decides to migrate to the elite island do not count it to the total population
            simulationData.populationSize--;
            System.out.println("Elite: " + agent.getSolution().getFunctionValues());
        }
    }

    public void giveAwayEnergy() {
        Set<Agent> neighbours = agent.getIsland()
                .getAgents()
                .stream()
                .filter(other -> !agent.equals(other))
                .filter(other -> other.getEnergy() > simulationData.deathEnergyBound) // sanity check
                .collect(Collectors.toSet());
        agent.setEnergy(agent.getEnergy() - simulationData.migrationEnergyTaken);
        double energyForEach = simulationData.migrationEnergyTaken /neighbours.size();
        for (Agent neighbour : neighbours) {
            neighbour.setEnergy(neighbour.getEnergy() + energyForEach);
        }
    }
}
