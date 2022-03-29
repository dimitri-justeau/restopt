package org.restopt.objectives;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;

import java.util.List;
import java.util.Map;

public abstract class AbstractRestoptObjective {

    protected RestoptProblem problem;
    protected IntVar objective;
    protected int timeLimit;
    protected boolean verbose;
    protected boolean maximize;
    protected Map<String, String> messages;


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

    public Solution solve(Criterion stop) {
        setSearch();
        return problem.getModel().getSolver().findOptimalSolution(objective, maximize, stop);
    }

    public Solution solve() {
        setSearch();
        return problem.getModel().getSolver().findOptimalSolution(objective, maximize);
    }

    public RestoptSolution findOptimalSolution() {
        initObjective();
        Model model = problem.getModel();
        Solver solver = problem.getModel().getSolver();
        if (verbose) {
            System.out.println(getInitialValueMessage());
            solver.showShortStatistics();
        }
        long t = System.currentTimeMillis();
        Solution solution;
        if (timeLimit > 0) {
            TimeCounter timeCounter = new TimeCounter(model, (long) (timeLimit * 1e9));
            solution = solve(timeCounter);
        } else {
            solution = solve();
        }
        if (solution == null) {
            if (verbose) {
                System.out.println("There is no solution satisfying the constraints");
            }
            return null;
        }
        return new RestoptSolution(problem, this, solution);
    }
}
