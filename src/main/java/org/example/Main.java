package org.example;

import org.example.algorithm.emas.EMAS;
import org.example.model.Solution;

import java.util.List;
import java.util.Set;

import static java.lang.Math.min;


public class Main {
    public static void main(String[] args) {
        EMAS emas = new EMAS();
        Set<Solution> solutions = emas.run();

        List<Solution> topSol = solutions.stream().sorted().toList().subList(0, min(solutions.size(), 10));
        System.out.println("\n\n--- SHOWING ONLY " + topSol.size() + " SOLUTIONS (sorted by the sum of function values) ---");
        for (Solution solution : topSol) {
            System.out.println(solution.getFunctionValues());
        }
    }
}