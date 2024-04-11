package org.example.model;


import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;
import org.example.model.action.ActionType;

public class Agent {
    private Solution solution;
    private double energy;
    private double prestige;
    private Island island;


    public Agent(Solution solution, double energy, double prestige, Island island) {
        this.solution = solution;
        this.energy = energy;
        this.prestige = prestige;
    }

    public Agent createNewAgent(Agent other) {
        Solution otherSolution = other.getSolution();

        Solution newSolution = EMASSolutionGenerator.generateSolution(solution, otherSolution);
        return new Agent(newSolution, energy, 0, island);
    }

    public void performAction() {
        Action action = ActionFactory.getAction(this);
        action.perform();
    }

    /**
     * Check if Agent has a potential partner to create a new Agent with.
     */
    public Agent getPartner() {
        return (Agent) island.getAgents().toArray()[0];
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

    public void setIsland(Island island) {
        this.island = island;
    }
}
