package org.example.model.action;


import org.example.model.Agent;

public class ReproductionAction extends Action {
    public ReproductionAction(Agent agent) {
        super(agent);
    }

    @Override
    public void perform() {

    }

    @Override
    public boolean isPossible() {
        return false;
    }
}
