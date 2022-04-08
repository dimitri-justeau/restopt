package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
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
    protected long totalRuntime;

    protected boolean provenOptimal;

    public AbstractRestoptObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        this.problem = problem;
        this.timeLimit = timeLimit;
        this.verbose = verbose;
        this.maximize = maximize;
        this.provenOptimal = false;
    }

    public boolean isProvenOptimal() {
        return provenOptimal;
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

    public List<Solution> solve(int nbSolutions, int timeLimit) {
        long t = System.currentTimeMillis();
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            TimeCounter timeCounter = new TimeCounter(problem.getModel(), (long) (timeLimit * 1e9));
            solutions.add(problem.getModel().getSolver().findOptimalSolution(objective, maximize, timeCounter));
            if (String.valueOf(problem.getModel().getSolver().getSearchState()) == "TERMINATED") {
                provenOptimal = true;
            }
        } else {
            solutions = findNOptimalSolutions(objective, maximize, nbSolutions, timeLimit);
        }
        totalRuntime = System.currentTimeMillis() - t;
        return solutions;
    }

    public List<Solution> solve(int nbSolutions) {
        long t = System.currentTimeMillis();
        setSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            solutions.add(problem.getModel().getSolver().findOptimalSolution(objective, maximize));
            if (String.valueOf(problem.getModel().getSolver().getSearchState()) == "TERMINATED") {
                provenOptimal = true;
            }
        } else {
            solutions = findNOptimalSolutions(objective, maximize, nbSolutions, 0);
        }
        totalRuntime = System.currentTimeMillis() - t;
        return solutions;
    }

    public List<RestoptSolution> findOptimalSolution(int nbSolutions) {
        initObjective();
        Solver solver = problem.getModel().getSolver();
        if (verbose) {
            System.out.println(getInitialValueMessage());
            solver.showShortStatistics();
        }
        List<Solution> solutions;
        if (timeLimit > 0) {
            solutions = solve(nbSolutions, timeLimit);
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

    protected List<Solution> findNSolutions(int nbSolutions, int timeLimit) {
        Solver solver = problem.getModel().getSolver();
        solver.getModel().clearObjective();
        TimeCounter timeCounter = new TimeCounter(problem.getModel(), (long) (timeLimit * 1e9));
        if (timeLimit >= 0) {
            solver.addStopCriterion(timeCounter);
        }
        List<Solution> solutions = new ArrayList<>();
        int i = 0;
        while (solver.solve() && i < nbSolutions) {
            solutions.add(new Solution(solver.getModel()).record());
            i++;
        }
        solver.removeStopCriterion(timeCounter);
        return solutions;
    }

    private List<Solution> findNOptimalSolutions(IntVar objective, boolean maximize, int nbSolutions, int timeLimit) {
        Solver solver = problem.getModel().getSolver();
        boolean defaultS = solver.getSearch() == null;// best bound (in default) is only for optim
        TimeCounter timeCounter = null;
        provenOptimal = false;
        Solution optimal;
        if (timeLimit >= 0) {
            timeCounter = new TimeCounter(problem.getModel(), (long) (timeLimit * 1e9));
            optimal = solver.findOptimalSolution(objective, maximize, timeCounter);
        } else {
            optimal = solver.findOptimalSolution(objective, maximize);
        }
        if (String.valueOf(solver.getSearchState()) != "TERMINATED") {
            List<Solution> sols = new ArrayList<>();
            sols.add(optimal);
            return sols;
        } else {
            provenOptimal = true;
        }
        int nTimeLimit = Math.round(timeLimit - solver.getTimeCount());
        if (!solver.isStopCriterionMet()
                && solver.getSolutionCount() > 0) {
            if (timeCounter != null) {
                solver.removeStopCriterion(timeCounter);
            }
            int opt = solver.getObjectiveManager().getBestSolutionValue().intValue();
            solver.reset();
            solver.getModel().clearObjective();
            Constraint forceOptimal = solver.getModel().arithm(objective, "=", opt);
            forceOptimal.post();
            if (defaultS)
                solver.setSearch(Search.defaultSearch(solver.getModel()));// best bound (in default) is only for optim
            List<Solution> solutions;
            if (timeLimit > 0) {
                solutions = findNSolutions(nbSolutions, nTimeLimit);
            } else {
                solutions = findNSolutions(nbSolutions, -1);
            }
            solver.getModel().unpost(forceOptimal);
            return solutions;
        } else {
            solver.removeStopCriterion(timeCounter);
            return Collections.emptyList();
        }
    }

    public long getTotalRuntime() {
        return totalRuntime;
    }
}
