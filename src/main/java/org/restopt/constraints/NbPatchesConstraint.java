package org.restopt.constraints;

import org.restopt.RestoptProblem;

/**
 * Constraint over the number of patches.
 */
public class NbPatchesConstraint extends AbstractRestoptConstraint {

    protected int minNP;
    protected int maxNP;

    public NbPatchesConstraint(RestoptProblem restoptProblem, int minNP, int maxNP) {
        super(restoptProblem);
        this.minNP = minNP;
        this.maxNP = maxNP;
    }

    @Override
    public void post() {
        if (minNP == maxNP && maxNP == 1) {
            getModel().connected(problem.getHabitatGraphVar()).post();
        } else {
            getModel().nbConnectedComponents(problem.getHabitatGraphVar(), getModel().intVar(minNP, maxNP)).post();
        }
    }
}
