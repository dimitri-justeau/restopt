package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.restopt.BaseProblem;

import java.util.*;

public class NbPlanningUnitsObjective extends AbstractRestoptObjective {

    public static final String KEY_NB_PUS_INITIAL = "nb_pus_initial";
    public static final String KEY_NB_PUS_BEST = "nb_pus_best";

    double initialValue;

    public NbPlanningUnitsObjective(BaseProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        super(problem, timeLimit, verbose, maximize);
    }

    @Override
    public void initObjective() {
        objective = problem.getRestoreSetVar().getCard();
        initialValue = objective.getLB();
    }

    @Override
    public String getInitialValueMessage() {
        return "\nNb PUS initial = " + initialValue + "\n";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]  {KEY_NB_PUS_INITIAL, KEY_NB_PUS_BEST};
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
        messages.add(new String[] {KEY_NB_PUS_INITIAL, "Initial nb pus value: "});
        messages.add(new String[] {KEY_NB_PUS_BEST, "Best nb pus value: "});
        return messages;
    }
}
