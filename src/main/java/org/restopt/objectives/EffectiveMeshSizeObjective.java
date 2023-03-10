package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.restopt.DataLoader;
import org.restopt.RestoptProblem;
import org.restopt.choco.LandscapeIndicesUtils;
import org.restopt.choco.PropEffectiveMeshSize;
import org.restopt.grid.regular.square.GroupedGrid;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectiveMeshSizeObjective extends AbstractRestoptObjective {

    public static final String KEY_MESH_INITIAL = "mesh_initial";
    public static final String KEY_MESH = "mesh";
    public static final String KEY_MESH_BEST = "mesh_best";

    int precision;
    double initialValue;

    public EffectiveMeshSizeObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize, int precision, String search) {
        super(problem, timeLimit, verbose, maximize, search);
        this.precision = precision;
    }

    public EffectiveMeshSizeObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize, int precision) {
        this(problem, timeLimit, verbose, maximize, precision, "");
    }

    @Override
    public void initObjective() {
        DataLoader data = problem.getData();
        GroupedGrid grid = problem.getGrid();
        if (problem.getAdditionalVariables().containsKey(KEY_MESH)) {
            objective = problem.getAdditionalVariables().get(KEY_MESH);
        } else {
            objective = problem.getModel().intVar(
                    "MESH",
                    0, (int) ((data.getHeight() * data.getWidth() - grid.getDiscardSet().size()) * Math.pow(10, precision))
            );
            int landscapeArea = problem.getLandscapeArea();
            Constraint meshCons = new Constraint(
                    "MESH_constraint",
                    new PropEffectiveMeshSize(
                            problem.getHabitatGraphVar(),
                            objective,
                            grid.getSizeCells(),
                            landscapeArea,
                            precision,
                            true
                    )
            );
            problem.getModel().post(meshCons);
            problem.getAdditionalVariables().put(KEY_MESH, objective);
        }
        initialValue = LandscapeIndicesUtils.effectiveMeshSize(
                problem.getHabitatGraph(),
                problem.getLandscapeArea()
        );
    }

    @Override
    public String getInitialValueMessage() {
        return "\nMESH initial = " + initialValue + "\n";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]{KEY_MESH_INITIAL, KEY_MESH, KEY_MESH_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_MESH_INITIAL, String.valueOf(1.0 * Math.round(initialValue * Math.pow(10, precision)) / Math.pow(10, precision)));
        charact.put(KEY_MESH, String.valueOf((1.0 * solution.getIntVal(objective)) / Math.pow(10, precision)));
        charact.put(KEY_MESH_BEST, String.valueOf((1.0 * getOptimalValue()) / Math.pow(10, precision)));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[]{KEY_MESH_INITIAL, "Initial MESH value: "});
        messages.add(new String[]{KEY_MESH, "MESH value: "});
        messages.add(new String[]{KEY_MESH_BEST, "Best known MESH value: "});
        return messages;
    }
}
