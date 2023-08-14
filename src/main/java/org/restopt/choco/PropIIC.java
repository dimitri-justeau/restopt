/*
 * Copyright (c) 2020, Dimitri Justeau-Allaire
 *
 * CIRAD, UMR AMAP, F-34398 Montpellier, France
 * Institut Agronomique neo-Caledonien (IAC), 98800 Noumea, New Caledonia
 * AMAP, Univ Montpellier, CIRAD, CNRS, INRA, IRD, Montpellier, France
 *
 * This file is part of Choco-reserve.
 *
 * Choco-reserve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Choco-reserve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Choco-reserve.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.restopt.choco;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.subgraph.SubGraphConnectedComponents;
import org.chocosolver.solver.variables.subgraph.SubGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.GroupedGrid;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Propagator maintaining a variable equals to the Integral Index of Connectivity (IIC).
 * <p>
 * Ref: https://link.springer.com/article/10.1007/s10980-006-0013-z
 *
 * @author Dimitri Justeau-Allaire
 */
public class PropIIC extends Propagator<IntVar> {

    protected SubGraphVar g;
    protected IntVar iic;
    protected int landscapeArea;
    protected int precision;
    protected GroupedGrid grid;
    protected int distanceThreshold;
    protected INeighborhood threshold;
    public int[][] threshNeigh;
    public int[][] thresh;
    private final boolean maximize;
    //private final ConnectivityFinderSpatialGraph ccLB;
    //private final ConnectivityFinderSpatialGraph ccUB;
    private IStateInt iicUB;

