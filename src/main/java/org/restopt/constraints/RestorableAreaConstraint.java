package org.restopt.constraints;

import org.chocosolver.solver.variables.IntVar;
import org.restopt.BaseProblem;

public class RestorableAreaConstraint extends AbstractRestoptConstraint {

    protected int minAreaToRestore;
    protected int maxAreaToRestore;
    protected int cellArea;
    protected double minProportion;

    public RestorableAreaConstraint(BaseProblem baseProblem, int minAreaToRestore, int maxAreaToRestore,
                                    int cellArea, double minProportion) {
        super(baseProblem);
        this.minAreaToRestore = minAreaToRestore;
        this.maxAreaToRestore = maxAreaToRestore;
        this.cellArea = cellArea;
        this.minProportion = minProportion;
    }

    @Override
    public void post() {
        // Minimum area to ensure every site to >= proportion
        assert minProportion >= 0 && minProportion <= 1;
        int[] minArea = new int[getGrid().getNbCells()];
        int[] maxRestorableArea = new int[getGrid().getNbCells()];
        int[] pus = problem.getAvailablePlanningUnits();
        int threshold = (int) Math.ceil(cellArea - cellArea * minProportion);
        for (int i = 0; i < problem.getAvailablePlanningUnits().length; i++) {
            int cell = pus[i];
            int value = (int) Math.round(problem.getData().getRestorableData()[getGrid().getUngroupedCompleteIndex(cell)]);
            maxRestorableArea[cell] = value;
            int restorable = (int) Math.round(problem.getData().getRestorableData()[getGrid().getUngroupedCompleteIndex(cell)]);
            minArea[cell] = restorable <= threshold ? 0 : restorable - threshold;
        }
        problem.minRestore = getModel().intVar(minAreaToRestore, maxAreaToRestore);
        problem.maxRestorable = getModel().intVar(0, maxAreaToRestore * cellArea);
        getModel().sumElements(getRestoreSetVar(), minArea, problem.minRestore).post();
        getModel().sumElements(getRestoreSetVar(), maxRestorableArea, problem.maxRestorable).post();
    }
}
