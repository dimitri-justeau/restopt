package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.restopt.RestoptProblem;
import org.restopt.search.OrderedRestorableAreaStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbPlanningUnitsObjective extends AbstractRestoptObjective {

    public static final String KEY_NB_PUS_BEST = "nb_planning_units_best";

    public NbPlanningUnitsObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize, String search) {
        super(problem, timeLimit, verbose, maximize, search);
    }

    public NbPlanningUnitsObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        this(problem, timeLimit, verbose, maximize, "");
    }

    @Override
    public void initObjective() {
        objective = problem.getRestoreSetVar().getCard();
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
        return "";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]{KEY_NB_PUS_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_NB_PUS_BEST, String.valueOf(getOptimalValue()));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[]{KEY_NB_PUS_BEST, "Best known nb planning units value: "});
        return messages;
    }
}
