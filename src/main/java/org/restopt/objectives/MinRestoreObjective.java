package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.restopt.RestoptProblem;
import org.restopt.exception.RestoptException;

import java.util.*;

public class MinRestoreObjective extends AbstractRestoptObjective {

    public static final String KEY_MIN_RESTORE = "min_restore";
    public static final String KEY_MIN_RESTORE_BEST = "min_restore_best";

    double minProportion;

    public MinRestoreObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize, String search) throws Exception {
        super(problem, timeLimit, verbose, maximize, search);
        if (problem.getMinRestore() == null) {
            throw new Exception("MinRestoreObjective without minProportion is only allowed" +
                    "if a restorable constraint was posted");
        }
    }

    public MinRestoreObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) throws Exception {
        this(problem, timeLimit, verbose, maximize, "");
    }

    public MinRestoreObjective(RestoptProblem problem, double minProportion, int timeLimit, boolean verbose, boolean maximize, String search) {
        super(problem, timeLimit, verbose, maximize, search);
        this.minProportion = minProportion;
    }

    @Override
    public void initObjective() {
        if (problem.getMinRestore() != null) {
            objective = problem.getMinRestore();
        } else {
            double maxRest = Arrays.stream(problem.getData().getRestorableData())
                    .filter(x -> !Double.isNaN(x))
                    .sum();
            try {
                problem.postRestorableConstraint(0, (int) maxRest, minProportion);
                objective = problem.getMinRestore();
            } catch (RestoptException e) {
                e.printStackTrace();
            }
        }
    }

/*    @Override
    public void setSearch() {
        if (problem.hasRestorableAreaConstraint()) {
            if (maximize) {
                new OrderedRestorableAreaStrategy(problem, true, true).setSearch();
            } else {
                new OrderedRestorableAreaStrategy(problem, false, true).setSearch();
            }
        } else {
            super.setSearch();
        }
    }*/

    @Override
    public String getInitialValueMessage() {
        return "";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]{KEY_MIN_RESTORE, KEY_MIN_RESTORE_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_MIN_RESTORE, String.valueOf(solution.getIntVal(objective)));
        charact.put(KEY_MIN_RESTORE_BEST, String.valueOf(optimalValue));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[]{KEY_MIN_RESTORE, "Min restore value: "});
        messages.add(new String[]{KEY_MIN_RESTORE_BEST, "Best known min restore value: "});
        return messages;
    }
}
