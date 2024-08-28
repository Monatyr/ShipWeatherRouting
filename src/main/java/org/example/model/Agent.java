package org.example.model;


import org.apache.commons.math3.util.Pair;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class Agent {
    private static int dominationFactorCounter = 0;
    private static int crowdingCounter = 0;
    private static int totalCounter = 0;

    private Solution solution;
    private double energy;
    private double prestige;
    private Island island;
    private Island previousIsland;
    private boolean madeAction;
    private final Random random = new Random();
    private static final SimulationData simulationData = SimulationData.getInstance();
    private final Set<Agent> similarAgents = new HashSet<>(); // agents which solution is closer than epsilon
    private final Map<Agent, Integer> differentAgents = new HashMap<>(); // agents that are not similar and the number of agents in their surroundings
    private int meetings = 0;
    private int dominatedTimes = 0;
    private double crowdingFactor = 0;
    public static int different = 0;
    public static int same = 0;
    public int id;

    public Agent(Solution solution, double energy, double prestige, Island island, boolean madeAction) {
        this.solution = solution;
        this.energy = energy;
        this.prestige = prestige;
        this.madeAction = madeAction;
        this.island = island;
        this.previousIsland = island;
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
        Agent newAgent = new Agent(newSolution, simulationData.reproductionEnergy * 2, 0, island, true);
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
        Set<Island> potentialTargetIslands = island.getNeighbouringIslands().stream()
                .filter(i -> !i.isElite())
                .collect(Collectors.toSet());
        int islandIndex = random.nextInt(potentialTargetIslands.size());
        return potentialTargetIslands.stream().toList().get(islandIndex);
    }

    public Island generateEliteIsland() {
        return island.getNeighbouringIslands()
                .stream()
                .filter(Island::isElite)
                .findFirst()
                .get();
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
            if (!commonGridPoints.isEmpty()) {
                return new Pair<>(partner, commonGridPoints);
            }
        }
        return null;
    }

    public void compareTo(Agent other) {
        Solution otherSolution = other.getSolution();
        int dominationResult = solution.checkIfDominates(otherSolution, false);
//        int dominationResult = solution.checkIfEpsilonDominates(otherSolution);
        double agentDominationFactor = meetings != 0 ? (double) dominatedTimes / meetings : 0;
        double otherDominationFactor = other.meetings != 0 ? (double) other.dominatedTimes / other.meetings : 0;

        if (dominationResult == 0) {
            if (agentDominationFactor != otherDominationFactor) {
                different++;
            } else {
                same++;
            }
        }

        totalCounter++;

        if (dominationResult == 1) { // agent dominates other
            transferResources(this, other, true);
        } else if (dominationResult == -1) { // other dominates agent
            transferResources(other, this, true);
        } else { // neither of the 2 dominated the other
            if (agentDominationFactor < otherDominationFactor) {
                transferResources(this, other, false);
                dominationFactorCounter++;
            } else if (otherDominationFactor < agentDominationFactor) {
                dominationFactorCounter++;
                transferResources(other, this, false);
            } else if (this.crowdingFactor > other.crowdingFactor) {
                crowdingCounter++;
                transferResources(this, other, false);
            } else if (other.crowdingFactor > this.crowdingFactor) {
                crowdingCounter++;
                transferResources(other, this, false);
            }
        }
//        System.out.println(dominationFactorCounter + " " + crowdingCounter + " " + totalCounter);
        // gathering information about the surroundings of different agents
        double solutionSimilarity = solution.similarityBetweenSolutions(otherSolution);
        if (solutionSimilarity >= simulationData.similarityEpsilon) {
            similarAgents.add(other);
        }
        int otherSurroundings = other.similarAgents.size();
        differentAgents.remove(other);
        differentAgents.put(other, otherSurroundings);

        meetings++;
        other.meetings++;
    }

    private static void transferResources(Agent toAgent, Agent fromAgent, boolean dominated) {
        if (dominated) {
            toAgent.prestige++;
            fromAgent.dominatedTimes++;
        }
        toAgent.energy += Math.min(fromAgent.energy, simulationData.energyTaken);
        fromAgent.energy -= Math.min(fromAgent.energy, simulationData.energyTaken);
    }

    public boolean canMigrateToElite() {
        double avgSurroundings = (double) differentAgents.values().stream().reduce(0, Integer::sum) / differentAgents.size();
        System.out.println("Elite check: " + similarAgents.size() + " " + avgSurroundings);
        return similarAgents.size() > avgSurroundings;
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

    public double getCrowdingFactor() {
        return crowdingFactor;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setIsland(Island island) {
        this.island = island;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public void setMadeAction(boolean value) {
        this.madeAction = value;
    }

    public void setCrowdingFactor(double crowdingFactor) {
        this.crowdingFactor = crowdingFactor;
    }

    public Island getPreviousIsland() {
        return previousIsland;
    }

    public void setPreviousIsland(Island previousIsland) {
        this.previousIsland = previousIsland;
    }

}