    /**
     * @param g             The graph variable associated to the region for which the propagator will maintain IIC.
     * @param iic           The integer variable equals to IIC, maintained by this propagator.
     * @param landscapeArea The total landscape area.
     */
    public PropIIC(SubGraphVar g, IntVar iic, GroupedGrid grid, int landscapeArea, int distanceThreshold, int precison, boolean maximize) {
        super(ArrayUtils.concat(g.getNodeVars(), iic), PropagatorPriority.QUADRATIC, false);
        this.g = g;
        this.grid = grid;
        this.iic = iic;
        this.landscapeArea = landscapeArea;
        this.precision = precison;
        this.thresh = new int[grid.getNbCells()][grid.getNbCells()];
        this.threshNeigh = new int[grid.getNbCells()][];
        this.threshold = Neighborhoods.PARTIAL_GROUPED_K_WIDE_FOUR_CONNECTED(distanceThreshold);
        this.distanceThreshold = distanceThreshold;
        this.maximize = maximize;
        //this.ccLB = new ConnectivityFinderSpatialGraph(g.getLB(), g.getUB(), grid.getSizeCells());
        //this.ccUB = new ConnectivityFinderSpatialGraph(g.getUB(), grid.getSizeCells());
        this.iicUB = getModel().getEnvironment().makeInt(landscapeArea);
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < getNbVars() - 1 && maximize && !g.isInstantiated() && !g.getNodeVars()[idxVarInProp].isInstantiatedTo(0)) {
            return;
        } else {
            if (idxVarInProp < getNbVars() - 1) {
                g.updateConnectivity();
                // LB
                if (!maximize || g.isInstantiated()) {
                    int iic_LB = (int) Math.round(getIICLB() * Math.pow(10, precision));
                    iic.updateLowerBound(iic_LB, this);
                }

                // UB
                int iic_UB = (int) Math.round(getIICUB() * Math.pow(10, precision));
                iic.updateUpperBound(iic_UB, this);
                iicUB.set(iic_UB);
                if (iic.getLB() == iicUB.get()) {
                    for (int i = 0; i < g.getNbPotentialNodes(); i++) {
                        int node = g.getPotentialNode(i);
                        g.enforceNode(node, this);
                    }
                }
            } else {
                if (iic.getLB() == iicUB.get()) {
                    for (int i = 0; i < g.getNbPotentialNodes(); i++) {
                        int node = g.getPotentialNode(i);
                        g.enforceNode(node, this);
                    }
                }
            }
        }
    }
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        g.updateConnectivity();
        // LB
        if (!maximize || g.isInstantiated()) {
            int iic_LB = (int) Math.round(getIICLB() * Math.pow(10, precision));
            iic.updateLowerBound(iic_LB, this);
        }
        // UB
        int iic_UB = (int) Math.round(getIICUB() * Math.pow(10, precision));
        iic.updateUpperBound(iic_UB, this);
        iicUB.set(iic_UB);

        if (iic.getLB() == iic_UB) {
            for (int i = 0; i < g.getNbPotentialNodes(); i++) {
                int node = g.getPotentialNode(i);
                g.enforceNode(node, this);
            }
        }
    }

    public float getIICLB() {
        return getIIC(true);
    }

    public float getIICUB() {
        return getIIC(false);
    }

    public float getIIC(boolean lb) {
        // GET CCs
        SubGraphConnectedComponents ccf = lb ? g.getConnectedComponentsLB() : g.getConnectedComponentsUB();
        int nbCC = ccf.getNbCC();
        int[][] ccs = new int[nbCC][];
        for (int i = 0; i < nbCC; i++) {
            ccs[i] = new int[ccf.getSizeCC(i)];
            int k = 0;
            for (int j = ccf.getCCFirstNode(i); j >= 0; j = ccf.getCCNextNode(j)) {
                ccs[i][k++] = j;
            }
        }
        int[][] adj = getLandscapeGraph(nbCC, ccs, lb);
        float iic = 0;
        for (int i = 0; i < adj.length; i++) {
            int[] dists = bfs(i, adj);
            for (int j = 0; j < adj.length; j++) {
                if (dists[j] >= 0) {
                    iic += (ccf.getAttributeCC(i) * ccf.getAttributeCC(j)) / (1 + dists[j]);
                }
            }
        }
        return iic / (landscapeArea * landscapeArea);
    }

    public int[][] getLandscapeGraph(int nbCC, int[][] ccs, boolean lb) {
        SubGraphConnectedComponents ccf = lb ? g.getConnectedComponentsLB() : g.getConnectedComponentsUB();
        int[][] neigh = new int[nbCC][];
        for (int i = 0; i < nbCC; i++) {
            boolean[] conn = new boolean[nbCC];
            int nAdj = 0;
            int[] cc = ccs[i];
            for (int node : cc) {
                if (threshNeigh[node] == null) {
                    threshNeigh[node] = threshold.getNeighbors(grid, node);
                }
                for (int j : threshNeigh[node]) {
                    if (ccf.getNodeCC(j) != i && ccf.containsNode(j) && !conn[ccf.getNodeCC(j)]) {
                        conn[ccf.getNodeCC(j)] = true;
                        nAdj += 1;
                    }
                }
            }
            int[] adj = new int[nAdj];
            int k = 0;
            for (int j = 0; j < nbCC; j++) {
                if (conn[j]) {
                    adj[k++] = j;
                }
            }
            neigh[i] = adj;
        }
        return neigh;
    }

    public int[] bfs(int source, int[][] adj) {
        int n = adj.length;
        boolean[] visited = new boolean[n];
        int[] queue = new int[n];
        int front = 0;
        int rear = 0;
        int[] dist = new int[n];
        for (int i = 0; i < n; i++) {
            dist[i] = -1;
        }
        int current;
        visited[source] = true;
        queue[front] = source;
        rear++;
        dist[source] = 0;
        while (front != rear) {
            current = queue[front++];
            for (int i : adj[current]) {
                if (!visited[i]) {
                    dist[i] = dist[current] + 1;
                    queue[rear++] = i;
                    visited[i] = true;
                }
            }
        }
        return dist;
    }


    @Override
    public ESat isEntailed() {
        g.updateConnectivity();
        int iic_LB = (int) Math.round(getIICLB() * Math.pow(10, precision));
        int iic_UB = (int) Math.round(getIICUB() * Math.pow(10, precision));
        if (iic_LB > iic.getUB() || iic_UB < iic.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
