package org.restopt.constraints;

import org.chocosolver.solver.constraints.Constraint;
import org.restopt.RestoptProblem;
import org.restopt.choco.PropNoNewCC;

/**
 * Constraint ensuring that no new patch will be created.
 */
public class NoNewPatchConstraint extends AbstractRestoptConstraint {

    public NoNewPatchConstraint(RestoptProblem restoptProblem) {
        super(restoptProblem);
    }

    @Override
    public void post() {
        getModel().post(new Constraint("NoNewPatch", new PropNoNewCC(problem.getHabitatGraphVar())));
    }
}
