package org.restopt.objectives;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.BaseProblem;
import org.restopt.SolutionExporter;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

import java.io.IOException;
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
        solCharacteristics.put(KEY_TOTAL_RESTORABLE, String.valueOf(problem.getMaxRestorableValue(solution)));
        solCharacteristics.put(KEY_NB_PUS, String.valueOf(solution.getSetVal(problem.getRestoreSetVar()).length));
        solCharacteristics.put(KEY_SOLVING_TIME, String.valueOf(model.getSolver().getTimeCount()));
        solCharacteristics.put(KEY_OPTIMALITY_PROVEN, String.valueOf(problem.getSearchState() == "TERMINATED"));
        solCharacteristics.putAll(appendCharacteristics(solution));
        return solCharacteristics;
    }

    public void printSolutionInfos(Map<String, String> solutionCharacteristics) {
        System.out.println("\n--- Best solution ---\n");
        for (String key : KEYS) {
            System.out.println(messages.get(key) + solutionCharacteristics.get(key));
        }
        for (String key : getAdditionalKeys()) {
            System.out.println(messages.get(key) + solutionCharacteristics.get(key));
        }
    }

    public Solution solve(Criterion stop) {
        return problem.getModel().getSolver().findOptimalSolution(objective, maximize, stop);
    }

    public Solution solve() {
        return problem.getModel().getSolver().findOptimalSolution(objective, maximize);
    }

    public boolean findOptimalSolution(String outputPath) throws IOException {
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
            return false;
        }
        Map<String, String> characteristics = getSolutionCharacteristics(solution);
        exportSolution(outputPath, solution, characteristics);
        if (verbose) {
            printSolutionInfos(characteristics);
            System.out.println("\nRaster exported at " + outputPath + ".tif");
            System.out.println("Solution characteristics exported at " + outputPath + ".csv\n");
        }
        return true;
    }

    public void exportSolution(String outputPath, Solution solution, Map<String, String> characteristics) throws IOException {
        PartialRegularGroupedGrid grid = problem.getGrid();
        int[] sites = new int[grid.getNbUngroupedCells()];
        ISet set = SetFactory.makeConstantSet(solution.getSetVal(problem.getRestoreSetVar()));
        for (int i = 0; i < grid.getNbUngroupedCells(); i++) {
            if (grid.getGroupIndexFromPartialIndex(i) < grid.getNbGroups()) {
                sites[i] = 2;
            } else if (set.contains(grid.getGroupIndexFromPartialIndex(i))) {
                sites[i] = 3;
            } else {
                sites[i] = 1;
            }
        }
        SolutionExporter exporter = new SolutionExporter(
                problem,
                sites,
                problem.getData().getHabitatRasterPath(),
                outputPath + ".csv",
                outputPath + ".tif",
                problem.getData().noDataHabitat
        );
        String[][] orderedCharacteristics = new String[2][];
        String[] allKeys = ArrayUtils.append(KEYS, getAdditionalKeys());
        orderedCharacteristics[0] = allKeys;
        orderedCharacteristics[1] = new String[allKeys.length];
        for (int i = 0; i < allKeys.length; i++) {
            orderedCharacteristics[1][i] = characteristics.get(allKeys[i]);
        }
        exporter.exportCharacteristics(orderedCharacteristics);
        exporter.generateRaster();
    }
}
