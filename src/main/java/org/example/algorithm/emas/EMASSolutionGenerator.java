package org.example.algorithm.emas;


import org.example.model.Point;
import org.example.model.Solution;
import org.example.util.Coordinates;
import org.example.util.SimulationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            Coordinates nextCoordinates = new Coordinates((new Random()).nextInt(0, simulationData.mapHeight), i);
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

        // TODO: Mix the two routes.
        boolean routeOneFirst = (new Random()).nextBoolean();

        if (!routeOneFirst) {
            System.out.println("FIRST FIRST");
            List<Point> temp = points1;
            points1 = points2;
            points2 = temp;
        } else {
            System.out.println("SECOND FIRST");

        }

        int solutionLength = points1.size();
        int halfIndex = solutionLength / 2;

        newPoints.addAll(points1.subList(0, halfIndex));
        newPoints.addAll(points2.subList(halfIndex, solutionLength));

        return new Solution(newPoints);
    }


    public static Solution mutateSolution(Solution sol) {
        return sol;
    }
}
