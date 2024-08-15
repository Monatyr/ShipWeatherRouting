package org.example.util;

import org.example.model.Agent;
import org.example.model.Island;
import org.example.model.OptimizedFunction;
import org.example.model.Solution;

import java.util.*;
import java.util.stream.Collectors;

public abstract class UtilFunctions {
    private static SimulationData simulationData = SimulationData.getInstance();

    public static void redistributeEnergyLeft(Agent agent, Island island) {
        Set<Agent> neighbours = island
                .getAgents()
                .stream()
                .filter(other -> !agent.equals(other))
                .filter(other -> other.getEnergy() > simulationData.deathEnergyBound) // sanity check
                .collect(Collectors.toSet());
        if (neighbours.size() == 0) {
            neighbours = island.getNeighbouringIslands()
                    .stream()
                    .filter(neighborIsland -> !neighborIsland.isElite())
                    .map(Island::getAgents)
                    .flatMap(Set::stream)
                    .filter(other -> !agent.equals(other))
                    .filter(other -> other.getEnergy() > simulationData.deathEnergyBound)
                    .collect(Collectors.toSet());
        }

        double energyForEach = agent.getEnergy()/neighbours.size();

        for (Agent neighbour : neighbours) {
            neighbour.setEnergy(neighbour.getEnergy() + energyForEach);
        }
        agent.setEnergy(0.0);
    }


    public static List<String> getBestPerCategory(Set<Solution> solutions) {
        List<Solution> solList = solutions.stream().toList();
        Solution solTime = solList.get(0);
        Solution solFuel = solList.get(0);
        Solution solSafety = solList.get(0);
        for (Solution s : solutions) {
            if (s.getFunctionValues().get(OptimizedFunction.TravelTime) < solTime.getFunctionValues().get(OptimizedFunction.TravelTime)) {
                solTime = s;
            }
            if (s.getFunctionValues().get(OptimizedFunction.FuelUsed) < solFuel.getFunctionValues().get(OptimizedFunction.FuelUsed)) {
                solFuel = s;
            }
            if (s.getFunctionValues().get(OptimizedFunction.Danger) < solSafety.getFunctionValues().get(OptimizedFunction.Danger)) {
                solSafety = s;
            }
        }
        System.out.println("\nTime: " + solTime.getFunctionValues());
        System.out.println("Fuel: " + solFuel.getFunctionValues());
        System.out.println("Danger: " + solSafety.getFunctionValues());

        double totalTime = solList.stream().map(s -> s.getFunctionValues().get(OptimizedFunction.TravelTime)).reduce(Float::sum).get();
        double totalFuel = solList.stream().map(s -> s.getFunctionValues().get(OptimizedFunction.FuelUsed)).reduce(Float::sum).get();
        double totalDanger = solList.stream().map(s -> s.getFunctionValues().get(OptimizedFunction.Danger)).reduce(Float::sum).get();
        System.out.println("Avg time: " + totalTime/solList.size() + "\tAvg fuel: " + totalFuel/solList.size() + "\tAvg danger: " + totalDanger/solList.size());

        System.out.println("\n\nSame: " + Agent.same + "\tDifferent: " + Agent.different);
        return List.of(
                solTime.getRoutePoints().toString(),
                solFuel.getRoutePoints().toString(),
                solSafety.getRoutePoints().toString()
        );
    }

    // returns the last 3 best routes per category
    public static List<String> getSortedByObjective(Set<Solution> solutions, OptimizedFunction sortObjective) {
        int n = solutions.size();
        List<Solution> solList = new ArrayList<>(solutions.stream().toList());
        solList.sort((o1, o2) -> Float.compare(o1.getFunctionValues().get(sortObjective), o2.getFunctionValues().get(sortObjective)));
        return solList.stream().map(sol -> sol.getRoutePoints().toString()).toList().subList(n-3, n);
    }
}
