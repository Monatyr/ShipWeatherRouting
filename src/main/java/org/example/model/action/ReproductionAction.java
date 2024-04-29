package org.example.model.action;

import org.example.model.Agent;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

public class ReproductionAction extends Action {
    private Agent partner;
    private List<Point2D> commonGridPoints;

    public ReproductionAction(Agent agent, Agent partner, List<Point2D> commonGridPoints) {
        super(agent);
        this.partner = partner;
        this.commonGridPoints = commonGridPoints;
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        Agent newAgent = agent.createNewAgent(partner, commonGridPoints);
        agentsToAdd.add(newAgent);
        simulationData.populationSize++;
        super.perform(agentsToAdd, agentsToRemove);
    }
}
