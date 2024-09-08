package org.example.model;


import org.apache.commons.math3.util.Pair;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;
import org.example.model.action.ActionType;
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
    private double prestige = 0;
    private Island island;
    private Island previousIsland;
    private boolean madeAction;
    private final Random random = new Random();
    private static final SimulationData simulationData = SimulationData.getInstance();
    private int similarAgentsCounter = 0;
    private LinkedList<Pair<Boolean, Integer>> recentCrowdingData = new LinkedList<>();
    private final Map<Agent, Integer> otherAgentsCounters = new HashMap<>();
    private final Set<Agent> similarAgents = new HashSet<>(); // agents which solution is closer than epsilon
    private final Set<Agent> allMetAgents = new HashSet<>(); // all met agents (similar and not)
    private final Map<Agent, Integer> differentAgents = new HashMap<>(); // agents that are not similar and the number of agents in their surroundings
    private int similarMeetings = 0;
    private int meetings = 0;
    private int dominatedTimes = 0;
    private double crowdingFactor = 0;
    private int age = 0;
    public int id;

    private final Map<Agent, Integer> recentlyMetAgentsData = new HashMap<>();
    private final Queue<Agent> recentlyMetAgentsQueue = new LinkedList<>();

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
            Action.actionCount.put(ActionType.Idle, Action.actionCount.get(ActionType.Idle) + 1);
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
        double agentDominationFactor = meetings != 0 ? (double) dominatedTimes / meetings : 0;
        double otherDominationFactor = other.meetings != 0 ? (double) other.dominatedTimes / other.meetings : 0;

        totalCounter++;

        if (dominationResult == 1) { // agent dominates other
            transferResources(this, other, true);
        } else if (dominationResult == -1) { // other dominates agent
            transferResources(other, this, true);
        } else { // neither of the 2 dominated the other
//            if (agentDominationFactor < otherDominationFactor) {
//                transferResources(this, other, false);
//                dominationFactorCounter++;
//            } else if (otherDominationFactor < agentDominationFactor) {
//                dominationFactorCounter++;
//                transferResources(other, this, false);
                int similarNeighbors = (int) getIsland().getAgents().stream().filter(a -> !a.equals(this)).filter(a -> a.areSimilar(this, simulationData.similarityEpsilon)).count();
                int otherSimilarNeighbors = (int) other.getIsland().getAgents().stream().filter(a -> !a.equals(other)).filter(a -> a.areSimilar(other, simulationData.similarityEpsilon)).count();
//            System.out.println(similarNeighbors + " " + otherSimilarNeighbors + " " + getIsland().getAgents().size());
                if (similarNeighbors < otherSimilarNeighbors) {
                    transferResources(this, other, false);
                } else if (similarNeighbors > otherSimilarNeighbors) {
                    transferResources(other, this, false);
                }
            }

//            else if (this.crowdingFactor > other.crowdingFactor) {
//                crowdingCounter++;
//                transferResources(this, other, false);
//            } else if (other.crowdingFactor > this.crowdingFactor) {
//                crowdingCounter++;
//                transferResources(other, this, false);
//            }
//            System.out.println(crowdingFactor + " " + other.crowdingFactor);
//             else if (areSimilar(other, simulationData.similarityEpsilon)) {
//                int index = random.nextInt(2);
//                if (index == 0) {
//                    transferResources(this, other, false);
//                } else {
//                    transferResources(other, this, false);
//                }
//            }
//            } else if (areSimilar(other)) {
//                int index = random.nextInt(2);
//                if (index == 0) {
//                    transferResources(this, other, false);
//                } else {
//                    transferResources(other, this, false);
//                }
//            }
//            } else {
//                double similarRatio = allMetAgents.size() != 0 ? (double) similarAgents.size() / allMetAgents.size() : 0;
//                double otherSimilarRatio = other.allMetAgents.size() != 0 ? (double) other.similarAgents.size() / other.allMetAgents.size() : 0;
////                double similarRatio = meetings != 0 ? (double) similarMeetings / meetings : 0;
////                double otherSimilarRatio = other.meetings != 0 ? (double) other.similarMeetings / other.meetings : 0;
//
//                if (similarRatio < otherSimilarRatio) {
//                    transferResources(this, other, false);
//                } else if (similarRatio > otherSimilarRatio) {
//                    transferResources(other, this, false);
//                }
//            }


        if (areSimilar(other, simulationData.similarityEpsilon)) {
            similarAgents.add(other);
            other.similarAgents.add(this);
//            similarMeetings++;
//            other.similarMeetings++;
        }

        allMetAgents.add(other);
        other.allMetAgents.add(this);


