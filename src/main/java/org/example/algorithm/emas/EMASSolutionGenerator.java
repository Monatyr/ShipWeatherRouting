package org.example.algorithm.emas;


import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.SimulationData;

import java.awt.geom.Point2D;
import java.util.*;
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

    public static Solution generateSolution(List<RoutePoint> routePoints) {
        if (routePoints != null) {
            return new Solution(routePoints);
        }
        // if points are null, create a random path
        RoutePoint startPos = new RoutePoint(simulationData.startPos, null, 0, null, null);
        RoutePoint endPos = new RoutePoint(simulationData.endPos, null, 0, null, null);

        routePoints = new ArrayList<>();
        routePoints.add(startPos);

        for (int i = 1; i < simulationData.mapWidth - 1; i++) {
            Point2D nextGridCoordinates = new Point2D.Double(i, random.nextInt(0, simulationData.mapHeight));
            RoutePoint nextRoutePoint = new RoutePoint(nextGridCoordinates, null, 0, null, null);
            routePoints.add(nextRoutePoint);
        }

        routePoints.add(endPos);
        return new Solution(routePoints);
    }

    public static Solution generateSolution(Solution sol1, Solution sol2) {
        Solution newSolution = crossoverSolutions(sol1, sol2);
        newSolution = mutateSolution(newSolution);
        return newSolution;
    }


    private static Solution crossoverSolutions(Solution sol1, Solution sol2) {
        List<RoutePoint> points1 = sol1.getRoutePoints();
        List<RoutePoint> points2 = sol2.getRoutePoints();
        List<RoutePoint> newRoutePoints = new ArrayList<>();

        boolean routeOneFirst = random.nextBoolean();

        if (!routeOneFirst) {
            List<RoutePoint> temp = points1;
            points1 = points2;
            points2 = temp;
        }

        int solutionLength = points1.size();
        int halfIndex = solutionLength / 2;

        newRoutePoints.addAll(points1.subList(0, halfIndex));
        newRoutePoints.addAll(points2.subList(halfIndex, solutionLength));

        //TODO: route smoothing at the connection point(s) - maybe through the use of a shortest path algorithm

        return new Solution(newRoutePoints);
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
            RoutePoint currRoutePoint = pointsWithIndex.get(i).routePoint();
            int pointIndex = pointsWithIndex.get(i).index();

            RoutePoint previousNeighbour = sol.getRoutePoints().get(pointIndex - 1);
            RoutePoint nextNeighbour = sol.getRoutePoints().get(pointIndex + 1);

            /**
             * Points must be located on a grid with rows and columns or the latitude difference should be
             * calculated using minutes and seconds.
             */
            double previousHeightDiff = previousNeighbour.getGridCoordinates().getY() - currRoutePoint.getGridCoordinates().getY();
            double nextHeightDiff = nextNeighbour.getGridCoordinates().getY() - currRoutePoint.getGridCoordinates().getY();

            List<Integer> availableHeights = new ArrayList<>();

            for (int j = -simulationData.maxVerticalDistance; j <= simulationData.maxVerticalDistance; j++) {
                // TODO: check if doesn't leave the grid
                if (previousHeightDiff < j && nextHeightDiff < j && j != 0) {
                    availableHeights.add(j);
                }
            }

            // if both neighbours are as far as they can be on opposite sides, don't mutate this point
            if (availableHeights.isEmpty()) {
                continue;
            }

            Collections.shuffle(availableHeights);
            int heightDiff = availableHeights.get(0);

            // TODO: insert real data and not randomly generated
            Point2D newGridCoordinates = new Point2D.Double(
                    currRoutePoint.getGridCoordinates().getX(),
                    currRoutePoint.getGridCoordinates().getY() + heightDiff
            );

            RoutePoint newRoutePoint = new RoutePoint(newGridCoordinates, null, 0, null, null);
            sol.getRoutePoints().set(pointIndex, newRoutePoint);
            System.out.println("NEW POINT");
        }

        return sol;
    }
}


record PointWithIndex(RoutePoint routePoint, int index){}