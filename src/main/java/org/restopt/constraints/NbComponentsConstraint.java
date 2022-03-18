package org.restopt.constraints;

import org.restopt.BaseProblem;

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
        getModel().nbConnectedComponents(getRestoreGraphVar(), getModel().intVar(minNbCC, maxNbCC)).post();
    }
}
