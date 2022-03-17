package org.restopt.objective;

import org.chocosolver.solver.Solution;
import org.chocosolver.util.criteria.Criterion;
import org.restopt.BaseProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoOptimizationObjective extends AbstractObjective {

    public NoOptimizationObjective(BaseProblem problem, int timeLimit, boolean verbose) {
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
    public Solution solve() {
        return problem.getModel().getSolver().findSolution();
    }

    @Override
    public Solution solve(Criterion stop) {
        return problem.getModel().getSolver().findSolution(stop);
    }
}
