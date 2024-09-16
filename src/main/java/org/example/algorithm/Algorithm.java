package org.example.algorithm;

import com.google.gson.*;
import org.example.algorithm.emas.EMASSolutionGenerator;
import org.example.model.Agent;
import org.example.model.OptimizedFunction;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.GridPoint;
import org.example.util.SimulationData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.example.Main.runPythonScript;
import static org.example.util.UtilFunctions.getBestPerCategory;
import static org.example.util.UtilFunctions.saveToJson;

public abstract class Algorithm {
    protected static SimulationData simulationData = SimulationData.getInstance();
    protected static int iterations = 0;
    protected static Set<Agent> population = new HashSet<>();
    protected static Random random = new Random();
    private List<Map<OptimizedFunction, Float>> averageFunctionValues = new ArrayList<>();

    public Set<Solution> run() throws Exception {
        if (population.isEmpty()) {
            generateInitialPopulation();
        }

        long startTime = System.nanoTime();

        while (validState() && !checkStopCondition()) {
//            if (iterations % 250 == 0 && iterations != 0) {
//                Set<Solution> solutions = population.stream().map(Agent::getSolution).collect(Collectors.toSet());
//                solutions = getNonDominatedSolutions(solutions.stream().toList());
//                saveToJson(solutions, String.format("results/resultSolutions%d.json", iterations));
////                runPythonScript("scripts/plot_pareto_front.py", List.of("--routes", String.format("results/resultSolutions%d.json", iterations), "--resultFile", "pareto_front.png"));
//            }
            runIteration();
            iterations++;
            averageFunctionValues.add(calculateAverageFunctionValues());
            if (iterations % 500 == 0) {
                long nonElitePopulationSize = population.stream().filter(agent -> !agent.getIsland().isElite()).count();
                System.out.println("\nIteration: " + iterations + (iterations < 10000 ? "\t" : "" ) + "\tNon-elite population: " + nonElitePopulationSize + "\t\tElite: " + population.stream().filter(o -> o.getIsland().isElite()).toList().size() + "\tEpsilon: " + simulationData.paretoEpsilon);
                System.out.println("Avg engine load: " + Solution.fullNodePower / Solution.nodeNumber / SimulationData.getInstance().maxOutput + "\tAvg speed: " + Solution.fullNodeSpeed / Solution.nodeNumber);
                getBestPerCategory(population.stream().map(Agent::getSolution).collect(Collectors.toSet()));
            }
        }

        long stopTime = System.nanoTime();
        System.out.println("Execution time: " + (double) (stopTime - startTime) / 1000000000.0);

        saveToJson(averageFunctionValues, "results/averageValues.json");
        Set<Solution> solutions = population.stream().map(Agent::getSolution).collect(Collectors.toSet());
        Set<Solution> nonDominatedSolutions = getNonDominatedSolutions(null);
        System.out.println("\n\nSOLUTIONS: " + solutions.size());
        System.out.println("Max age: " + population.stream().map(Agent::getAge).max(Integer::compare).get());

        return nonDominatedSolutions;
    }

    private boolean validState() {
        return population != null;
    }

    protected boolean checkStopCondition() {
        return iterations > simulationData.maxIterations;
    }

