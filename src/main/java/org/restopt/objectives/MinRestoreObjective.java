package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.restopt.RestoptProblem;
import org.restopt.exception.RestoptException;

import java.io.IOException;
import java.util.*;

public class MinRestoreObjective extends AbstractRestoptObjective {

    public static final String KEY_MIN_RESTORE_INITIAL = "min_restore_initial";
    public static final String KEY_MIN_RESTORE = "min_restore";
    public static final String KEY_MIN_RESTORE_BEST = "min_restore_best";

    double initialValue;
    double minProportion;

    public MinRestoreObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) throws Exception {
        super(problem, timeLimit, verbose, maximize);
        if (problem.getMinRestore() == null) {
            throw new Exception("MinRestoreObjective without minProportion is only allowed" +
                    "if a restorable constraint was posted");
        }
    }

    public MinRestoreObjective(RestoptProblem problem, double minProportion, int timeLimit, boolean verbose, boolean maximize) {
        super(problem, timeLimit, verbose, maximize);
        this.minProportion = minProportion;
    }

    @Override
    public void initObjective() {
        if (problem.getMinRestore() != null) {
            objective = problem.getMinRestore();
        } else {
            double maxRest = Arrays.stream(problem.getData().getRestorableData()).sum();
            try {
                problem.postRestorableConstraint(0, (int) maxRest, minProportion);
                objective = problem.getMinRestore();
            } catch (IOException | RestoptException e) {
                e.printStackTrace();
            }
        }
        initialValue = objective.getLB();
    }

    @Override
    public String getInitialValueMessage() {
        return "\nMin restore initial = " + initialValue + "\n";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]{KEY_MIN_RESTORE_INITIAL, KEY_MIN_RESTORE, KEY_MIN_RESTORE_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_MIN_RESTORE_INITIAL, String.valueOf(initialValue));
        charact.put(KEY_MIN_RESTORE, String.valueOf(solution.getIntVal(objective)));
        charact.put(KEY_MIN_RESTORE_BEST, String.valueOf(optimalValue));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[]{KEY_MIN_RESTORE_INITIAL, "Initial min restore value: "});
        messages.add(new String[]{KEY_MIN_RESTORE, "Min restore value: "});
        messages.add(new String[]{KEY_MIN_RESTORE_BEST, "Best known min restore value: "});
        return messages;
    }
}
