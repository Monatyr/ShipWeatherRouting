package org.example.model;


import org.example.algorithm.emas.EMASSolutionGenerator;

public class Agent {
    private Solution solution;
    private float energy;
    private float prestige;


    public Agent(Solution solution, float energy, float prestige) {
        this.solution = solution;
        this.energy = energy;
        this.prestige = prestige;
    }

    public Agent createNewAgent(Agent other) {
        Solution otherSolution = other.getSolution();

        Solution newSolution = EMASSolutionGenerator.generateSolution(solution, otherSolution);
        return new Agent(solution, energy, 0);
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
}