//        int windowSize = (int) Double.POSITIVE_INFINITY;
        int windowSize = 20;
//        updateCrowdingEstimate(windowSize, other.similarAgentsCounter, similar);
//        other.updateCrowdingEstimate(windowSize, this.similarAgentsCounter, similar);
        updateSimilarAgents(other, windowSize);
        other.updateSimilarAgents(this, windowSize);

//        this.otherAgentsCounters.put(other, other.similarAgentsCounter);
//        other.otherAgentsCounters.put(this, this.similarAgentsCounter);

        meetings++;
        other.meetings++;
    }

    private void updateSimilarAgents(Agent other, int windowSize) {
        if (recentlyMetAgentsData.containsKey(other)) {
            recentlyMetAgentsData.remove(other);
            recentlyMetAgentsQueue.remove(other);
            if (areSimilar(other, simulationData.similarityEpsilon)) {
                similarAgentsCounter--;
            }
        }
        if (recentlyMetAgentsQueue.size() >= windowSize) {
            Agent oldestAgent = recentlyMetAgentsQueue.remove();
            recentlyMetAgentsData.remove(oldestAgent);
            if (areSimilar(oldestAgent, simulationData.similarityEpsilon)) {
                similarAgentsCounter--;
            }
        }
        recentlyMetAgentsData.put(other, other.similarAgentsCounter);
        recentlyMetAgentsQueue.add(other);
        if (areSimilar(other, simulationData.similarityEpsilon)) {
            similarAgentsCounter++;
        }
    }

    private boolean areSimilar(Agent other, double epsilon) {
        Map<OptimizedFunction, Float> otherFunctions = other.getSolution().getFunctionValues();
        for (OptimizedFunction function : solution.getFunctionValues().keySet()) {
            float value = solution.getFunctionValues().get(function);
            float otherValue = otherFunctions.get(function);
            if (Math.min(value, otherValue) / Math.max(value, otherValue) < epsilon) {
                return false;
            }
        }

//        for (OptimizedFunction function : solution.getFunctionValues().keySet()) {
//            float value = solution.getFunctionValues().get(function);
//            float otherValue = otherFunctions.get(function);
//            System.out.println(function + " " +  (Math.min(value, otherValue) / Math.max(value, otherValue)));
//        }
        return true;
    }

    // testing the crowding factor used in EMAS papers
    private boolean areSimilar(Agent other) {
        Map<OptimizedFunction, Float> otherFunctions = other.getSolution().getFunctionValues();
        for (OptimizedFunction function : solution.getFunctionValues().keySet()) {
            double maxDiff = simulationData.crowdingFactorMap.get(function);
            float value = solution.getFunctionValues().get(function);
            float otherValue = otherFunctions.get(function);
//            System.out.println(function.toString() + ": " + abs(value - otherValue));
            if (abs(value - otherValue) > maxDiff) {
//                System.out.println(function.toString());
                return false;
            }
        }
        return true;
    }

    private static void transferResources(Agent toAgent, Agent fromAgent, boolean dominated) {
        if (dominated) {
            toAgent.prestige++;
            fromAgent.dominatedTimes++;
        }
        toAgent.energy += Math.min(fromAgent.energy, simulationData.energyTaken);
        fromAgent.energy -= Math.min(fromAgent.energy, simulationData.energyTaken);
    }

//    public boolean canMigrateToElite() {
//        // TODO: REWRITE IT ACCORDING TO THE PAPER. THE DECISION MUST BE MADE BY THE AGENT AND ITS SURROUNDING ON THE PARETO FRONTIER
////        double avgSurroundings = 0;
//
//        int nonZeroValues = otherAgentsCounters.values().stream().filter(v -> v != 0).collect(Collectors.toSet()).size();
//        double avgSurroundings = (double) otherAgentsCounters.values().stream().reduce(0, Integer::sum) / nonZeroValues;
//        System.out.println("\nSimilar: " + similarAgentsCounter + "\t Surroundings: " + avgSurroundings + "\tPrestige: " + prestige + " " + nonZeroValues + " (" + otherAgentsCounters.size() + ")\n");
//        return similarAgentsCounter > avgSurroundings;
//    }

    public boolean canMigrateToElite() {
        System.out.println(similarAgentsCounter + " " + crowdingFactor + " " + energy);
//        return true;
        return similarAgentsCounter > crowdingFactor;
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

    public int getAge() {
        return age;
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

    public void setAge(int age) {
        this.age = age;
    }
}
