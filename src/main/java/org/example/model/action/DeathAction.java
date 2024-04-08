package org.example.model.action;


import org.example.model.Agent;

public class DeathAction extends Action{
    public DeathAction(Agent agent) {
        super(agent);
    }

    @Override
    public void perform() {

    }

    @Override
    public boolean isPossible() {
        return agent.getEnergy() <= 0;
    }
}
