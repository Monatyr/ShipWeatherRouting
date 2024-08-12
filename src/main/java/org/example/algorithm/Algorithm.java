package org.example.algorithm;

import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.Agent;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.SimulationData;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.util.UtilFunctions.getBestPerCategory;

public abstract class Algorithm {
    protected static SimulationData simulationData = SimulationData.getInstance();
    protected static int iterations = 0;
    protected static Set<Agent> population = new HashSet<>();
    protected static Random random = new Random();

    public Set<Solution> run() throws Exception {
        if (population.isEmpty()) {
            generateInitialPopulation();
        }

        while (validState() && !checkStopCondition()) {
            runIteration();
            iterations++;
            if (iterations % 500 == 0) { // || (iterations > 0.9 * simulationData.maxIterations && iterations % 5 == 0)) {
                long nonElitePopulationSize = population.stream().filter(agent -> !agent.getIsland().isElite()).count();
                System.out.println("Iteration: " + iterations + (iterations < 10000 ? "\t" : "" ) + "\tNon-elite population: " + nonElitePopulationSize + "\t\tElite: " + population.stream().filter(o -> o.getIsland().isElite()).toList().size() + "\tEpsilon: " + simulationData.paretoEpsilon);
                System.out.println("Below: " + Solution.below + "\tAbove: " + Solution.above);
                System.out.println("Avg engine load: " + Solution.fullNodePower / Solution.nodeNumber / SimulationData.getInstance().maxOutput + "\tAvg speed: " + Solution.fullNodeSpeed / Solution.nodeNumber);
                getBestPerCategory(population.stream().map(Agent::getSolution).collect(Collectors.toSet()));
            }
        }

        Set<Solution> solutions = population.stream().map(Agent::getSolution).collect(Collectors.toSet());
        System.out.println("\n\nSOLUTIONS: " + solutions.size());
        Set<Solution> nonDominatedSolutions = getNonDominatedSolutions(null);
        nonDominatedSolutions = lastSolutionImprovement(nonDominatedSolutions);
        System.out.println("NONDOMINATED SOLUTIONS: " + nonDominatedSolutions.size());
        return nonDominatedSolutions;
    }

    private boolean validState() {
        return population != null;
    }

    protected boolean checkStopCondition() {
        return iterations > simulationData.maxIterations;
    }

    protected Set<Solution> getNonDominatedSolutions(List<Solution> solutions) {
        if (solutions == null) {
            solutions = population.stream().map(Agent::getSolution).toList();
        }
        Set<Solution> nonDominatedSolutions = new HashSet<>();
        for (int i = 0; i < solutions.size(); i++) {
            Solution currSolution = solutions.get(i);
            boolean dominated = false;
            for (int j = 0; j < solutions.size(); j++) {
                if (i == j) {
                    continue;
                }
                Solution otherSolution = solutions.get(j);
                if (otherSolution.checkIfDominates(currSolution, false) == 1) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                nonDominatedSolutions.add(currSolution);
            }
        }
        return nonDominatedSolutions;
    }

    public static Solution generateInitialSolution(List<RoutePoint> route) {
        double routeTargetSpeed = random.nextDouble(simulationData.minSpeed, simulationData.maxSpeed);
        for (RoutePoint routePoint : route) {
            routePoint.setShipSpeed(routeTargetSpeed);
        }
        Solution solution = EMASSolutionGenerator.generateSolution(route);
        int counter = 0;
        do {
            if (counter != 0) {
                System.out.println("Initial population too dangerous: " + counter);
            }
            solution = EMASSolutionGenerator.mutateSolution(solution, simulationData.initialMutationRate);
            solution.calculateRouteValues();
            counter++;
        } while (solution.isTooDangerous());
        return solution;
    }

    protected static void generateInitialPopulation() {
        List<List<RoutePoint>> startingRoutes = new ArrayList<>();
        for (int i = 0; i < simulationData.populationSize; i++) {
            if (i % 2 == 0) {
                startingRoutes.add(EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/great_circle_route.txt"));
            } else {
                startingRoutes.add(EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/rhumb_line_route.txt"));
            }
        }
        for (List<RoutePoint> route : startingRoutes) {
            Solution solution = generateInitialSolution(route);
            solution.calculateFunctionValues();
            population.add(new Agent(solution, simulationData.initialEnergy, 0, null, false));
        }
    }

    private Set<Solution> lastSolutionImprovement(Set<Solution> solutions) {
        int counter = 0;
        List<Solution> nonDominatedSolutionsList = new ArrayList<>(solutions.stream().toList());
        for (int i = 0; i < nonDominatedSolutionsList.size(); i++) {
            for (int j = 0; j < 400; j++) {
                Solution sol = nonDominatedSolutionsList.get(i);
                Solution newSolution = EMASSolutionGenerator.mutateSolution(sol, simulationData.eliteMutationRate);
                if (newSolution.checkIfDominates(sol, false) > 0 && !newSolution.isTooDangerous()) {
                    nonDominatedSolutionsList.set(i, newSolution);
                    counter++;
                }
            }
        }
        System.out.println("\nSolution improved " + counter + " times :)");
        return getNonDominatedSolutions(nonDominatedSolutionsList);
    }

    protected abstract void runIteration() throws Exception;
}
