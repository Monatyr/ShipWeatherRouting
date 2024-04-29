package org.example.model;


import org.apache.commons.math3.util.Pair;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;
import org.example.util.SimulationData;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Agent {
    private Solution solution;
    private double energy;
    private double prestige;
    private Island island;
    private boolean madeAction;
    private Random random = new Random();
    private SimulationData simulationData = SimulationData.getInstance();
    public int id;


    public Agent(Solution solution, double energy, double prestige, Island island, boolean madeAction) {
        this.solution = solution;
        this.energy = energy;
        this.prestige = prestige;
        this.madeAction = madeAction;
        this.island = island;
        this.id = SimulationData.getInstance().generateId();
    }

    public Agent createNewAgent(Agent other, List<Point2D> commonGridPoints) {
        Solution otherSolution = other.getSolution();
        Solution newSolution = EMASSolutionGenerator.generateSolution(solution, otherSolution, commonGridPoints);
        energy -= simulationData.reproductionEnergy;
        other.energy -= simulationData.reproductionEnergy;
        Agent newAgent = new Agent(newSolution, energy, 0, island, true);
        return newAgent;
    }

    public void performAction(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        Action action = ActionFactory.getAction(this);
        if (action == null) {
            return;
        }
        action.perform(agentsToAdd, agentsToRemove);
    }

    public Island generateTargetIsland() {
        Set<Island> potentialTargetIslands = island.getNeighbouringIslands();
        int islandIndex = random.nextInt(potentialTargetIslands.size());
        return potentialTargetIslands.stream().toList().get(islandIndex);
    }

    /** Check if Agent has a potential partner to create a new Agent with. */
    public Pair<Agent, List<Point2D>> getPartner() {
        Set<Agent> availablePartners = island.getAgents().stream()
                .filter(p -> !p.equals(this))
                .filter(p -> !p.madeAction)
                .filter(p -> p.getEnergy() >= simulationData.reproductionEnergyBound)
                .collect(Collectors.toSet());

        if (availablePartners.isEmpty()) {
            return null;
        }

        List<RoutePoint> routePoints = solution.getRoutePoints();
        List<Point2D> commonGridPoints = new ArrayList<>();

        while (!availablePartners.isEmpty()) {
            int partnerIndex = random.nextInt(availablePartners.size());
            Agent partner = availablePartners.stream().toList().get(partnerIndex);
            List<RoutePoint> partnerRoutePoints = partner.solution.getRoutePoints();
            availablePartners.remove(partner);

            for (int i = 0; i < routePoints.size(); i++) {
                Point2D currRoutePoint = routePoints.get(i).getGridCoordinates();
                Point2D currPartnerRoutePoint = partnerRoutePoints.get(i).getGridCoordinates();

                if (currRoutePoint.equals(currPartnerRoutePoint)) {
                    commonGridPoints.add(currRoutePoint);
                }
            }

            if (!commonGridPoints.isEmpty()) {
                return new Pair<>(partner, commonGridPoints);
            }
        }
        return null;
    }

    public void compareTo(Agent other) {
        Solution otherSolution = other.getSolution();
        int dominationResult = solution.checkIfDominates(otherSolution);

        if (dominationResult == 1) {
            prestige++;
            energy += Math.min(other.getEnergy(), simulationData.energyTaken);
            other.energy -= Math.min(other.getEnergy(), simulationData.energyTaken);
        } else if (dominationResult == -1) {
            other.prestige++;
            other.energy += Math.min(energy, simulationData.energyTaken);
            energy -= Math.min(energy, simulationData.energyTaken);
        }
    }

    public double getEnergy() {
        return energy;
    }

    public double getPrestige() {
        return prestige;
    }

    public Solution getSolution() {
        return solution;
    }

    public Island getIsland() {
        return island;
    }

    public boolean getMadeAction() {
        return madeAction;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public void setMadeAction(boolean value) {
        this.madeAction = value;
    }
}
