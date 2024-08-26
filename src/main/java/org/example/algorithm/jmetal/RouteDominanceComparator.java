package org.example.algorithm.jmetal;

import org.example.model.Solution;
import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;


public class RouteDominanceComparator implements DominanceComparator<RouteSolution> {
    private final double epsilon;
    public static int totalCounter = 0;
    public static int partCounter = 0;

    public RouteDominanceComparator(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public int compare(RouteSolution o1, RouteSolution o2) {
        Solution sol1 = o1.getSolution();
        Solution sol2 = o2.getSolution();
//        int res = sol1.checkIfDominates(sol2, true);
        return dominanceTest(o1, o2);
    }

    private int dominanceTest(RouteSolution solution1, RouteSolution solution2) {
        boolean bestIsOne = false;
        boolean bestIsTwo = false;
        totalCounter++;

        for(int i = 0; i < solution1.objectives().length; ++i) {
            double value1 = Math.floor(solution1.objectives()[i] / this.epsilon);
            double value2 = Math.floor(solution2.objectives()[i] / this.epsilon);
            if (value1 < value2) {
                bestIsOne = true;
                if (bestIsTwo) {
                    return 0;
                }
            } else if (value2 < value1) {
                bestIsTwo = true;
                if (bestIsOne) {
                    return 0;
                }
            }
        }

        partCounter++;
//        System.out.println(totalCounter + " " + partCounter);

        if (!bestIsOne && !bestIsTwo) {
            double dist1 = 0.0;
            double dist2 = 0.0;

            for(int i = 0; i < solution1.objectives().length; ++i) {
                double index1 = Math.floor(solution1.objectives()[i] / this.epsilon);
                double index2 = Math.floor(solution2.objectives()[i] / this.epsilon);
                dist1 += Math.pow(solution1.objectives()[i] - index1 * this.epsilon, 2.0);
                dist2 += Math.pow(solution2.objectives()[i] - index2 * this.epsilon, 2.0);
            }

            return dist1 < dist2 ? -1 : 1;
        } else {
            return bestIsTwo ? 1 : -1;
        }
    }

//    private int checkIfDominates(RouteSolution r1, RouteSolution r2) {
//        boolean bestIsOne = false;
//        boolean bestIsTwo = false;
//
//        Map<OptimizedFunction, Float> normalizedValues = new HashMap<>(this.functionValues);
//        Map<OptimizedFunction, Float> otherNormalizedValues = new HashMap<>(other.functionValues);
//        Float travelTime = normalizedValues.get(TravelTime);
//        Float otherTravelTime = otherNormalizedValues.get(TravelTime);
//        Float fuelUsed = normalizedValues.get(FuelUsed);
//        Float otherFuelUsed = otherNormalizedValues.get(FuelUsed);
//        Float safety = normalizedValues.get(Danger);
//        Float otherSafety = otherNormalizedValues.get(Danger);
//        normalizedValues.put(TravelTime, (float) ((travelTime - RouteSolution.minTime) / (RouteSolution.maxTime - RouteSolution.minTime)));
//        otherNormalizedValues.put(TravelTime, (float) ((otherTravelTime - RouteSolution.minTime) / (RouteSolution.maxTime - RouteSolution.minTime)));
//        normalizedValues.put(FuelUsed, (float) ((fuelUsed - RouteSolution.minFuel) / (RouteSolution.maxFuel - RouteSolution.minFuel)));
//        otherNormalizedValues.put(FuelUsed, (float) ((otherFuelUsed - RouteSolution.minFuel) / (RouteSolution.maxFuel - RouteSolution.minFuel)));
//        normalizedValues.put(Danger, (float) ((safety - RouteSolution.minSafety) / (RouteSolution.maxSafety - RouteSolution.minSafety)));
//        otherNormalizedValues.put(Danger, (float) ((otherSafety - RouteSolution.minSafety) / (RouteSolution.maxSafety - RouteSolution.minSafety)));
//
//        double epsilon = SimulationData.getInstance().paretoEpsilon;
//
//        List<OptimizedFunction> keyList = normalizedValues.keySet().stream().toList();
//
//        for (int i = 0; i < normalizedValues.size(); i++) {
//            OptimizedFunction key = keyList.get(i);
//            double value1 = Math.floor(normalizedValues.get(key) / epsilon);
//            double value2 = Math.floor(otherNormalizedValues.get(key) / epsilon);
//            if (value1 < value2) {
//                bestIsOne = true;
//
//                if (bestIsTwo) {
//                    return 0;
//                }
//            } else if (value2 < value1) {
//                bestIsTwo = true;
//
//                if (bestIsOne) {
//                    return 0;
//                }
//            }
//        }
//        if (!bestIsOne && !bestIsTwo) {
//            double dist1 = 0.0;
//            double dist2 = 0.0;
//
//            for (int i = 0; i < normalizedValues.size(); i++) {
//                OptimizedFunction key = keyList.get(i);
//
//                double index1 = Math.floor(normalizedValues.get(key) / epsilon);
//                double index2 = Math.floor(otherNormalizedValues.get(key) / epsilon);
//
//                dist1 += Math.pow(normalizedValues.get(key) - index1 * epsilon,
//                        2.0);
//                dist2 += Math.pow(otherNormalizedValues.get(key) - index2 * epsilon,
//                        2.0);
//            }
//
//            if (dist1 < dist2) {
//                return -1;
//            } else {
//                return 1;
//            }
//        } else if (bestIsTwo) {
//            return 1;
//        } else {
//            return -1;
//        }
//    }
}
