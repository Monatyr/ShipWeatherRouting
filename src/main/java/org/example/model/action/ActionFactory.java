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
        Action action;

        if (agent.getEnergy() <= 0) {
            actionType = ActionType.Death;
        } else {
            actionType = (ActionType) enumeratedDistribution.sample();

        }

        switch (actionType) {
            case Reproduction:
                if (agent.getEnergy() >= simulationData.reproductionEnergy) {
                     Agent partner = agent.getPartner();
                     if (partner != null) {
                         return new ReproductionAction(agent, partner);
                     }
                }
            case Migration:
                // TODO: should migration require energy - it should, but the question is should it decrease it?
                if (agent.getEnergy() >= simulationData.migrationEnergy) {
                    return new MigrationAction(agent);
                }
            case Death:
                return new DeathAction(agent);
            default:
                return null;
        }
    }
}
