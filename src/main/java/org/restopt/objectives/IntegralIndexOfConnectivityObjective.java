package org.restopt.objectives;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.restopt.RestoptProblem;
import org.restopt.choco.PropIIC;
import org.restopt.exception.RestoptException;
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegralIndexOfConnectivityObjective extends AbstractRestoptObjective {

    public static final String KEY_IIC_INITIAL = "iic_initial";
    public static final String KEY_IIC = "iic";
    public static final String KEY_IIC_BEST = "iic_best";

    int precision;
    double initialValue;
    int distanceThreshold;

    public IntegralIndexOfConnectivityObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize,
                                                int precision) throws RestoptException {
        this(problem, timeLimit, verbose, maximize, precision, 1);
    }

    public IntegralIndexOfConnectivityObjective(RestoptProblem problem, int timeLimit, boolean verbose, boolean maximize,
                                                int precision, int distanceThreshold) throws RestoptException {
        super(problem, timeLimit, verbose, maximize);
        this.precision = precision;
        if (distanceThreshold < 1) {
            throw new RestoptException("The distance threshold for the integral index of connectivity must be at least" +
                    "equal to 1");
        }
        this.distanceThreshold = distanceThreshold;
    }

    private INeighborhood getNeighborhood() {
        if (distanceThreshold == 1) {
            return Neighborhoods.PARTIAL_GROUPED_TWO_WIDE_FOUR_CONNECTED;
        } else {
            return Neighborhoods.PARTIAL_GROUPED_K_WIDE_FOUR_CONNECTED(distanceThreshold + 1);
        }
    }

    @Override
    public void initObjective() {
        PartialRegularGroupedGrid grid = problem.getGrid();
        int landscapeArea = problem.getLandscapeArea();
        objective = problem.getModel().intVar(
                "IIC",
                0, (int) (Math.pow(10, precision))
        );
        Constraint consIIC = new Constraint(
                "IIC_constraint",
                new PropIIC(
                        problem.getHabitatGraphVar(),
                        objective,
                        grid,
                        landscapeArea,
                        distanceThreshold + 1,
                        precision,
                        true
                )
        );
        problem.getModel().post(consIIC);
        initialValue = ((PropIIC) consIIC.getPropagator(0)).getIICLB();
    }

    @Override
    public String getInitialValueMessage() {
        return "\nIIC initial = " + initialValue + "\n";
    }

    @Override
    public String[] getAdditionalKeys() {
        return new String[]{KEY_IIC_INITIAL, KEY_IIC, KEY_IIC_BEST};
    }

    @Override
    public Map<String, String> appendCharacteristics(Solution solution) {
        Map<String, String> charact = new HashMap<>();
        charact.put(KEY_IIC_INITIAL, String.valueOf(1.0 * Math.round(initialValue * Math.pow(10, precision)) / Math.pow(10, precision)));
        charact.put(KEY_IIC, String.valueOf((1.0 * solution.getIntVal(objective)) / Math.pow(10, precision)));
        charact.put(KEY_IIC_BEST, String.valueOf((1.0 * getOptimalValue()) / Math.pow(10, precision)));
        return charact;
    }

    @Override
    public List<String[]> appendMessages() {
        List<String[]> messages = new ArrayList<>();
        messages.add(new String[]{KEY_IIC_INITIAL, "Initial IIC value: "});
        messages.add(new String[]{KEY_IIC, "IIC value: "});
        messages.add(new String[]{KEY_IIC_BEST, "Best known IIC value: "});
        return messages;
    }
}
