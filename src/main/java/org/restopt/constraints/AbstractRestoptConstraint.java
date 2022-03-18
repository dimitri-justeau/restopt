package org.restopt.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.restopt.BaseProblem;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;

public abstract class AbstractRestoptConstraint {

    protected BaseProblem problem;

    public AbstractRestoptConstraint(BaseProblem baseProblem) {
        this.problem = baseProblem;
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

    protected PartialRegularGroupedGrid getGrid() {
        return problem.getGrid();
    }
}
