package org.restopt.search;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.restopt.RestoptProblem;

import java.util.ArrayList;
import java.util.List;

/**
 * Search strategy which create boolean views over the planning units of the restore set variable, that are ordered
 * according to the amount of restorable area. An input order LB / UB search is used on these boolean views.
 */
public class OrderedRestorableAreaStrategy extends AbstractRestoptSearchStrategy {

    boolean increasingOrder;
    boolean UB;

    public OrderedRestorableAreaStrategy(RestoptProblem problem, boolean increasingOrder, boolean UB) {
        super(problem);
        this.increasingOrder = increasingOrder;
        this.UB = UB;
    }

    public void setSearch() {
        int[] pus = problem.getAvailablePlanningUnits();
        List<Integer> a = new ArrayList<>();
        for (int i = 0; i < pus.length; i++) {
            a.add(i);
        }
        a.sort((i, j) -> {
            int ir;
            int jr;
            if (problem.hasRestorableAreaConstraint()) {
                ir = problem.getRestorableAreaConstraint().getMinArea(pus[i]);
                jr = problem.getRestorableAreaConstraint().getMinArea(pus[j]);
            } else {
                ir = problem.getRestorableArea(pus[i]);
                jr = problem.getRestorableArea(pus[j]);
            }
            if (increasingOrder) {
                return (ir - jr);
            } else {
                return (jr - ir);
            }
        });
        BoolVar[] boolVars = new BoolVar[pus.length];
        for (int i = 0; i < a.size(); i++) {
            boolVars[i] = problem.getModel().setBoolView(problem.getRestoreSetVar(), pus[a.get(i)]);
        }
        if (UB) {
            problem.getModel().getSolver().setSearch(Search.inputOrderUBSearch(boolVars));
        } else {
            problem.getModel().getSolver().setSearch(Search.inputOrderLBSearch(boolVars));
        }
    }
}
