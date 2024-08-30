package org.example.algorithm.emas;

import org.apache.commons.math3.util.Pair;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Math.*;

/**
 * The solution generator takes two solutions and creates a new route based on the routes of the parents.
 * <p>
 * I have to come up with a specific (and sophisticated) strategy for the creation process of new solutions.
 * A simple average between the latitudes of the points at a given leg might not prove to be too effective
 * (e.g. two completely different routes - one going far north and the other far south).
 * <p>
 * I could try randomly selecting certain portions of the two routes and gluing them together, while
 * also ensuring that the validity/sensibility of such an operation - connecting two very far away points
 * wouldn't be too practical.
 */
public class EMASSolutionGenerator {
    private static final SimulationData simulationData = SimulationData.getInstance();
    private static final Random random = new Random();
    private static final Coordinates[][] grid = createMapGrid();

    private static double minSpeed = Double.POSITIVE_INFINITY;

    public static Solution generateSolution(List<RoutePoint> routePoints) {
        if (routePoints != null) {
            return new Solution(routePoints);
        }
        return null;
    }

    public static Solution generateSolution(Solution sol1, Solution sol2, List<GridPoint> commonGridPoints) {
        Solution newSolution;
        int counter = 0;
        do {
            if (counter != 0) {
                System.out.println("DANGEROUS: " + counter);
            }
            newSolution = crossoverSolutions(sol1, sol2, commonGridPoints, simulationData.routeSwitches);
            newSolution = mutateSolution(newSolution, simulationData.mutationRate);
            newSolution.calculateRouteValues();
            counter++;
        } while (newSolution.isTooDangerous());// && counter < 10);
//        if (counter < 10) {
            newSolution.calculateFunctionValues();
            return newSolution;
//        }
//        return new Solution(sol1);
    }

