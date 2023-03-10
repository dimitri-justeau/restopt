package org.restopt.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.restopt.RestoptProblem;
import org.restopt.grid.regular.square.GroupedGrid;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

/**
 * Abstract base class for constraints over a restopt problem. The only method that needs to be implemented by
 * inheriting constraints is the post method.
 */
public abstract class AbstractRestoptConstraint {

    protected RestoptProblem problem;

    public AbstractRestoptConstraint(RestoptProblem restoptProblem) {
        this.problem = restoptProblem;
    }

    public abstract void post();

    protected Model getModel() {
        return problem.getModel();
    }

    protected SetVar getRestoreSetVar() {
        return problem.getRestoreSetVar();
    }

    protected UndirectedGraphVar getRestoreGraphVar() {
        return problem.getRestoreGraphVar();
    }

    protected GroupedGrid getGrid() {
        return problem.getGrid();
    }
}
