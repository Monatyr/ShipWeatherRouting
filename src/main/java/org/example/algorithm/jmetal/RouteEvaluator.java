package org.example.algorithm.jmetal;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;
import java.util.Objects;


public class RouteEvaluator<S> implements SolutionListEvaluator<S> {
    @Override
    public List<S> evaluate(List<S> solutionList, Problem<S> problem) throws JMetalException {
//        RouteSolution.minTime = Double.POSITIVE_INFINITY;
//        RouteSolution.minFuel = Double.POSITIVE_INFINITY;
//        RouteSolution.minSafety = Double.POSITIVE_INFINITY;
//        RouteSolution.maxTime = Double.NEGATIVE_INFINITY;
//        RouteSolution.maxFuel = Double.NEGATIVE_INFINITY;
//        RouteSolution.maxSafety = Double.NEGATIVE_INFINITY;

        solutionList.forEach(problem::evaluate);
        return solutionList;
    }

    @Override
    public void shutdown() {
        // This method is an intentionally-blank override.
    }
}