    public static Set<Solution> getNonDominatedSolutions(List<Solution> solutions) {
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

    public static Solution generateInitialSolution(List<RoutePoint> route, double targetSpeed) {
        int maxTries = 50;
        for (RoutePoint routePoint : route) {
            routePoint.setShipSpeed(targetSpeed);
        }
        Solution solution = EMASSolutionGenerator.generateSolution(route);
        int counter = 0;
        do {
            if (counter != 0) {
                System.out.println("Initial population too dangerous: " + counter);
            }
            if (random.nextDouble() <= simulationData.initialMutationProbability) {
                solution = EMASSolutionGenerator.mutateSolution(solution, simulationData.initialMutationRate);
//                solution = EMASSolutionGenerator.mutateSolutionEta(solution, simulationData.mutationEta);
            }
            solution.calculateRouteValues();
            counter++;
        } while (solution.isTooDangerous() && counter < maxTries);
        if (counter == maxTries) {
            return null;
        }
        return solution;
    }

    protected static void loadInitialPopulation() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/initial-routes/initialSolutions.json"))) {
            // Read the entire content of the file into a string
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            JSONArray solutionsArray = new JSONArray(jsonString.toString());

            for (int i = 0; i < solutionsArray.length(); i++) {
                JSONObject solutionJson = solutionsArray.getJSONObject(i);
                JSONArray routePoints = solutionJson.getJSONArray("routePoints");
                List<RoutePoint> solutionRoutePoints = new ArrayList<>();

                for (int j = 0; j < routePoints.length(); j++) {
                    JSONObject pointJson = routePoints.getJSONObject(j);
                    JSONObject gridCoordinatesJson = pointJson.getJSONObject("gridCoordinates");
                    JSONObject coordinatesJson = pointJson.getJSONObject("coordinates");
                    int y = gridCoordinatesJson.getInt("y");
                    int x = gridCoordinatesJson.getInt("x");
                    double latitude = coordinatesJson.getDouble("latitude");
                    double longitude = coordinatesJson.getDouble("longitude");
                    double shipSpeed = pointJson.getDouble("shipSpeed");

                    GridPoint gridPoint = new GridPoint(y, x);
                    Coordinates coordinates = new Coordinates(latitude, longitude);
                    RoutePoint newPoint =  new RoutePoint(gridPoint, coordinates);
                    newPoint.setShipSpeed(shipSpeed);
                    solutionRoutePoints.add(newPoint);
                }
                Solution newSolution = new Solution(solutionRoutePoints);
                newSolution.calculateRouteValues();
                newSolution.calculateFunctionValues();
                population.add(new Agent(newSolution, simulationData.initialEnergy, 0, null, false));
            }

//            Set<Agent> additionalAgents = new HashSet<>();
//
//            for (int i = 0; i < population.size() - 1; i++) {
//                Solution currSolution = population.stream().toList().get(i).getSolution();
//                for (int j = i + 1; j < population.size(); j++) {
//                    Solution otherSolution = population.stream().toList().get(j).getSolution();
//
//                    List<GridPoint> commonGridPoints = new ArrayList<>();
//
//                    for (int k = 0; k < currSolution.getRoutePoints().size(); k++) {
//                        GridPoint currRoutePoint = currSolution.getRoutePoints().get(k).getGridCoordinates();
//                        GridPoint currPartnerRoutePoint = otherSolution.getRoutePoints().get(k).getGridCoordinates();
//                        if (currRoutePoint.equals(currPartnerRoutePoint)) {
//                            commonGridPoints.add(currRoutePoint);
//                        }
//                    }
//                    Solution newSolution = EMASSolutionGenerator.crossoverSolutions(currSolution, otherSolution, commonGridPoints, 1);
//                    newSolution = EMASSolutionGenerator.mutateSolution(newSolution, 0.4);
//                    newSolution.calculateRouteValues();
//                    newSolution.calculateFunctionValues();
//                    additionalAgents.add(new Agent(newSolution, simulationData.initialEnergy, 0, null, false));
//                }
//            }
//
//            additionalAgents.addAll(population);
//            List<Agent> populationList = new ArrayList<>(additionalAgents.stream().toList());
//            Collections.shuffle(populationList);
//            population = new HashSet<>(populationList.subList(0, population.size()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        try (FileReader reader = new FileReader("src/main/resources/initial-routes/initialSolutions.json")) {
//            JsonArray solutionsArray = JsonParser.parseReader(reader).getAsJsonArray();
//
//            for (int i = 0; i < solutionsArray.size(); i++) {
//                JSONObject jsonObject = solutionsArray.get(i);
//
//
//            for (JsonObject solutionJson : jsonObject) {
//                solutionJson.getAsJsonObject().getAsJsonArray();
//            }
////            for (Object solution : solutions) {
////                Solution solutionObj = (Solution) solution;
////                solutionObj.calculateRouteValues();
////                solutionObj.calculateFunctionValues();
////                population.add(new Agent(solutionObj, simulationData.initialEnergy, 0, null, false));
////            }
//        } catch (JsonSyntaxException e) {
//            System.err.println("JsonSyntaxException: " + e.getMessage());
//        } catch (IOException e) {
//            System.err.println("IOException: " + e.getMessage());
//        }
    }

