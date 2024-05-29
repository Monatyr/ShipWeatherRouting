package org.example.model.action;


import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.example.model.Agent;
import org.example.model.Island;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.util.List;


public class ActionFactory {
    public static SimulationData simulationData = SimulationData.getInstance();
    private static List<Pair<ActionType, Double>> itemWeights = List.of(
            new Pair(ActionType.Migration, simulationData.migrationProbability),
            new Pair(ActionType.Reproduction, simulationData.reproductionProbability)
    );
    private static final EnumeratedDistribution<ActionType> enumeratedDistribution = new EnumeratedDistribution<>(itemWeights);

    public static Action getAction(Agent agent) {
        ActionType actionType;

        if (agent.getEnergy() <= simulationData.deathEnergyBound) {
            actionType = ActionType.Death;
        } else {
            actionType = enumeratedDistribution.sample();
        }

        switch (actionType) {
            case Reproduction -> {
                if (agent.getEnergy() >= simulationData.reproductionEnergyBound && simulationData.populationSize < simulationData.maxPopulation) {
                    Pair<Agent, List<GridPoint>> partnerWithCommonPoints = agent.getPartner();
                    if (partnerWithCommonPoints != null) {
                        return new ReproductionAction(agent, partnerWithCommonPoints.getFirst(), partnerWithCommonPoints.getSecond());
                    }
                }
            }
            case Migration -> {
                // TODO: should migration require energy - it should, but the question is should it decrease it?
                if (agent.getEnergy() >= simulationData.migrationEnergy && !agent.getIsland().isElite()) {
                    Island targetIsland = agent.generateTargetIsland();
                    return new MigrationAction(agent, targetIsland);
                }
            }
            case Death -> {
                return new DeathAction(agent);
            }
        }
        return null;
    }
}
