package org.example.algorithm.emas;

import org.apache.commons.math3.util.Pair;
import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.GridPoint;
import org.example.util.SimulationData;

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

    public static Solution generateSolution(List<RoutePoint> routePoints) {
        if (routePoints != null) {
            return new Solution(routePoints);
        }
        return null;
    }

    public static Solution generateSolution(Solution sol1, Solution sol2, List<GridPoint> commonGridPoints) {
//        System.out.println(sol1.getRoutePoints().stream().map(RoutePoint::getGridCoordinates).toList());
        Solution newSolution = crossoverSolutions(sol1, sol2, commonGridPoints);
        newSolution = mutateSolution(newSolution);
        newSolution.calculateRouteValues();
        return newSolution;
    }

    private static Coordinates[][] createMapGrid() {
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
                currLongitude += longitudeChange;
            }
            currLatitude -= latitudeChange;
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
//        System.out.println(route);
        int xChange = route.get(0).getSecond() < route.get(1).getSecond() ? 1 : -1;
        List<Pair<Integer, Integer>> newRoute = new ArrayList<>();
        newRoute.add(route.get(0));
        for (int i = 1; i < route.size(); i++) {
            Pair<Integer, Integer> prev = route.get(i - 1);
            Pair<Integer, Integer> curr = route.get(i);
            int distance = abs(curr.getSecond() - prev.getSecond());
            if (distance == 1) {
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
            RoutePoint routePoint = new RoutePoint(gridCoords, coordinates, 0);
            route.add(routePoint);
        }
        System.out.println(route.size() + " " + route.stream().map(RoutePoint::getGridCoordinates).toList());
        return route;
    }

    public static Coordinates calculateCoordinates(GridPoint gridPoint) {
        return grid[gridPoint.y()][gridPoint.x()];
    }

    private static Solution crossoverSolutions(Solution sol1, Solution sol2, List<GridPoint> commonGridPoints) {
        List<RoutePoint> sourcePoints = sol1.getRoutePoints();
        List<RoutePoint> otherPoints = sol2.getRoutePoints();
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
        // the idea is to switch the source of the route at each intersection point
        for (int i = 0; i < solutionLength; i++) {
            RoutePoint currPoint = sourcePoints.get(i);
            RoutePoint newPoint = new RoutePoint(currPoint);
            newRoutePoints.add(newPoint);
            GridPoint currCommonPoint = commonGridPoints.get(currCommonIndex);
//            System.out.println(sourcePoints.stream().map(RoutePoint::getGridCoordinates).toList());
//            System.out.println(otherPoints.stream().map(RoutePoint::getGridCoordinates).toList());
//            System.out.println(currCommonPoint);
//            System.out.println();
            if (currCommonPoint.x() == currPoint.getGridCoordinates().x()
                    && currCommonPoint.y() == currPoint.getGridCoordinates().y()) {
                temp = sourcePoints;
                sourcePoints = otherPoints;
                otherPoints = temp;
                currCommonIndex++;
            }
        }
        return new Solution(newRoutePoints);
    }

    public static Solution mutateSolution(Solution sol) {
        List<PointWithIndex> pointsWithIndex = new ArrayList<>();
        for (int i = 1; i < sol.getRoutePoints().size() - 1; i++) {
            pointsWithIndex.add(new PointWithIndex(sol.getRoutePoints().get(i), i));
        }
        Collections.shuffle(pointsWithIndex);
        int cellsToMutate = min(
            (int) ((simulationData.mapWidth - 2) * simulationData.mutationRate),
            pointsWithIndex.size()
        );

        for (int i = 0; i < cellsToMutate; i++) {
            RoutePoint currRoutePoint = pointsWithIndex.get(i).routePoint();
            int pointIndex = pointsWithIndex.get(i).index();
            RoutePoint previousNeighbour = sol.getRoutePoints().get(pointIndex - 1);
            RoutePoint nextNeighbour = sol.getRoutePoints().get(pointIndex + 1);
            /**
             * Points must be located on a grid with rows and columns or the latitude difference should be
             * calculated using minutes and seconds.
             */
            List<Integer> availableHeights = new ArrayList<>();
            int minY = (int) min(previousNeighbour.getGridCoordinates().y(), nextNeighbour.getGridCoordinates().y());
            int maxY = (int) max(previousNeighbour.getGridCoordinates().y(), nextNeighbour.getGridCoordinates().y());
            int currY = (int) currRoutePoint.getGridCoordinates().y();
            for (int j = -simulationData.maxVerticalDistance; j <= simulationData.maxVerticalDistance; j++) {
                int potentialHeight = currY + j;
                if (potentialHeight < 0 || potentialHeight >= simulationData.mapHeight) {
                    continue;
                }
                if (abs(minY - potentialHeight) <= simulationData.maxVerticalDistance &&
                        abs(maxY - potentialHeight) <= simulationData.maxVerticalDistance &&
                        potentialHeight != currY
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
            // TODO: insert real time data from an API and not randomly generated
            GridPoint newGridCoordinates = new GridPoint(newHeight, currRoutePoint.getGridCoordinates().x());
            RoutePoint newRoutePoint = new RoutePoint(newGridCoordinates, calculateCoordinates(newGridCoordinates), 0);
            sol.getRoutePoints().set(pointIndex, newRoutePoint);
        }
        return sol;
    }
}


record PointWithIndex(RoutePoint routePoint, int index){}