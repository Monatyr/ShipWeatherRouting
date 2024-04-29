package org.example.model.action;


import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.example.model.Agent;
import org.example.model.Island;
import org.example.util.SimulationData;

import java.awt.geom.Point2D;
import java.util.List;

public class ActionFactory {
    public static SimulationData simulationData = SimulationData.getInstance();
    private static List<Pair<ActionType, Double>> itemWeights = List.of(
            new Pair(ActionType.Migration, simulationData.migrationProbability),
            new Pair(ActionType.Reproduction, simulationData.reproductionProbability)
    );
    private static EnumeratedDistribution enumeratedDistribution = new EnumeratedDistribution<>(itemWeights);

    public static Action getAction(Agent agent) {
        ActionType actionType;

        if (agent.getEnergy() < simulationData.deathEnergyBound) {
            actionType = ActionType.Death;
        } else {
            actionType = (ActionType) enumeratedDistribution.sample();
        }

        switch (actionType) {
            case Reproduction:
                if (agent.getEnergy() >= simulationData.reproductionEnergyBound && simulationData.populationSize < simulationData.maxPopulation) {
                     Pair<Agent, List<Point2D>> partnerWithCommonPoints = agent.getPartner();
                     if (partnerWithCommonPoints != null) {
                         return new ReproductionAction(agent, partnerWithCommonPoints.getFirst(), partnerWithCommonPoints.getSecond());
                     }
//                    System.out.println("NO PARTNERS!");
                }
                break;
            case Migration:
                // TODO: should migration require energy - it should, but the question is should it decrease it?
                if (agent.getEnergy() >= simulationData.migrationEnergy && !agent.getIsland().isElite()) {
                    Island targetIsland = agent.generateTargetIsland();
                    return new MigrationAction(agent, targetIsland);
                }
                break;
            case Death:
                return new DeathAction(agent);
        }
        return null;
    }
}
