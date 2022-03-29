package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.restopt.RestoptProblem;
import org.restopt.search.OrderedRestorableAreaStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbPlanningUnitsObjective extends AbstractRestoptObjective {

    public static final String KEY_NB_PUS_INITIAL = "nb_pus_initial";
    public static final String KEY_NB_PUS_BEST = "nb_pus_best";

    double initialValue;

    public NbPlanningUnitsObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        super(problem, timeLimit, verbose, maximize);
    }

    @Override
    public void initObjective() {
        objective = problem.getRestoreSetVar().getCard();
        initialValue = objective.getLB();
        if (this.problem.hasRestorableAreaConstraint()) {
            int[] cardBounds = problem.getRestorableAreaConstraint().getCardinalityBounds();
            problem.getModel().arithm(problem.getRestoreSetVar().getCard(), ">=", cardBounds[0]).post();
            problem.getModel().arithm(problem.getRestoreSetVar().getCard(), "<=", cardBounds[1]).post();
        }
    }

    @Override
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
    }

    @Override
    public String getInitialValueMessage() {
        return "\nNb PUS initial = " + initialValue + "\n";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]{KEY_NB_PUS_INITIAL, KEY_NB_PUS_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_NB_PUS_INITIAL, String.valueOf(initialValue));
        charact.put(KEY_NB_PUS_BEST, String.valueOf(solution.getIntVal(objective)));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[]{KEY_NB_PUS_INITIAL, "Initial nb pus value: "});
        messages.add(new String[]{KEY_NB_PUS_BEST, "Best nb pus value: "});
        return messages;
    }
}
