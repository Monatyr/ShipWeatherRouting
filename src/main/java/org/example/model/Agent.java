package org.example.model;


import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;
import org.example.util.SimulationData;

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

    public Agent createNewAgent(Agent other) {
        Solution otherSolution = other.getSolution();
        Solution newSolution = EMASSolutionGenerator.generateSolution(solution, otherSolution);
        Agent newAgent = new Agent(newSolution, energy, 0, island, true);
        return newAgent;
    }

    public void performAction(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        Action action = ActionFactory.getAction(this);
        action.perform(agentsToAdd, agentsToRemove);
    }

    public Island generateTargetIsland() {
        Set<Island> potentialTargetIslands = island.getNeighbouringIslands();
        int islandIndex = random.nextInt(potentialTargetIslands.size());
        return potentialTargetIslands.stream().toList().get(islandIndex);
    }

    /** Check if Agent has a potential partner to create a new Agent with. */
    public Agent getPartner() {
        Set<Agent> availablePartners = island.getAgents().stream()
                .filter(p -> !p.equals(this))
                .filter(p -> !p.madeAction)
                .filter(p -> p.getEnergy() >= simulationData.reproductionEnergy)
                .collect(Collectors.toSet());

        if (availablePartners.isEmpty()) {
            return null;
        }

        int partnerIndex = random.nextInt(availablePartners.size());
        return availablePartners.stream().toList().get(partnerIndex);
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

    public void setIsland(Island island) {
        this.island = island;
    }

    public void setMadeAction(boolean value) {
        this.madeAction = value;
    }
}
