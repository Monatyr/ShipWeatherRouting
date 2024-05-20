package org.example.model;


import org.apache.commons.math3.util.Pair;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

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

    public Agent createNewAgent(Agent other, List<GridPoint> commonGridPoints) throws Exception {
        Solution otherSolution = other.getSolution();
        Solution newSolution = EMASSolutionGenerator.generateSolution(solution, otherSolution, commonGridPoints);
        List<RoutePoint> route = newSolution.getRoutePoints();
        for (int i = 1; i < route.size(); i++) {
            if (
                    abs(route.get(i-1).getGridCoordinates().x() - route.get(i).getGridCoordinates().x()) != 1
                    || abs(route.get(i-1).getGridCoordinates().y() - route.get(i).getGridCoordinates().y()) > simulationData.maxVerticalDistance
            ) {
                // Sanity check Exception - make sure that the generated solutions follow the system rules
                throw new Exception(
                        "(" + route.get(i-1).getGridCoordinates().y() + "," + route.get(i-1).getGridCoordinates().x() + ") ("
                                + route.get(i).getGridCoordinates().y() + ", "
                                + route.get(i).getGridCoordinates().x() + ")"
                );
            }
        }
        energy -= simulationData.reproductionEnergy;
        other.energy -= simulationData.reproductionEnergy;
        Agent newAgent = new Agent(newSolution, energy, 0, island, true);
        return newAgent;
    }

    public void performAction(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) throws Exception {
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

    /** Return a potential partner with common points for an Agent or null */
    public Pair<Agent, List<GridPoint>> getPartner() {
        Set<Agent> availablePartners = island.getAgents().stream()
                .filter(p -> !p.equals(this))
                .filter(p -> !p.madeAction)
                .filter(p -> p.getEnergy() >= simulationData.reproductionEnergyBound)
                .collect(Collectors.toSet());
        if (availablePartners.isEmpty()) {
            return null;
        }
        List<RoutePoint> routePoints = solution.getRoutePoints();
        List<GridPoint> commonGridPoints = new ArrayList<>();

        while (!availablePartners.isEmpty()) {
            int partnerIndex = random.nextInt(availablePartners.size());
            Agent partner = availablePartners.stream().toList().get(partnerIndex);
            List<RoutePoint> partnerRoutePoints = partner.solution.getRoutePoints();
            availablePartners.remove(partner);

            for (int i = 0; i < routePoints.size(); i++) {
                GridPoint currRoutePoint = routePoints.get(i).getGridCoordinates();
                GridPoint currPartnerRoutePoint = partnerRoutePoints.get(i).getGridCoordinates();
                if (currRoutePoint.equals(currPartnerRoutePoint)) {
                    commonGridPoints.add(currRoutePoint);
                }
            }

            System.out.println();
            System.out.println("Common grid points: " + commonGridPoints.size());
            System.out.println("Route 1: " + routePoints.stream().map(RoutePoint::getGridCoordinates).toList());
            System.out.println("Route 2: " + partnerRoutePoints.stream().map(RoutePoint::getGridCoordinates).toList());
            System.out.println();

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