    private static Coordinates[][] createMapGrid() {
        String gridString = "";

        double currLongitude;
        double currLatitude = simulationData.maxLatitude;
        double latitudeChange = (simulationData.maxLatitude - simulationData.minLatitude) / (simulationData.mapHeight - 1);
        double longitudeChange = (simulationData.endCoordinates.longitude() - simulationData.startCoordinates.longitude()) / (simulationData.mapWidth - 1);

        Coordinates[][] grid = new Coordinates[simulationData.mapHeight][];
        for (int i = 0; i < simulationData.mapHeight; i++) {
            currLongitude = simulationData.startCoordinates.longitude();
            grid[i] = new Coordinates[simulationData.mapWidth];
            for (int j = 0; j < simulationData.mapWidth; j++) {
                grid[i][j] = new Coordinates(currLatitude, currLongitude);
                gridString += currLatitude + ", " + currLongitude + "\n";
                currLongitude += longitudeChange;
            }
            currLatitude -= latitudeChange;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/map-grid.txt"));
            writer.write(gridString);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return grid;
    }

    /** Read generated route from file. Place the generated points at the nearest grid points to them. */
    private static List<Pair<Integer, Integer>> readRouteFromFile(String filename) {
        // Create a grid of points based on the size of the map. Need to define the range of latitudes and longitudes
        // that the map encompasses (e.g. upper left and lower right corners). Based on the number of rows and columns
        // create the grid of certain density. If two route points are put in the same spot in the grid, ignore one
        // of them. If the points are not in subsequent columns, then interpolate the ones between them.
        // If creating the route exceeds the maximum height difference between the points, then the user should
        // somehow be notified.
        List<Pair<Integer, Integer>> gridPlacement = new ArrayList<>();
        List<String> pathPointsList = null;
        try {
            pathPointsList = Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String point : pathPointsList) {
            List<Double> coordinates = Arrays.stream(point.split(",\s")).map(Double::valueOf).toList();
            double latitude = coordinates.get(0);
            double longitude = coordinates.get(1);
            Integer minX = 0;
            Integer minY = 0;
            double minDistance = Double.POSITIVE_INFINITY;
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    double newDistance = Coordinates.realDistance(grid[i][j].latitude(), grid[i][j].longitude(), latitude, longitude);
                    if (newDistance < minDistance) {
                        minDistance = newDistance;
                        minY = i;
                        minX = j;
                    }
                }
            }
            gridPlacement.add(new Pair<>(minY, minX));
        }
        return gridPlacement;
    }

    private static List<Pair<Integer, Integer>> removeDuplicates(List<Pair<Integer, Integer>> route) {
        List<Pair<Integer, Integer>> uniqueRoute = new ArrayList<>();
        uniqueRoute.add(route.get(0));
        for (int i = 1; i < route.size(); i++) {
            int lastX = uniqueRoute.get(uniqueRoute.size() - 1).getSecond();
            if (route.get(i).getSecond() != lastX) {
                uniqueRoute.add(route.get(i));
            }
        }
        // Make sure the last point is not replaced by the previous (if they have the same value on the x-axis).
        Pair<Integer, Integer> lastEl = uniqueRoute.get(uniqueRoute.size() - 1);
        if (lastEl.getSecond() == route.get(route.size() - 1).getSecond()) {
            uniqueRoute.remove(uniqueRoute.size() - 1);
            uniqueRoute.add(route.get(route.size() - 1));
        }
        return uniqueRoute;
    }

    private static List<Pair<Integer, Integer>> fillMissingColumns(List<Pair<Integer, Integer>> route) {
        int xChange = route.get(0).getSecond() < route.get(1).getSecond() ? 1 : -1;
        List<Pair<Integer, Integer>> newRoute = new ArrayList<>();
        newRoute.add(route.get(0));
        for (int i = 1; i < route.size(); i++) {
            Pair<Integer, Integer> prev = route.get(i - 1);
            Pair<Integer, Integer> curr = route.get(i);
            int distance = abs(curr.getSecond() - prev.getSecond());
            if (distance == 1) {
                newRoute.add(curr);
                continue;
            }
            double yChange = (double) (curr.getFirst() - prev.getFirst()) / distance;
            for (int j = 1; j < distance; j++) {
                Pair<Integer, Integer> newPoint = new Pair<>((int) ((j * yChange) + prev.getFirst()), prev.getSecond() + xChange * j);
                newRoute.add(newPoint);
            }
            newRoute.add(curr);
        }
        return newRoute;
    }

    public static List<RoutePoint> getRouteFromFile(String filename) {
        List<Pair<Integer, Integer>> gridRoute = readRouteFromFile(filename);
        gridRoute = removeDuplicates(gridRoute);
        gridRoute = fillMissingColumns(gridRoute);
        List<RoutePoint> route = new ArrayList<>();
        for (Pair<Integer, Integer> gridPoint : gridRoute) {
            GridPoint gridCoords = new GridPoint(gridPoint.getFirst(), gridPoint.getSecond());
            Coordinates coordinates = grid[gridCoords.y()][gridCoords.x()];
            RoutePoint routePoint = new RoutePoint(gridCoords, coordinates);
            route.add(routePoint);
        }
        return route;
    }

    public static List<RoutePoint> flattenRoute(List<RoutePoint> arcPoints, double flattenFactor) {
        List<RoutePoint> flattenedPoints = new ArrayList<>();
        flattenedPoints.add(new RoutePoint(arcPoints.get(0)));
        int n = arcPoints.size();

        // Calculate the direction vector from start to end (AB)
        double[] AB = new double[]{arcPoints.get(n-1).getGridCoordinates().y() - arcPoints.get(0).getGridCoordinates().y(), arcPoints.get(n-1).getGridCoordinates().x() - arcPoints.get(0).getGridCoordinates().x()};
        double ABNorm = Math.sqrt(AB[0] * AB[0] + AB[1] * AB[1]);
        double[] ABNormalized = new double[]{AB[0] / ABNorm, AB[1] / ABNorm};

        for (int i = 1; i < n - 1; i++) {
            RoutePoint point = arcPoints.get(i);
            // Calculate vector AP (from start to point P)
            double[] AP = new double[]{point.getGridCoordinates().y() - arcPoints.get(0).getGridCoordinates().y(), point.getGridCoordinates().x() - arcPoints.get(0).getGridCoordinates().x()};

            // Projection of AP onto AB (dot product)
            double projectionLength = AP[0] * ABNormalized[0] + AP[1] * ABNormalized[1];

            // Closest point on the line from start to end
            double[] closestPoint = new double[]{
                    arcPoints.get(0).getGridCoordinates().y() + projectionLength * ABNormalized[0],
                    arcPoints.get(0).getGridCoordinates().x() + projectionLength * ABNormalized[1]
            };

            int newY = (int) (point.getGridCoordinates().y() + flattenFactor * (closestPoint[0] - point.getGridCoordinates().y()));

            int prevY = flattenedPoints.get(i - 1).getGridCoordinates().y();
            if (Math.abs(newY - prevY) > simulationData.maxVerticalDistance) {
                newY = prevY + (newY > prevY ? simulationData.maxVerticalDistance : -simulationData.maxVerticalDistance);  // Adjust the Y value to respect the maximum difference of 2
            }
            GridPoint gridCoords = new GridPoint(newY, point.getGridCoordinates().x());
            Coordinates coordinates = grid[gridCoords.y()][gridCoords.x()];
            RoutePoint routePoint = new RoutePoint(gridCoords, coordinates);
            flattenedPoints.add(routePoint);
        }
        flattenedPoints.add(new RoutePoint(arcPoints.get(n-1)));
        return flattenedPoints;
    }

    public static Coordinates calculateCoordinates(GridPoint gridPoint) {
        return grid[gridPoint.y()][gridPoint.x()];
    }

    public static Solution crossoverSolutions(Solution sol1, Solution sol2, List<GridPoint> commonGridPoints, int numOfSwitches) {
        List<RoutePoint> sourcePoints = sol1.getRoutePoints();
        List<RoutePoint> otherPoints = sol2.getRoutePoints();
        if (numOfSwitches < 0 || numOfSwitches > commonGridPoints.size()) {
            numOfSwitches = commonGridPoints.size();
        }
        final int[] switchPointsIndices = Arrays.stream(new Random().ints(0, commonGridPoints.size()).distinct().limit(numOfSwitches).toArray()).sorted().toArray();
        List<GridPoint> switchPoints = new ArrayList<>();
        for (int index : switchPointsIndices) {
            switchPoints.add(commonGridPoints.get(index));
        }

        List<RoutePoint> newRoutePoints = new ArrayList<>();
        int solutionLength = sourcePoints.size();
        boolean routeOneFirst = random.nextBoolean();
        List<RoutePoint> temp;
        if (!routeOneFirst) {
            temp = sourcePoints;
            sourcePoints = otherPoints;
            otherPoints = temp;
        }

        int currCommonIndex = 0;
        GridPoint currCommonPoint = null;
        if (switchPoints.size() != 0) {
            currCommonPoint = switchPoints.get(currCommonIndex);
        }
        for (int i = 0; i < solutionLength; i++) {
            RoutePoint currPoint = sourcePoints.get(i);
            RoutePoint newPoint = new RoutePoint(currPoint);
            newRoutePoints.add(newPoint);
            if (switchPoints.size() != 0) {
                if (currCommonIndex < switchPoints.size()) {
                    currCommonPoint = switchPoints.get(currCommonIndex);
                }
                if (currPoint.getGridCoordinates().x() == currCommonPoint.x() && currPoint.getGridCoordinates().y() == currCommonPoint.y()) {
                    temp = sourcePoints;
                    sourcePoints = otherPoints;
                    otherPoints = temp;
                    currCommonIndex++;
                }
            }
        }
        return new Solution(newRoutePoints);
    }

    public static Solution mutateSolution(Solution sol, double mutationRate) {
        Solution mutatedSolution = new Solution(sol);
        List<PointWithIndex> pointsWithIndex = new ArrayList<>();
        for (int i = 1; i < mutatedSolution.getRoutePoints().size() - 1; i++) {
            pointsWithIndex.add(new PointWithIndex(mutatedSolution.getRoutePoints().get(i), i));
        }
        Collections.shuffle(pointsWithIndex);
        int cellsToMutate = min(
            (int) ((simulationData.mapWidth - 2) * mutationRate),
            pointsWithIndex.size()
        );

//        System.out.println((int) ((simulationData.mapWidth - 2) * mutationRate));

        for (int i = 0; i < cellsToMutate; i++) {
            RoutePoint currRoutePoint = pointsWithIndex.get(i).routePoint();
            int pointIndex = pointsWithIndex.get(i).index();
            RoutePoint previousNeighbour = mutatedSolution.getRoutePoints().get(pointIndex - 1);
            RoutePoint nextNeighbour = mutatedSolution.getRoutePoints().get(pointIndex + 1);
            /**
             * Points must be located on a grid with rows and columns or the latitude difference should be
             * calculated using minutes and seconds.
             */
            List<Integer> availableHeights = new ArrayList<>();
            int minY = min(previousNeighbour.getGridCoordinates().y(), nextNeighbour.getGridCoordinates().y());
            int maxY = max(previousNeighbour.getGridCoordinates().y(), nextNeighbour.getGridCoordinates().y());
            int currY = currRoutePoint.getGridCoordinates().y();
            for (int j = -simulationData.maxVerticalDistance; j <= simulationData.maxVerticalDistance; j++) {
                int potentialHeight = currY + j;
                if (
                        potentialHeight < 0 ||
                        potentialHeight >= simulationData.mapHeight ||
                        !simulationData.checkIfWater(currRoutePoint.getCoordinates())
                ) {
                    continue;
                }
                if (abs(minY - potentialHeight) <= simulationData.maxVerticalDistance &&
                        abs(maxY - potentialHeight) <= simulationData.maxVerticalDistance// &&
//                        potentialHeight != currY
                ) {
                    availableHeights.add(potentialHeight);
                }
            }
            // if both neighbours are as far as they can be on opposite sides, don't mutate this point
            if (availableHeights.isEmpty()) {
                continue;
            }
            Collections.shuffle(availableHeights);
            int newHeight = availableHeights.get(0);
            GridPoint newGridCoordinates = new GridPoint(newHeight, currRoutePoint.getGridCoordinates().x());
            RoutePoint newRoutePoint = new RoutePoint(newGridCoordinates, calculateCoordinates(newGridCoordinates)); // TODO: does the arrival time get updated in the calculateRouteValues function?

            // Mutate speed
            double mutatedSpeed = currRoutePoint.getShipSpeed();
            double speedChange = random.nextDouble(-0.5, 0.5);
            if (mutatedSpeed + speedChange >= simulationData.minSpeed && mutatedSpeed + speedChange <= simulationData.maxSpeed) {
                mutatedSpeed += speedChange;
            }
            newRoutePoint.setShipSpeed(mutatedSpeed);

            mutatedSolution.getRoutePoints().set(pointIndex, newRoutePoint);
        }
        mutatedSolution.calculateRouteValues();
        mutatedSolution.calculateFunctionValues();
        return mutatedSolution;
    }

    public static void main(String[] args) {
        List<RoutePoint> gcr = EMASSolutionGenerator.getRouteFromFile("src/main/resources/initial-routes/great_circle_route.txt");
        gcr.forEach(p -> System.out.println(p.getCoordinates().longitude() + ", " + p.getCoordinates().latitude()));
        EMASSolutionGenerator.flattenRoute(gcr, 0.1).forEach(p -> System.out.println(p.getCoordinates().longitude() + ", " + p.getCoordinates().latitude()));
        EMASSolutionGenerator.flattenRoute(gcr, 0.2).forEach(p -> System.out.println(p.getCoordinates().longitude() + ", " + p.getCoordinates().latitude()));
        EMASSolutionGenerator.flattenRoute(gcr, 0.4).forEach(p -> System.out.println(p.getCoordinates().longitude() + ", " + p.getCoordinates().latitude()));
        EMASSolutionGenerator.flattenRoute(gcr, 0.6).forEach(p -> System.out.println(p.getCoordinates().longitude() + ", " + p.getCoordinates().latitude()));
    }
}


record PointWithIndex(RoutePoint routePoint, int index){}