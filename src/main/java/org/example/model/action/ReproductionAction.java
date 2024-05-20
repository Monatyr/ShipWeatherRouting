package org.example.model.action;

import org.example.model.Agent;
import org.example.util.GridPoint;

import java.util.List;
import java.util.Set;

public class ReproductionAction extends Action {
    private Agent partner;
    private List<GridPoint> commonGridPoints;

    public ReproductionAction(Agent agent, Agent partner, List<GridPoint> commonGridPoints) {
        super(agent);
        this.partner = partner;
        this.commonGridPoints = commonGridPoints;
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) throws Exception {
        Agent newAgent = agent.createNewAgent(partner, commonGridPoints);
        agentsToAdd.add(newAgent);
        simulationData.populationSize++;
        super.perform(agentsToAdd, agentsToRemove);
    }
}
