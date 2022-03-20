package org.restopt.constraints;

import org.restopt.BaseProblem;

import java.io.IOException;
import java.util.stream.IntStream;

public class RestorableAreaConstraint extends AbstractRestoptConstraint {

    protected int minAreaToRestore;
    protected int maxAreaToRestore;
    protected int[] cellArea;
    protected double minProportion;

    public RestorableAreaConstraint(BaseProblem baseProblem, int minAreaToRestore, int maxAreaToRestore,
                                    int cellArea, double minProportion) throws IOException {
        this(
                baseProblem,
                minAreaToRestore,
                maxAreaToRestore,
                IntStream.generate(() -> cellArea)
                        .limit(baseProblem.getData().getRestorableData().length)
                        .toArray(),
                minProportion
        );
    }

    public RestorableAreaConstraint(BaseProblem baseProblem, int minAreaToRestore, int maxAreaToRestore,
                                    int[] cellArea, double minProportion) throws IOException {
        super(baseProblem);
        this.minAreaToRestore = minAreaToRestore;
        this.maxAreaToRestore = maxAreaToRestore;
        if (cellArea.length != problem.getData().getRestorableData().length) {
            throw new IOException("Cell area array must have the same dimensions as the landscape");
        }
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
        int maxCellArea = 0;
        for (int i = 0; i < problem.getAvailablePlanningUnits().length; i++) {
            int cell = pus[i];
            int completeUngroupedIndex = getGrid().getUngroupedCompleteIndex(cell);
            int cArea = cellArea[completeUngroupedIndex];
            maxCellArea = maxCellArea < cArea ? cArea : maxCellArea;
            int threshold = (int) Math.ceil(cArea * (1 - minProportion));
            int value = (int) Math.round(problem.getData().getRestorableData()[completeUngroupedIndex]);
            maxRestorableArea[cell] = value;
            int restorable = (int) Math.round(problem.getData().getRestorableData()[completeUngroupedIndex]);
            minArea[cell] = restorable <= threshold ? 0 : restorable - threshold;
        }
        problem.minRestore = getModel().intVar(minAreaToRestore, maxAreaToRestore);
        problem.maxRestorable = getModel().intVar(0, maxAreaToRestore * maxCellArea);
        getModel().sumElements(getRestoreSetVar(), minArea, problem.minRestore).post();
        getModel().sumElements(getRestoreSetVar(), maxRestorableArea, problem.maxRestorable).post();
    }
}
