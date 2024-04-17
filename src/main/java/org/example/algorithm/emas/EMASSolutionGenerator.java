package org.example.algorithm.emas;


import org.example.model.Point;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.SimulationData;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.min;

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
    private static SimulationData simulationData = SimulationData.getInstance();
    private static Random random = new Random();

    public static Solution generateSolution(List<Point> points) {
        if (points != null) {
            return new Solution(points);
        }
        // if points are null, create a random path
        Point startPos = new Point(simulationData.startPos, 0, null, null);
        Point endPos = new Point(simulationData.endPos, 0, null, null);

        points = new ArrayList<>();
        points.add(startPos);

        for (int i = 1; i < simulationData.mapWidth - 1; i++) {
            Coordinates nextCoordinates = new Coordinates(random.nextInt(0, simulationData.mapHeight), i);
            Point nextPoint = new Point(nextCoordinates, 0, null, null);
            points.add(nextPoint);
        }

        points.add(endPos);
        return new Solution(points);
    }

    public static Solution generateSolution(Solution sol1, Solution sol2) {
        Solution newSolution = crossoverSolutions(sol1, sol2);
        newSolution = mutateSolution(newSolution);
        return newSolution;
    }


    private static Solution crossoverSolutions(Solution sol1, Solution sol2) {
        List<Point> points1 = sol1.getRoutePoints();
        List<Point> points2 = sol2.getRoutePoints();
        List<Point> newPoints = new ArrayList<>();

        boolean routeOneFirst = random.nextBoolean();

        if (!routeOneFirst) {
            List<Point> temp = points1;
            points1 = points2;
            points2 = temp;
        }

        int solutionLength = points1.size();
        int halfIndex = solutionLength / 2;

        newPoints.addAll(points1.subList(0, halfIndex));
        newPoints.addAll(points2.subList(halfIndex, solutionLength));

        //TODO: route smoothing at the connection point(s)

        return new Solution(newPoints);
    }

    // TODO: the function IS NOT COMPLETE! Mutation should be based on the distance between the points in the grid
    public static Solution mutateSolution(Solution sol) {
        List<PointWithIndex> pointsWithIndex = new ArrayList<>();
        for (int i = 1; i < sol.getRoutePoints().size() - 1; i++) {
            pointsWithIndex.add(new PointWithIndex(sol.getRoutePoints().get(i), i));
        }

        Collections.shuffle(pointsWithIndex);

        int cellsToMutate = min(
                (int) ((simulationData.mapWidth - 2) * simulationData.mutationRate),
                pointsWithIndex.size());

        for (int i = 0; i < cellsToMutate; i++) {
            Point currPoint = pointsWithIndex.get(i).point();
            int pointIndex = pointsWithIndex.get(i).index();

            Point previousNeighbour = sol.getRoutePoints().get(pointIndex - 1);
            Point nextNeighbour = sol.getRoutePoints().get(pointIndex + 1);

            /**
             * Points must be located on a grid with rows and columns or the latitude difference should be
             * calculated using minutes and seconds.
             */
            double previousHeightDiff = previousNeighbour.getCoordinates().latitude() - currPoint.getCoordinates().latitude();
            double nextHeightDiff = nextNeighbour.getCoordinates().latitude() - currPoint.getCoordinates().latitude();

            List<Integer> availableLatitudes = new ArrayList<>();

            for (int j = -simulationData.maxVerticalDistance; j <= simulationData.maxVerticalDistance; j++) {
                // TODO: check if doesn't leave the grid
                if (previousHeightDiff < j && nextHeightDiff < j) {
                    availableLatitudes.add(j);
                }
            }

            // if both neighbours are as far as they can be on opposite sides, don't mutate this point
            if (availableLatitudes.isEmpty()) {
                continue;
            }

            Collections.shuffle(availableLatitudes);
            int latitudeDiff = availableLatitudes.get(0);

            // TODO: insert real data and not randomly generated
            Coordinates newCoordinates = new Coordinates(
                    currPoint.getCoordinates().latitude() + latitudeDiff,
                    currPoint.getCoordinates().longitude());
            Point newPoint = new Point(newCoordinates, 0, null, null);
            sol.getRoutePoints().set(pointIndex, newPoint);
            System.out.println("NEW POINT");
        }

        return sol;
    }
}


record PointWithIndex(Point point, int index){}