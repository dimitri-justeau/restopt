package org.restopt.constraints;

import org.restopt.RestoptProblem;

/**
 * Constraint over the number of planning units of the selected area for restoration.
 */
public class NbPlanningUnitsConstraint extends AbstractRestoptConstraint {

    protected int minNbPUs;
    protected int maxNbPUs;

    public NbPlanningUnitsConstraint(RestoptProblem restoptProblem, int minNbPUs, int maxNbPUs) {
        super(restoptProblem);
        this.minNbPUs = minNbPUs;
        this.maxNbPUs = maxNbPUs;
    }

    @Override
    public void post() {
        if (minNbPUs == maxNbPUs) {
            getModel().sum(getRestoreGraphVar().getNodeVars(), "=", minNbPUs).post();
        } else {
            getModel().sum(getRestoreGraphVar().getNodeVars(), ">=", minNbPUs).post();
            getModel().sum(getRestoreGraphVar().getNodeVars(), "<=", maxNbPUs).post();

        }
    }
}
