package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.exception.RestoptException;

import java.util.*;

public abstract class AbstractRestoptObjective {

    private static Set<String> SEARCH_KEYS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "RANDOM",
                    "DOM_OVER_W_DEG",
                    "DOM_OVER_W_DEG_REF",
                    "MIN_DOM_LB",
                    "MIN_DOM_UB",
                    "ACTIVITY_BASED",
                    "CONFLICT_HISTORY",
                    "FAILURE_RATE",
                    "FAILURE_LENGTH"
            )));

    protected RestoptProblem problem;

    protected IntVar objective;
    protected int timeLimit;
    protected boolean verbose;
    protected boolean maximize;
    protected long totalRuntime;
    protected String search;
    protected boolean lns;
    protected BoolVar[] decisionVars;

    protected int optimalValue;

    protected boolean provenOptimal;

    public AbstractRestoptObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize, String search,
                                    boolean lns) {
        this.problem = problem;
        this.timeLimit = timeLimit;
        this.verbose = verbose;
        this.maximize = maximize;
        this.provenOptimal = false;
        this.search = search;
        this.lns = lns;
        this.decisionVars = null;
    }

    public AbstractRestoptObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        this(problem, timeLimit, verbose, maximize, "", false);
    }

    public boolean isProvenOptimal() {
        return provenOptimal;
    }

    public int getOptimalValue() {
        return optimalValue;
    }

    public IntVar getObjective() {
        return objective;
    }

    public void initDecisionVars() {
        decisionVars = new BoolVar[problem.getAvailablePlanningUnits().length];
        for (int i = 0; i < decisionVars.length; i++) {
            decisionVars[i] = problem.getModel().setBoolView(
                    problem.getRestoreSetVar(),
                    problem.getAvailablePlanningUnits()[i]
            );
        }
    }

    /**
     * Set the search strategy. Override according to the objective.
     */
    public void setSearch() {
        if (SEARCH_KEYS.contains(this.search)) {
            if (decisionVars == null) {
                initDecisionVars();
            }
            switch (this.search) {
                case "RANDOM":
                    problem.getModel().getSolver().setSearch(Search.randomSearch(decisionVars, System.currentTimeMillis()));
                    break;
                case "DOM_OVER_W_DEG":
                    problem.getModel().getSolver().setSearch(Search.domOverWDegSearch(decisionVars));
                    break;
                case "DOM_OVER_W_DEG_REF":
                    problem.getModel().getSolver().setSearch(Search.domOverWDegRefSearch(decisionVars));
                    break;
                case "MIN_DOM_LB":
                    problem.getModel().getSolver().setSearch(Search.minDomLBSearch(decisionVars));
                    break;
                case "MIN_DOM_UB":
                    problem.getModel().getSolver().setSearch(Search.minDomUBSearch(decisionVars));
                    break;
                case "ACTIVITY_BASED":
                    problem.getModel().getSolver().setSearch(Search.activityBasedSearch(decisionVars));
                    break;
                case "CONFLICT_HISTORY":
                    problem.getModel().getSolver().setSearch(Search.conflictHistorySearch(decisionVars));
                    break;
                case "FAILURE_RATE":
                    problem.getModel().getSolver().setSearch(Search.failureRateBasedSearch(decisionVars));
                    break;
                case "FAILURE_LENGTH":
                    problem.getModel().getSolver().setSearch(Search.failureLengthBasedSearch(decisionVars));
                    break;
            }
        } else {
            if (!this.search.equals("")) {
                System.out.println("Warning: the search strategy '" + this.search + "' does not exist. Setting default search");
            }
            problem.getModel().getSolver().setSearch(Search.setVarSearch(problem.getRestoreSetVar()));
        }
    }

    public void configureSearch() {
        setSearch();
        if (lns) {
            if (decisionVars == null) {
                initDecisionVars();
            }
            problem.getModel().getSolver().setLNS(INeighborFactory.random(decisionVars));
        }
    }

    public abstract void initObjective();

    public abstract String getInitialValueMessage();

    public abstract String[] getAdditionalKeys();

    public abstract Map<String, String> appendCharacteristics(Solution solution);

    public abstract List<String[]> appendMessages();

    public List<Solution> solve(int nbSolutions, int timeLimit, double optimalityGap) throws RestoptException {
        long t = System.currentTimeMillis();
        configureSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            TimeCounter timeCounter = new TimeCounter(problem.getModel(), (long) (timeLimit * 1e9));
            Solution solution = problem.getModel().getSolver().findOptimalSolution(objective, maximize, timeCounter);
            optimalValue = problem.getModel().getSolver().getObjectiveManager().getBestSolutionValue().intValue();
            if (solution != null) {
                solutions.add(solution);
            }
            if (String.valueOf(problem.getModel().getSolver().getSearchState()) == "TERMINATED") {
                provenOptimal = true;
            }
        } else {
            solutions = findNOptimalSolutions(objective, maximize, nbSolutions, timeLimit, optimalityGap);
        }
        totalRuntime = System.currentTimeMillis() - t;
        return solutions;
    }

    public List<Solution> solve(int nbSolutions, double optimalityGap) throws RestoptException {
        long t = System.currentTimeMillis();
        configureSearch();
        List<Solution> solutions;
        if (nbSolutions == 1) {
            solutions = new ArrayList<>();
            Solution opt = problem.getModel().getSolver().findOptimalSolution(objective, maximize);
            if (opt != null) {
                provenOptimal = true;
                solutions.add(opt);
                optimalValue = problem.getModel().getSolver().getObjectiveManager().getBestSolutionValue().intValue();
            }
        } else {
            solutions = findNOptimalSolutions(objective, maximize, nbSolutions, 0, optimalityGap);
        }
        totalRuntime = System.currentTimeMillis() - t;
        return solutions;
    }

    public List<RestoptSolution> findOptimalSolution(int nbSolutions, double optimalityGap) throws RestoptException {
        initObjective();
        Solver solver = problem.getModel().getSolver();
        if (verbose) {
            System.out.println(getInitialValueMessage());
            solver.showShortStatistics();
        }
        List<Solution> solutions;
        if (timeLimit > 0) {
            solutions = solve(nbSolutions, timeLimit, optimalityGap);
        } else {
            solutions = solve(nbSolutions, optimalityGap);
        }
        if (solutions.size() == 0) {
            if (verbose) {
                System.out.println("There is no solution satisfying the constraints");
            }
            return new ArrayList<>();
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
        if (this.timeLimit > 0) {
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

    private List<Solution> findNOptimalSolutions(IntVar objective, boolean maximize, int nbSolutions, int timeLimit, double optimalityGap) throws RestoptException {
        if (optimalityGap < 0 || optimalityGap > 1) {
            throw new RestoptException("Optimality gap must be comprised between 0 and 1");
        }
        Solver solver = problem.getModel().getSolver();
        boolean defaultS = solver.getSearch() == null;// best bound (in default) is only for optim
        TimeCounter timeCounter = null;
        provenOptimal = false;
        Solution optimal;
        if (timeLimit > 0) {
            timeCounter = new TimeCounter(problem.getModel(), (long) (timeLimit * 1e9));
            optimal = solver.findOptimalSolution(objective, maximize, timeCounter);
        } else {
            optimal = solver.findOptimalSolution(objective, maximize);
        }
        optimalValue = solver.getObjectiveManager().getBestSolutionValue().intValue();
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
            solver.reset();
            solver.getModel().clearObjective();
            String operator = maximize ? ">=" : "<=";
            int optWithGap = (int) (maximize ?
                    Math.ceil(optimalValue * (1 - optimalityGap)) : Math.floor(optimalValue * (1 + optimalityGap)));
            Constraint forceOptimal = solver.getModel().arithm(objective, operator, optWithGap);
            forceOptimal.post();
            if (defaultS)
                Search.defaultSearch(solver.getModel());// best bound (in default) is only for optim
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
