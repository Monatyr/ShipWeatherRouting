package org.example.algorithm.emas;

import org.example.model.RoutePoint;
import org.example.model.Solution;
import org.example.util.SimulationData;

import java.awt.geom.Point2D;
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
    private static SimulationData simulationData = SimulationData.getInstance();
    private static Random random = new Random();

    public static Solution generateSolution(List<RoutePoint> routePoints) {
        if (routePoints != null) {
            return new Solution(routePoints);
        }

        // TODO: better random path generation (currently the height between the current and target points is evened out by chance)

        // if points are null, create a random path
        RoutePoint startPos = new RoutePoint(simulationData.startPos, null, simulationData.startingTime);
        RoutePoint endPos = new RoutePoint(simulationData.endPos, null, 0);

        routePoints = new ArrayList<>();
        routePoints.add(startPos);
        double currHeight = startPos.getGridCoordinates().getY();
        double targetHeight = endPos.getGridCoordinates().getY();

        for (int i = 1; i < simulationData.mapWidth - 1; i++) {
            boolean getCloser = random.nextDouble(1.0) > 0.65;
            if (getCloser) {
                if (targetHeight > currHeight) {
                    currHeight++;
                } else if (targetHeight < currHeight) {
                    currHeight--;
                }
            }
            Point2D nextGridCoordinates = new Point2D.Double(i, currHeight);
            RoutePoint nextRoutePoint = new RoutePoint(nextGridCoordinates, null, 0);
            routePoints.add(nextRoutePoint);
        }

        routePoints.add(endPos);
        return new Solution(routePoints);
    }

    public static Solution generateSolution(Solution sol1, Solution sol2, List<Point2D> commonGridPoints) {
        Solution newSolution = crossoverSolutions(sol1, sol2, commonGridPoints);
        newSolution = mutateSolution(newSolution);
        newSolution.calculateRouteValues();
        return newSolution;
    }

    private static Solution crossoverSolutions(Solution sol1, Solution sol2, List<Point2D> commonGridPoints) {
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

            Point2D currCommonPoint = commonGridPoints.get(currCommonIndex);

            if (currCommonPoint.getX() == currPoint.getGridCoordinates().getX()
                    && currCommonPoint.getY() == currPoint.getGridCoordinates().getY()) {
                temp = sourcePoints;
                sourcePoints = otherPoints;
                otherPoints = temp;
                currCommonIndex++;
            }
        }

//        System.out.println(commonGridPoints);
        for (RoutePoint r : newRoutePoints) {
            System.out.print(r.getGridCoordinates() + " ");
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

            List<Integer> availableHeights = new ArrayList<>();

            int minY = (int) min(previousNeighbour.getGridCoordinates().getY(), nextNeighbour.getGridCoordinates().getY());
            int maxY = (int) max(previousNeighbour.getGridCoordinates().getY(), nextNeighbour.getGridCoordinates().getY());
            int currY = (int) currRoutePoint.getGridCoordinates().getY();

            // TODO: check if doesn't leave the grid
            for (int j = -simulationData.maxVerticalDistance; j <= simulationData.maxVerticalDistance; j++) {
                int potentialHeight = currY + j;
//                System.out.println(minY + " " + maxY + " " + potentialHeight);
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
            Point2D newGridCoordinates = new Point2D.Double(
                    currRoutePoint.getGridCoordinates().getX(),
                    newHeight
            );

            RoutePoint newRoutePoint = new RoutePoint(newGridCoordinates, null, 0);
            sol.getRoutePoints().set(pointIndex, newRoutePoint);
        }

        return sol;
    }
}


record PointWithIndex(RoutePoint routePoint, int index){}