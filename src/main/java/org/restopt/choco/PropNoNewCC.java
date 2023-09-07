package org.restopt.choco;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.subgraph.SubGraphConnectedComponents;
import org.chocosolver.solver.variables.subgraph.SubGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

public class PropNoNewCC extends Propagator<IntVar> {

    private final SubGraphVar g;
    private final ISet LBNodes;

    public PropNoNewCC(SubGraphVar g) {
        super(g.getNodeVars(), PropagatorPriority.QUADRATIC, false);
        this.g = g;
        this.LBNodes = g.getNodesLB();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        g.updateConnectivity();
        SubGraphConnectedComponents ccUB = g.getConnectedComponentsUB();
        ISet ccOfOrignalLB = SetFactory.makeRangeSet();
        for (int i : LBNodes) {
            ccOfOrignalLB.add(ccUB.getNodeCC(i));
        }
        // 1 - Remove all unreachable nodes.
        for (int i = 0; i < g.getNbPotentialNodes(); i++) {
            int node = g.getPotentialNode(i);
            if (!ccOfOrignalLB.contains(ccUB.getNodeCC(node))) {
                g.removeNode(node, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        g.updateConnectivity();
        SubGraphConnectedComponents ccUB = g.getConnectedComponentsUB();
        ISet ccOfOrignalLB = SetFactory.makeRangeSet();
        for (int i : LBNodes) {
            ccOfOrignalLB.add(ccUB.getNodeCC(i));
        }
        for (int i = 0; i < g.getNbPotentialNodes(); i++) {
            int node = g.getPotentialNode(i);
            if (ccUB.isAlsoInLB(node) && !ccOfOrignalLB.contains(ccUB.getNodeCC(node))) {
                return ESat.FALSE;
            }
        }
        if (!g.isInstantiated()) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }
}
