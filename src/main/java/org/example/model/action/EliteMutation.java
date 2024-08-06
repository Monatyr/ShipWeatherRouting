package org.example.model.action;

import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.Agent;
import org.example.model.Solution;

import java.util.Set;

public class EliteMutation extends Action {
    public EliteMutation(Agent agent) {
        super(agent);
    }

    @Override
    public void perform(Set<Agent> agentsToAdd, Set<Agent> agentsToRemove) {
        Solution newSolution = EMASSolutionGenerator.mutateSolution(agent.getSolution(), simulationData.eliteMutationRate);
        if (newSolution.checkIfDominates(agent.getSolution(), false) > 0 && !newSolution.isTooDangerous()) {
            System.out.println("Elite agent found better solution!");
            agent.setSolution(newSolution);
        }
    }
}
