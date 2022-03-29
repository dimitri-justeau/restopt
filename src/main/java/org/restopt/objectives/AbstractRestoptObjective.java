package org.restopt.objectives;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractRestoptObjective {

    protected RestoptProblem problem;
    protected IntVar objective;
    protected int timeLimit;
    protected boolean verbose;
    protected boolean maximize;

    public AbstractRestoptObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        this.problem = problem;
        this.timeLimit = timeLimit;
        this.verbose = verbose;
        this.maximize = maximize;
    }

    /**
     * Set the search strategy. Override according to the objective.
     */
    public void setSearch() {
        problem.getModel().getSolver().setSearch(Search.setVarSearch(problem.getRestoreSetVar()));
    }

    public abstract void initObjective();

    public abstract String getInitialValueMessage();

    public abstract String[] getAdditionalKeys();

    public abstract Map<String, String> appendCharacteristics(Solution solution);

    public abstract List<String[]> appendMessages();

    public List<Solution> solve(int nbSolutions, Criterion stop) {
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            solutions.add(problem.getModel().getSolver().findOptimalSolution(objective, maximize, stop));
        } else {
            solutions = findNOptimalSolutions(objective, maximize, nbSolutions, stop);
        }
        return solutions;
    }

    public List<Solution> solve(int nbSolutions) {
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            solutions.add(problem.getModel().getSolver().findOptimalSolution(objective, maximize));
        } else {
            solutions = findNOptimalSolutions(objective, maximize, nbSolutions);
        }
        return solutions;
    }

    public List<RestoptSolution> findOptimalSolution(int nbSolutions) {
        initObjective();
        Model model = problem.getModel();
        Solver solver = problem.getModel().getSolver();
        if (verbose) {
            System.out.println(getInitialValueMessage());
            solver.showShortStatistics();
        }
        long t = System.currentTimeMillis();
        List<Solution> solutions;
        if (timeLimit > 0) {
            TimeCounter timeCounter = new TimeCounter(model, (long) (timeLimit * 1e9));
            solutions = solve(nbSolutions, timeCounter);
        } else {
            solutions = solve(nbSolutions);
        }
        if (solutions.size() == 0) {
            if (verbose) {
                System.out.println("There is no solution satisfying the constraints");
            }
            return null;
        }
        List<RestoptSolution> restoptSolutions = new ArrayList<>();
        for (Solution s : solutions) {
            restoptSolutions.add(new RestoptSolution(problem, this, s));
        }
        return restoptSolutions;
    }

    protected List<Solution> findNSolutions(int nbSolutions, Criterion... stop) {
        Solver solver = problem.getModel().getSolver();
        solver.getModel().clearObjective();
        solver.addStopCriterion(stop);
        List<Solution> solutions = new ArrayList<>();
        int i = 0;
        while (solver.solve() && i < nbSolutions) {
            solutions.add(new Solution(solver.getModel()).record());
            i++;
        }
        solver.removeStopCriterion(stop);
        return solutions;
    }

    private List<Solution> findNOptimalSolutions(IntVar objective, boolean maximize, int nbSolutions, Criterion... stop) {
        Solver solver = problem.getModel().getSolver();
        boolean defaultS = solver.getSearch() == null;// best bound (in default) is only for optim
        solver.findOptimalSolution(objective, maximize);
        if (!solver.isStopCriterionMet()
                && solver.getSolutionCount() > 0) {
            solver.removeStopCriterion(stop);
            int opt = solver.getObjectiveManager().getBestSolutionValue().intValue();
            solver.reset();
            solver.getModel().clearObjective();
            Constraint forceOptimal = solver.getModel().arithm(objective, "=", opt);
            forceOptimal.post();
            if (defaultS)
                solver.setSearch(Search.defaultSearch(solver.getModel()));// best bound (in default) is only for optim
            List<Solution> solutions = findNSolutions(nbSolutions, stop);
            solver.getModel().unpost(forceOptimal);
            return solutions;
        } else {
            solver.removeStopCriterion(stop);
            return Collections.emptyList();
        }
    }
}
