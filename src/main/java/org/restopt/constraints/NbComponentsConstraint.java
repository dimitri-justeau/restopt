package org.restopt.constraints;

import org.restopt.RestoptProblem;
import org.restopt.choco.ConnectivityFinderSpatialGraph;

/**
 * Constraint over the number of connected components of the selected area for restoration.
 */
public class NbComponentsConstraint extends AbstractRestoptConstraint {

    protected int minNbCC;
    protected int maxNbCC;

    public NbComponentsConstraint(RestoptProblem restoptProblem, int minNbCC, int maxNbCC) {
        super(restoptProblem);
        this.minNbCC = minNbCC;
        this.maxNbCC = maxNbCC;
    }

    @Override
    public void post() {
        if (minNbCC == maxNbCC && maxNbCC == 1) {
            getModel().connected(getRestoreGraphVar()).post();
        } else {
            getModel().nbConnectedComponents(getRestoreGraphVar(), getModel().intVar(minNbCC, maxNbCC)).post();
        }
    }
}
