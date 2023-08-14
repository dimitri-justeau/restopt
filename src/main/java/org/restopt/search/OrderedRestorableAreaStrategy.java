package org.restopt.search;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.subgraph.NodeSubGraphVar;
import org.restopt.RestoptProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
        BoolVar[] bools = IntStream.range(0, a.size())
                .filter(i -> problem.getRestoreGraphVar().getNodeVars()[pus[a.get(i)]] instanceof NodeSubGraphVar)
                .mapToObj(i -> problem.getRestoreGraphVar().getNodeVars()[pus[a.get(i)]])
                .toArray(BoolVar[]::new);
        if (UB) {
            problem.getModel().getSolver().setSearch(Search.inputOrderUBSearch(bools));
        } else {
            problem.getModel().getSolver().setSearch(Search.inputOrderLBSearch(bools));
        }
    }
}
