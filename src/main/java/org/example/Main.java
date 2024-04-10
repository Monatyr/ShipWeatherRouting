package org.example;

import org.example.algorithm.emas.EMAS;
import org.example.model.Solution;

import java.util.Set;


public class Main {
    public static void main(String[] args) {
        EMAS emas = new EMAS();
        Set<Solution> solutions = emas.run();

        for (Solution solution : solutions) {
            System.out.println(solution.getFunctionValues());
        }
    }
}