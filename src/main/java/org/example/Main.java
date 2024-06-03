package org.example;

import org.example.algorithm.emas.EMAS;
import org.example.model.*;
import org.example.model.action.Action;
import org.example.model.action.ActionFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.lang.Math.min;


public class Main {
    private static EMAS emas;

    public static void main(String[] args) throws Exception {
        runRouteGenerationScript();
        emas = new EMAS();

        Set<Agent> population = emas.getPopulation();
        System.out.println("\n--- TOTAL ENERGY: " + population.stream().map(Agent::getEnergy).reduce(0.0, Double::sum));

        Set<Solution> solutions = emas.run();

        generalInfo(solutions);
        getBestPerCategory(solutions);
        getIslandsInfo(emas);
//        solTime.getRoutePoints().stream().map(RoutePoint::getCoordinates).map(a -> a.longitude() + ", " + a.latitude()).forEach(System.out::println);
        System.out.println(Action.actionCount);
    }

    public static void runRouteGenerationScript() {
        String scriptPath = "scripts/generate_initial_routes.py";
        ProcessBuilder processBuilder = new ProcessBuilder("/run/current-system/sw/bin/python", scriptPath);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("Initial route generation exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void generalInfo(Set<Solution> solutions) {
        List<Solution> sortedSolutions = solutions.stream().sorted().toList();
        int showSize = min(solutions.size(), 5);
        List<Solution> topSol = sortedSolutions.subList(0, showSize);
        List<Solution> bottomSol = sortedSolutions.subList(solutions.size() - showSize, solutions.size());

        System.out.println("\n\n--- TOP " + topSol.size() + " SOLUTIONS (sum of function values) ---");
        for (Solution solution : topSol) {
            System.out.println(solution.getFunctionValues() + " " + solution.getFunctionValues().values().stream().reduce(Float::sum));
        }
        System.out.println("\n--- BOTTOM " + bottomSol.size() + " SOLUTIONS (sum of function values) ---");
        for (Solution solution : bottomSol) {
            System.out.println(solution.getFunctionValues() + " " + solution.getFunctionValues().values().stream().reduce(Float::sum));
        }
        System.out.println("\n--- TOTAL SOLUTIONS: " + solutions.size() + " ---");

        Set<Agent> population = emas.getPopulation();
        System.out.println("\n--- TOTAL ENERGY: " + population.stream().map(Agent::getEnergy).reduce(0.0, Double::sum) + " ---");
    }

    public static void getBestPerCategory(Set<Solution> solutions) {
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
    }

    public static void getIslandsInfo(EMAS emas) {
        List<Island> islands = emas.getIslands();
        islands.forEach(i -> System.out.println((i.isElite() ? "\n ELITE:\t" : "NORMAL:\t") + i.getAgents().size()));
        System.out.println();
    }
}