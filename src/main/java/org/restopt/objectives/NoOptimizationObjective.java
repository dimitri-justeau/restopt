package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.util.criteria.Criterion;
import org.restopt.RestoptProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoOptimizationObjective extends AbstractRestoptObjective {

    public NoOptimizationObjective(RestoptProblem problem, int timeLimit, boolean verbose) {
        super(problem, timeLimit, verbose, true);
    }

    @Override
    public void initObjective() {
    }

    @Override
    public String getInitialValueMessage() {
        return "";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[0];
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        return new HashMap<>();
    }

    @Override
    public List<String[]> appendMessages() {
        return new ArrayList();
    }

    @Override
    public List<Solution> solve(int nbSolutions) {
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            solutions.add(problem.getModel().getSolver().findSolution());
        } else {
            solutions = findNSolutions(nbSolutions);
        }
        return solutions;
    }

    @Override
    public List<Solution> solve(int nbSolutions, Criterion stop) {
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            solutions.add(problem.getModel().getSolver().findSolution(stop));
        } else {
            solutions = findNSolutions(nbSolutions, stop);
        }
        return solutions;
    }
}
