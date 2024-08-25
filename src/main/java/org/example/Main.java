package org.example;

import com.sun.istack.Nullable;
import org.example.algorithm.emas.EMAS;
import org.example.model.*;
import org.example.model.action.Action;
import org.example.util.SimulationData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static org.example.util.UtilFunctions.*;


public class Main {
    private static EMAS emas;

    public static void main(String[] args) throws Exception {
        // route generation script
        runPythonScript("scripts/generate_initial_routes.py", null);
        emas = new EMAS();

        getIslandsInfo(emas);
        Set<Agent> population = emas.getPopulation();
        saveToJson(population.stream().map(Agent::getSolution).collect(Collectors.toSet()), "results/initialSolutions.json");
        System.out.println("\n--- TOTAL ENERGY: " + population.stream().map(Agent::getEnergy).reduce(0.0, Double::sum));

        List<String> allRoutes = population.stream().map(Agent::getSolution).map(s -> s.getRoutePoints().toString()).toList();
        List<String> topRoutes = getBestPerCategory(population.stream().map(Agent::getSolution).collect(Collectors.toSet()));
        writeSolutionsToFile(allRoutes, "src/main/resources/visualisation-solutions/initial-solutions.txt");
        List<String> arguments = List.of(
                "--resultFile", "initial_routes.png",
                "--weatherFile", SimulationData.getInstance().weatherPath,
                "--routes", "src/main/resources/visualisation-solutions/initial-solutions.txt"
        );
        runPythonScript("scripts/plot_routes.py", arguments);

        Set<Solution> solutions = emas.run();

        generalInfo(solutions);
        allRoutes = solutions.stream().map(s -> s.getRoutePoints().toString()).toList();
        topRoutes = getBestPerCategory(solutions);
//        topRoutes = getSortedByObjective(solutions, OptimizedFunction.FuelUsed);
        writeSolutionsToFile(allRoutes, "src/main/resources/visualisation-solutions/resulting-solutions.txt");
        getIslandsInfo(emas);
        System.out.println(Action.actionCount);
        arguments = List.of(
                "--resultFile", "top3_routes.png",
                "--weatherFile", SimulationData.getInstance().weatherPath,
                "--routes", "src/main/resources/visualisation-solutions/resulting-solutions.txt"
        );
        runPythonScript("scripts/plot_routes.py", arguments);
        runPythonScript("scripts/plot_average_values.py", List.of("--resultFile", "average_function_values.png"));
        runPythonScript("scripts/plot_pareto_front.py", List.of("--routes", "results/resultSolutions.json", "--resultFile", "pareto_front.png"));
        saveToJson(solutions, "results/resultSolutions.json");
    }

    public static void runPythonScript(String scriptPath, @Nullable List<String> args) {
        ProcessBuilder processBuilder;
        List<String> command = new ArrayList<>();
        command.add("/run/current-system/sw/bin/python");
        command.add(scriptPath);
        if (args != null && !args.isEmpty()) {
            for (String arg : args) {
                command.add(arg);
            }
        }
        processBuilder = new ProcessBuilder(command);
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

    public static void writeSolutionsToFile(List<String> solutions, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : solutions) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}