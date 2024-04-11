package org.example.model.action;

import org.example.model.Agent;

public abstract class Action {
    protected Agent agent;

    public Action(Agent agent) {
        this.agent = agent;
        System.out.println(this.getClass().getSimpleName());
    }

    public abstract void perform();
}
