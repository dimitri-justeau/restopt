package org.restopt.constraints;

import org.restopt.RestoptProblem;
import org.restopt.exception.RestoptException;

import java.io.IOException;
import java.util.Arrays;

/**
 * Constraint over the amount of area to be restored.
 */
public class RestorableAreaConstraint extends AbstractRestoptConstraint {

    protected int minAreaToRestore;
    protected int maxAreaToRestore;
    protected double minProportion;
    protected int[] minArea;

    public RestorableAreaConstraint(RestoptProblem restoptProblem, int minAreaToRestore, int maxAreaToRestore,
                                    double minProportion) throws RestoptException {
        super(restoptProblem);
        this.minAreaToRestore = minAreaToRestore;
        this.maxAreaToRestore = maxAreaToRestore;
        this.minProportion = minProportion;
        this.problem.setRestorableAreaConstraint(this);
    }

    @Override
    public void post() {
        // Minimum area to ensure every site to >= proportion
        assert minProportion >= 0 && minProportion <= 1;
        int[] pus = problem.getAvailablePlanningUnits();
        this.minArea = new int[pus.length];
        int[] maxRestorableArea = new int[pus.length];
        int maxCellArea = 0;
        int offset = problem.getGrid().getNbGroups();
        for (int i = 0; i < problem.getAvailablePlanningUnits().length; i++) {
            int cell = pus[i];
            int cArea = problem.getCellArea(cell);
            maxCellArea = maxCellArea < cArea ? cArea : maxCellArea;
            int threshold = (int) Math.ceil(cArea * (1 - minProportion));
            int restorable = problem.getRestorableArea(cell);
            maxRestorableArea[cell - offset] = restorable;
            minArea[cell - offset] = restorable <= threshold ? 0 : restorable - threshold;
        }
        problem.minRestore = getModel().intVar(minAreaToRestore, maxAreaToRestore);
        problem.totalRestorable = getModel().intVar(0, pus.length * maxCellArea);
        getModel().sumElements(getRestoreSetVar(), minArea, offset, problem.minRestore).post();
        getModel().sumElements(getRestoreSetVar(), maxRestorableArea, offset, problem.totalRestorable).post();
    }

    public int[] getCardinalityBounds() {
        int[] areas = Arrays.copyOf(minArea, minArea.length);
        Arrays.sort(areas);
        int UB = 0;
        int sum = 0;
        for (int i : areas) {
            sum += i;
            if (sum > maxAreaToRestore) {
                break;
            }
            UB++;
        }
        int LB = 0;
        sum = 0;
        boolean started = false;
        for (int i = areas.length - 1; i >= 0; i--) {
            if (!started) {
                if (areas[i] <= maxAreaToRestore) {
                    started = true;
                    LB++;
                    sum += areas[i];
                }
            } else {
                sum += areas[i];
                if (sum > maxAreaToRestore) {
                    break;
                }
                LB++;
            }
        }
        return new int[]{LB, UB};
    }

    public int getMinArea(int pu) {
        return minArea[pu - problem.getGrid().getNbGroups()];
    }
}
