package org.example.model.action;


import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.example.model.Agent;
import org.example.util.SimulationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionFactory {
    public static SimulationData simulationData = SimulationData.getInstance();
    private static List<Pair<ActionType, Double>> itemWeights = List.of(
            new Pair(ActionType.Migration, simulationData.migrationProbability),
            new Pair(ActionType.Reproduction, simulationData.reproductionProbability)
    );
    private static EnumeratedDistribution enumeratedDistribution = new EnumeratedDistribution<>(itemWeights);

    public static Action getAction(Agent agent) {
        ActionType actionType;

        if (agent.getEnergy() <= 0) {
            actionType = ActionType.Death;
        } else {
            actionType = (ActionType) enumeratedDistribution.sample();

        }

        // TODO: rethink how to select actions. Should the condition be checked while creating an action?
        switch (actionType) {
            case Reproduction:
                System.out.println("REPRO");
                return new ReproductionAction(agent);
            case Migration:
                System.out.println("MIGRA");

                return new MigrationAction(agent);
            case Death:
                System.out.printf("DEATH");
                return new DeathAction(agent);
            default:
                System.out.println("NOTHING");
        }
    }
}
