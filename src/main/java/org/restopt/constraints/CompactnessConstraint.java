package org.restopt.constraints;

import org.chocosolver.solver.constraints.Constraint;
import org.restopt.RestoptProblem;
import org.restopt.choco.PropSmallestEnclosingCircleSpatialGraph;

import java.util.Arrays;

/**
 * Constraint over the compactness of the selected restoration area. The compactness here refers to the diameter of
 * the smallest circle enclosing all planning units of the restoration area.
 */
public class CompactnessConstraint extends AbstractRestoptConstraint {

    protected double minDiameter;
    protected double maxDiameter;

    public CompactnessConstraint(RestoptProblem restoptProblem, double maxDiameter) {
        this(restoptProblem, 0, maxDiameter);
    }

    public CompactnessConstraint(RestoptProblem restoptProblem, double minDiameter, double maxDiameter) {
        super(restoptProblem);
        this.minDiameter = minDiameter;
        this.maxDiameter = maxDiameter;
    }

    @Override
    public void post() {
        double[][] coords = new double[getGrid().getNbCells()][];
        double[][] compCoords = getGrid().getCartesianCoordinates();
        int[] pus = problem.getAvailablePlanningUnits();
        for (int i = 0; i < problem.getAvailablePlanningUnits().length; i++) {
            coords[pus[i]] = compCoords[getGrid().getUngroupedPartialIndex(pus[i])];
        }
        double xMax = Arrays.stream(getGrid().getCartesianCoordinates())
                .mapToDouble(c -> c[0]).max().getAsDouble();
        double yMax = Arrays.stream(getGrid().getCartesianCoordinates())
                .mapToDouble(c -> c[1]).max().getAsDouble();
        PropSmallestEnclosingCircleSpatialGraph propCompact = new PropSmallestEnclosingCircleSpatialGraph(
                getRestoreGraphVar(),
                coords,
                getModel().realVar("radius", 0.5 * minDiameter, 0.5 * maxDiameter, 1e-5),
                getModel().realVar("centerX", -xMax, xMax, 1e-5),
                getModel().realVar("centerY", -yMax, yMax,1e-5)
        );
        Constraint cons = new Constraint("maxDiam", propCompact);
        getModel().post(cons);
    }
}
