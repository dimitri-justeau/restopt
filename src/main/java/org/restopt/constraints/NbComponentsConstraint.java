package org.restopt.constraints;

import org.restopt.BaseProblem;
import org.restopt.choco.ConnectivityFinderSpatialGraph;

/**
 * Constraint over the number of connected components of the selected area for restoration.
 */
public class NbComponentsConstraint extends AbstractRestoptConstraint {

    protected int minNbCC;
    protected int maxNbCC;

    public NbComponentsConstraint(BaseProblem baseProblem, int minNbCC, int maxNbCC) {
        super(baseProblem);
        this.minNbCC = minNbCC;
        this.maxNbCC = maxNbCC;
    }

    @Override
    public void post() {
        if (minNbCC == maxNbCC && maxNbCC == 1) {
            getModel().connected(getRestoreGraphVar()).post();
            ConnectivityFinderSpatialGraph cf = new ConnectivityFinderSpatialGraph(getRestoreGraphVar().getUB());
            cf.findAllCC();
            getModel().arithm(getRestoreSetVar().getCard(), "<=", cf.getSizeMaxCC()).post();
        } else {
            getModel().nbConnectedComponents(getRestoreGraphVar(), getModel().intVar(minNbCC, maxNbCC)).post();
        }
    }
}
