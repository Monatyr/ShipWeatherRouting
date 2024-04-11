package org.example.model.action;


import org.example.model.Agent;

import java.util.Set;

public class ReproductionAction extends Action {
    private Agent partner;

    public ReproductionAction(Agent agent, Agent partner) {
        super(agent);
        this.partner = partner;
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        Agent newAgent = agent.createNewAgent(partner);
        agentsToAdd.add(newAgent);
        super.perform( agentsToAdd, agentsToRemove);
    }
}
