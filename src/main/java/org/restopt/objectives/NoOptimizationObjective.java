package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.limits.TimeCounter;
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
    public List<Solution> solve(int nbSolutions, double optimalityGap) {
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            Solution solution = problem.getModel().getSolver().findSolution();
            if (solution != null) {
                solutions.add(solution);
            }
        } else {
            solutions = findNSolutions(nbSolutions, -1);
        }
        return solutions;
    }

    @Override
    public List<Solution> solve(int nbSolutions, int timeLimit, double optimalityGap) {
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            TimeCounter timeCounter = new TimeCounter(problem.getModel(), (long) (timeLimit * 1e9));
            Solution solution = problem.getModel().getSolver().findSolution(timeCounter);
            if (solution != null) {
                solutions.add(solution);
            }
        } else {
            solutions = findNSolutions(nbSolutions, timeLimit);
        }
        return solutions;
    }
}
