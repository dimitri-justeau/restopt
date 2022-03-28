package org.restopt.objectives;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.restopt.BaseProblem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRestoptObjective {

    public static final String KEY_MIN_RESTORE = "min_restore";
    public static final String KEY_TOTAL_RESTORABLE = "total_restorable";
    public static final String KEY_NB_PUS = "nb_planning_units";
    public static final String KEY_OPTIMALITY_PROVEN = "optimality_proven";
    public static final String KEY_SOLVING_TIME = "solving_time";
    public static final String[] KEYS = {
            KEY_MIN_RESTORE, KEY_TOTAL_RESTORABLE, KEY_NB_PUS, KEY_OPTIMALITY_PROVEN, KEY_SOLVING_TIME
    };

    protected BaseProblem problem;
    protected IntVar objective;
    protected int timeLimit;
    protected boolean verbose;
    protected boolean maximize;
    protected Map<String, String> messages;


    public AbstractRestoptObjective(BaseProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        this.problem = problem;
        this.timeLimit = timeLimit;
        this.verbose = verbose;
        this.maximize = maximize;
        initMessages();
    }

    public void initMessages() {
        this.messages = new HashMap<>();
        messages.put(KEY_MIN_RESTORE, "Minimum area to restore: ");
        messages.put(KEY_TOTAL_RESTORABLE, "Total restorable area: ");
        messages.put(KEY_NB_PUS, "Number of planning units: ");
        messages.put(KEY_OPTIMALITY_PROVEN, "Optimality proven: ");
        messages.put(KEY_SOLVING_TIME, "Solving time (seconds): ");
        for (String[] s : appendMessages()) {
            messages.put(s[0], s[1]);
        }
    }

    public abstract void initObjective();

    public abstract String getInitialValueMessage();

    public abstract String[] getAdditionalKeys();

    public abstract Map<String, String> appendCharacteristics(Solution solution);

    public abstract List<String[]> appendMessages();

    public Map<String, String> getSolutionCharacteristics(Solution solution) {
        Model model = problem.getModel();
        Map<String, String> solCharacteristics = new HashMap<>();
        solCharacteristics.put(KEY_MIN_RESTORE, String.valueOf(problem.getMinRestoreValue(solution)));
        solCharacteristics.put(KEY_TOTAL_RESTORABLE, String.valueOf(problem.getTotalRestorableValue(solution)));
        solCharacteristics.put(KEY_NB_PUS, String.valueOf(solution.getSetVal(problem.getRestoreSetVar()).length));
        solCharacteristics.put(KEY_SOLVING_TIME, String.valueOf(model.getSolver().getTimeCount()));
        solCharacteristics.put(KEY_OPTIMALITY_PROVEN, String.valueOf(problem.getSearchState() == "TERMINATED"));
        solCharacteristics.putAll(appendCharacteristics(solution));
        return solCharacteristics;
    }

    public Solution solve(Criterion stop) {
        return problem.getModel().getSolver().findOptimalSolution(objective, maximize, stop);
    }

    public Solution solve() {
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
