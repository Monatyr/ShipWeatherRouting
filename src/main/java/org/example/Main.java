package org.example;

import org.example.algorithm.emas.EMAS;
import org.example.model.Agent;
import org.example.model.Solution;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.lang.Math.min;


public class Main {
    public static void main(String[] args) throws Exception {
        runRouteGenerationScript();

        EMAS emas = new EMAS();
        Set<Agent> population = emas.getPopulation();
        System.out.println("\n--- TOTAL ENERGY: " + population.stream().map(Agent::getEnergy).reduce(0.0, Double::sum));
        Set<Solution> solutions = emas.run();

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

        population = emas.getPopulation();
        System.out.println("\n--- TOTAL ENERGY: " + population.stream().map(Agent::getEnergy).reduce(0.0, Double::sum));
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
}