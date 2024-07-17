package org.example.model.action;


import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.example.model.Agent;
import org.example.model.Island;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.util.List;
import java.util.Random;


public abstract class ActionFactory {
    public static SimulationData simulationData = SimulationData.getInstance();
    private static List<Pair<ActionType, Double>> itemWeights = List.of(
            new Pair(ActionType.Migration, simulationData.migrationProbability),
            new Pair(ActionType.Reproduction, simulationData.reproductionProbability)
    );
    private static final EnumeratedDistribution<ActionType> enumeratedDistribution = new EnumeratedDistribution<>(itemWeights);
    private static Random random = new Random();

    public static Action getAction(Agent agent) {
        ActionType actionType;

        if (agent.getEnergy() <= simulationData.deathEnergyBound) {
            actionType = ActionType.Death;
        } else {
            actionType = enumeratedDistribution.sample();
        }

        if (agent.getIsland().isElite()) {
            return new EliteMutation(agent);
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
                // TODO: implement migrations to the elite island and the behavior of elite agents
                Island targetIsland;
                if (agent.getEnergy() < simulationData.migrationEnergy || agent.getIsland().isElite()) {
                    break;
                }
                if (agent.getPrestige() > SimulationData.getInstance().neededPrestige
                        && agent.canMigrateToElite()
                ) {
                    targetIsland = agent.generateEliteIsland();
                } else {
                    targetIsland = agent.generateTargetIsland();
                }
                return new MigrationAction(agent, targetIsland);
            }
            case Death -> {
                return new DeathAction(agent);
            }
        }
        return null;
    }
}
