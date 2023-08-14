package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.graph.subgraph.PropSubGraphNbCC;
import org.restopt.RestoptProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbPatchesObjective extends AbstractRestoptObjective {

    public static final String KEY_NB_PATCHES_INITIAL = "nb_patches_initial";
    public static final String KEY_NB_PATCHES = "nb_patches";
    public static final String KEY_NB_PATCHES_BEST = "nb_patches_best";

    int initialValue;

    public NbPatchesObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize, String search) {
        super(problem, timeLimit, verbose, maximize, search);
    }

    public NbPatchesObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize) {
        this(problem, timeLimit, verbose, maximize, "");
    }

    @Override
    public void initObjective() {
        objective = problem.getModel().intVar(1, problem.getLandscapeArea());
        Constraint c = new Constraint("nbCC", new PropSubGraphNbCC(problem.getHabitatGraphVar(), objective, true));
        problem.getModel().post(c);
        initialValue = problem.getHabitatGraph().getNBCC();
    }

    @Override
    public String getInitialValueMessage() {
        return "NB_PATCHES initial = " + initialValue + "\n";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[] { KEY_NB_PATCHES_INITIAL, KEY_NB_PATCHES, KEY_NB_PATCHES_BEST };
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_NB_PATCHES_INITIAL, String.valueOf(initialValue));
        charact.put(KEY_NB_PATCHES, String.valueOf(solution.getIntVal(objective)));
        charact.put(KEY_NB_PATCHES_BEST, String.valueOf(getOptimalValue()));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[] {KEY_NB_PATCHES_INITIAL, "Initial number of habitat patches: "});
        messages.add(new String[] {KEY_NB_PATCHES, "Number of habitat patches after restoration: "});
        messages.add(new String[] {KEY_NB_PATCHES_BEST, "Best known value for the number of habitat patches: "});
        return messages;
    }
}
