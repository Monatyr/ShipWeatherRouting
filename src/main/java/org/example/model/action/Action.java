package org.example.model.action;

import org.example.model.Agent;
import org.example.util.SimulationData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public abstract class Action {
    protected Agent agent;
    protected SimulationData simulationData = SimulationData.getInstance();
    public static Map<ActionType, Integer> actionCount = new HashMap<>(
            Map.ofEntries(
                entry(ActionType.Death, 0),
                entry(ActionType.Idle, 0),
                entry(ActionType.Migration, 0),
                entry(ActionType.Reproduction, 0)
            )
    );

    public Action(Agent agent) {
        this.agent = agent;
    }

    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) throws Exception {
        agent.setMadeAction(true);
    };
}
