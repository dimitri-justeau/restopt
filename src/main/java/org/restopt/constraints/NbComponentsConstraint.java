package org.restopt.constraints;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.graph.subgraph.PropSubGraphConnected;
import org.chocosolver.solver.constraints.graph.subgraph.PropSubGraphNbCC;
import org.restopt.RestoptProblem;

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
            Constraint c = new Constraint("connected", new PropSubGraphConnected(problem.getRestoreGraphVar()));
            getModel().post(c);
        } else {
            Constraint c = new Constraint("nbCC", new PropSubGraphNbCC(problem.getRestoreGraphVar(), getModel().intVar(minNbCC, maxNbCC)));
            getModel().post(c);
        }
    }
}