    protected static void generateInitialPopulation() {
        List<List<RoutePoint>> startingRoutes = new ArrayList<>();
        double[] factors = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        int routesPerType = simulationData.populationSize / (factors.length + 2);
        List<RoutePoint> gcr = EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/great_circle_route.txt");
        List<Double> speedList = createSpeedList(simulationData.minSpeed, simulationData.maxSpeed, routesPerType);

        for (double factor : factors) {
            for (int i = 0; i < routesPerType; i++) {
                startingRoutes.add(EMASSolutionGenerator.flattenRoute(gcr, factor));
            }
        }
        for (int i = 0; i < simulationData.populationSize - routesPerType * factors.length; i++) {
            if (i % 2 == 0) {
                startingRoutes.add(EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/great_circle_route.txt"));
            } else {
                startingRoutes.add(EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/rhumb_line_route.txt"));
            }
        }
        for (int i = 0; i < startingRoutes.size(); i++) {
            List<RoutePoint> route = startingRoutes.get(i);
            Solution solution = generateInitialSolution(route, speedList.get(i % speedList.size()));
            if (solution != null) {
                solution.calculateFunctionValues();
                population.add(new Agent(solution, simulationData.initialEnergy, 0, null, false));
            }
        }
        while (population.size() != startingRoutes.size()) {
            List<RoutePoint> route = startingRoutes.get(random.nextInt(startingRoutes.size()));
            Solution solution = generateInitialSolution(route, speedList.get(random.nextInt(speedList.size())));
            if (solution != null) {
                solution.calculateFunctionValues();
                population.add(new Agent(solution, simulationData.initialEnergy, 0, null, false));
            }
        }
        System.out.println("\nSTARTING ROUTES: " + startingRoutes.size());
    }

    private static List<Double> createSpeedList(double minValue, double maxValue, double elements) {
        List<Double> res = new ArrayList<>();
        double step = (maxValue - minValue) / elements;
        for (int i = 0; i < elements; i++) {
            res.add(minValue + i * step);
        }
        return res;
    }

    public static Set<Solution> lastSolutionImprovement(Set<Solution> solutions, int improvements) {
        int counter = 0;
        List<Solution> nonDominatedSolutionsList = new ArrayList<>(solutions.stream().toList());
        for (int i = 0; i < nonDominatedSolutionsList.size(); i++) {
            for (int j = 0; j < improvements; j++) {
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

    private Map<OptimizedFunction, Float> calculateAverageFunctionValues() {
        Float totalTime = population.stream().map(a -> a.getSolution().getFunctionValues().get(OptimizedFunction.TravelTime)).reduce(Float::sum).get();
        Float totalFuel = population.stream().map(a -> a.getSolution().getFunctionValues().get(OptimizedFunction.FuelUsed)).reduce(Float::sum).get();
        Float totalSafety = population.stream().map(a -> a.getSolution().getFunctionValues().get(OptimizedFunction.Danger)).reduce(Float::sum).get();
        return Map.of(
                OptimizedFunction.TravelTime, totalTime / population.size(),
                OptimizedFunction.FuelUsed, totalFuel / population.size(),
                OptimizedFunction.Danger, totalSafety / population.size()
        );
    }

    protected abstract void runIteration() throws Exception;
}
