package org.example.model.action;

import org.example.model.Agent;

public abstract class Action {
    protected Agent agent;

    public Action(Agent agent) {
        this.agent = agent;
    }

    public abstract void perform();

    public abstract boolean isPossible();
}
