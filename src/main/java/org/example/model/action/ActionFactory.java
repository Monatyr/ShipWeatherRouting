package org.example.model.action;


import org.example.model.Agent;

public class ActionFactory {
    public static Action getAction(Agent agent, ActionType actionType) {
        switch (actionType) {
            case Reproduction:
                return new ReproductionAction(agent);
            case Migration:
                return new MigrationAction(agent);
            case Death:
                return new DeathAction(agent);
            default:
                throw new IllegalArgumentException("Unknown action type: " + actionType);
        }
    }
}
