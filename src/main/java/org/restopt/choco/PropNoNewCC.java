package org.restopt.choco;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

public class PropNoNewCC extends Propagator<UndirectedGraphVar> {

    private final UndirectedGraphVar g;
    private final ISet LBNodes;
    private ConnectivityFinderSpatialGraph ccUB;

    public PropNoNewCC(UndirectedGraphVar g) {
        super(new UndirectedGraphVar[] {g}, PropagatorPriority.QUADRATIC, false);
        this.g = g;
        this.ccUB = new ConnectivityFinderSpatialGraph(g.getUB());
        this.LBNodes = SetFactory.makeConstantSet(g.getMandatoryNodes().toArray());
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ccUB.findAllCC();
        ISet ccOfOrignalLB = SetFactory.makeRangeSet();
        for (int i : LBNodes) {
            ccOfOrignalLB.add(ccUB.getNodeCC()[i]);
        }
        // 1 - Remove all unreachable nodes.
        for (int node : g.getPotentialNodes()) {
            if (!ccOfOrignalLB.contains(ccUB.getNodeCC()[node])) {
                g.removeNode(node, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        ccUB.findAllCC();
        ISet ccOfOrignalLB = SetFactory.makeRangeSet();
        for (int i : LBNodes) {
            ccOfOrignalLB.add(ccUB.getNodeCC()[i]);
        }
        for (int node : g.getPotentialNodes()) {
            if (g.getMandatoryNodes().contains(node) && !ccOfOrignalLB.contains(ccUB.getNodeCC()[node])) {
                return ESat.FALSE;
            }
        }
        if (!g.isInstantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }
}
