package org.example.model;


import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;

import java.util.Set;

public class Agent {
    private Solution solution;
    private double energy;
    private double prestige;
    private Island island;
    private boolean madeAction = false;


    public Agent(Solution solution, double energy, double prestige, Island island) {
        this.solution = solution;
        this.energy = energy;
        this.prestige = prestige;
    }

    public Agent createNewAgent(Agent other) {
        Solution otherSolution = other.getSolution();

        Solution newSolution = EMASSolutionGenerator.generateSolution(solution, otherSolution);
        Agent newAgent = new Agent(newSolution, energy, 0, island);
//        island.addAgent(newAgent);
        return newAgent;
    }

    public void performAction(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        Action action = ActionFactory.getAction(this);
        action.perform(agentsToAdd, agentsToRemove);
    }

    /** Check if Agent has a potential partner to create a new Agent with. */
    public Agent getPartner() {
        return (Agent) island.getAgents().toArray()[0];
    }

    /** Notify other elements to remove references */
    public void die() {
        island.removeAgent(this);
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
