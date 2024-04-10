package org.example.model;


import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.action.ActionFactory;
import org.example.model.action.ActionType;

public class Agent {
    private Solution solution;
    private float energy;
    private float prestige;
    private Island island;


    public Agent(Solution solution, float energy, float prestige, Island island) {
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

        ActionFactory.getAction(this);
    }

    public float getEnergy() {
        return energy;
    }

    public float getPrestige() {
        return prestige;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setIsland(Island island) {
        this.island = island;
    }
}
