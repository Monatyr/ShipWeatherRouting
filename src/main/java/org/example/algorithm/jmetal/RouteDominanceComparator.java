package org.example.algorithm.jmetal;

import org.example.model.Solution;
import org.uma.jmetal.util.comparator.dominanceComparator.DominanceComparator;

public class RouteDominanceComparator implements DominanceComparator<RouteSolution> {
    private boolean epsilonDominance;

    public RouteDominanceComparator(boolean epsilonDominance) {
        this.epsilonDominance = epsilonDominance;
    }

    @Override
    public int compare(RouteSolution o1, RouteSolution o2) {
        Solution sol1 = o1.getSolution();
        Solution sol2 = o2.getSolution();
        int res = sol1.checkIfDominates(sol2, epsilonDominance);
        if (res == -1) {
            return 1;
        } else if (res == 1) {
            return -1;
        }
        return 0;
    }
}
