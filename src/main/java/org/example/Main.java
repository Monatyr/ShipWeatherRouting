package org.example;

import org.example.algorithm.emas.EMAS;
import org.example.model.*;
import org.example.model.action.Action;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static org.example.util.UtilFunctions.getBestPerCategory;


public class Main {
    private static EMAS emas;

    public static void main(String[] args) throws Exception {
        // route generation script
        runPythonScript("scripts/generate_initial_routes.py", "");
        emas = new EMAS();

        getIslandsInfo(emas);
        Set<Agent> population = emas.getPopulation();
        System.out.println("\n--- TOTAL ENERGY: " + population.stream().map(Agent::getEnergy).reduce(0.0, Double::sum));

        List<String> topRoutes = getBestPerCategory(population.stream().map(Agent::getSolution).collect(Collectors.toSet()));
        runPythonScript("scripts/plot_routes.py", "initial_routes.png " + topRoutes.toString());

        Set<Solution> solutions = emas.run();

        generalInfo(solutions);
        topRoutes = getBestPerCategory(solutions);
        getIslandsInfo(emas);
        System.out.println(Action.actionCount);
        // visualise routes
        runPythonScript("scripts/plot_routes.py", "top3_routes.png "  + topRoutes.toString());
    }

    public static void runPythonScript(String scriptPath, String args) {
        ProcessBuilder processBuilder;
        List<String> parameters;
        if (args.isEmpty()) {
            processBuilder = new ProcessBuilder("/run/current-system/sw/bin/python", scriptPath);
        } else {
            parameters = List.of(args.split(" ", 2));
            processBuilder = new ProcessBuilder("/run/current-system/sw/bin/python", scriptPath, parameters.get(0), parameters.get(1));
        }
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println(scriptPath + " exit code: " + exitCode);
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

    public static void getIslandsInfo(EMAS emas) {
        List<Island> islands = emas.getIslands();
        islands.forEach(i -> System.out.println((i.isElite() ? "\n ELITE:\t" : "NORMAL:\t") + i.getAgents().size()));
        System.out.println();
    }
}