package org.restopt.constraints;

import org.restopt.BaseProblem;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Constraint over the amount of area to be restored.
 */
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
            int restorable = (int) Math.round(problem.getData().getRestorableData()[completeUngroupedIndex]);
            maxRestorableArea[cell] = restorable;
            minArea[cell] = restorable <= threshold ? 0 : restorable - threshold;
        }
        problem.minRestore = getModel().intVar(minAreaToRestore, maxAreaToRestore);
        problem.totalRestorable = getModel().intVar(0, maxAreaToRestore * maxCellArea);
        getModel().sumElements(getRestoreSetVar(), minArea, problem.minRestore).post();
        getModel().sumElements(getRestoreSetVar(), maxRestorableArea, problem.totalRestorable).post();
        int[] cardBounds = getCardinalityBounds();
        getModel().arithm(getRestoreSetVar().getCard(), ">=", cardBounds[0]).post();
        getModel().arithm(getRestoreSetVar().getCard(), "<=", cardBounds[1]).post();
    }

    public int[] getCardinalityBounds() {
        int[] pus = problem.getAvailablePlanningUnits();
        int[] minArea = new int[pus.length];
        for (int i = 0; i < problem.getAvailablePlanningUnits().length; i++) {
            int cell = pus[i];
            int completeUngroupedIndex = getGrid().getUngroupedCompleteIndex(cell);
            int cArea = cellArea[completeUngroupedIndex];
            int threshold = (int) Math.ceil(cArea * (1 - minProportion));
            int restorable = (int) Math.round(problem.getData().getRestorableData()[completeUngroupedIndex]);
            minArea[i] = restorable <= threshold ? 0 : restorable - threshold;
        }
        Arrays.sort(minArea);
        int UB = 0;
        int sum = 0;
        for (int i : minArea) {
/*            if (i == 0) {
                continue;
            }*/
            sum += i;
            if (sum > maxAreaToRestore) {
                break;
            }
            UB++;
        }
        int LB = 0;
        sum = 0;
        boolean started = false;
        for (int i = minArea.length - 1; i >= 0; i--) {
            if (!started) {
                if (minArea[i] <= maxAreaToRestore) {
                    started = true;
                    LB++;
                    sum += minArea[i];
                }
            } else  {
                sum += minArea[i];
                if (sum > maxAreaToRestore) {
                    break;
                }
                LB++;
            }
        }
        return new int[] {LB, UB};
    }
}
