package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.restopt.BaseProblem;
import org.restopt.DataLoader;
import org.restopt.choco.LandscapeIndicesUtils;
import org.restopt.choco.PropEffectiveMeshSize;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class MinRestoreObjective extends AbstractRestoptObjective {

    public static final String KEY_MIN_RESTORE_INITIAL = "min_restore_initial";
    public static final String KEY_MIN_RESTORE_BEST = "min_restore_best";

    double initialValue;
    int[] cellArea;
    double minProportion;

    public MinRestoreObjective(BaseProblem problem, int timeLimit, boolean verbose, boolean maximize) throws Exception {
        super(problem, timeLimit, verbose, maximize);
        if (problem.getMinRestore() == null) {
            throw new Exception("MinRestoreObjective without cellArea and minProportion is only allowed" +
                    "if a restorable constraint was posted");
        }
    }

    public MinRestoreObjective(BaseProblem problem, int cellArea, double minProportion, int timeLimit, boolean verbose, boolean maximize) {
        this(
                problem,
                IntStream.generate(() -> cellArea)
                    .limit(problem.getData().getRestorableData().length)
                    .toArray(),
                minProportion,
                timeLimit,
                verbose,
                maximize
        );
    }

    public MinRestoreObjective(BaseProblem problem, int[] cellArea, double minProportion, int timeLimit, boolean verbose, boolean maximize) {
        super(problem, timeLimit, verbose, maximize);
        this.cellArea = cellArea;
        this.minProportion = minProportion;
    }

    @Override
    public void initObjective() {
        if (problem.getMinRestore() != null) {
            objective = problem.getMinRestore();
        } else {
            double maxRest = Arrays.stream(problem.getData().getRestorableData()).sum();
            try {
                problem.postRestorableConstraint(0, (int) maxRest, cellArea, minProportion);
                objective = problem.getMinRestore();
            } catch (IOException e) {
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
        return new String[]  {KEY_MIN_RESTORE_INITIAL, KEY_MIN_RESTORE_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_MIN_RESTORE_INITIAL, String.valueOf(initialValue));
        charact.put(KEY_MIN_RESTORE_BEST, String.valueOf(solution.getIntVal(objective)));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[] {KEY_MIN_RESTORE_INITIAL, "Initial min restore Value: "});
        messages.add(new String[] {KEY_MIN_RESTORE_BEST, "Best min restore Value: "});
        return messages;
    }
}
