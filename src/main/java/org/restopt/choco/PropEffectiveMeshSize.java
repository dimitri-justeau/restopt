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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.util.stream.IntStream;

/**
 * Propagator maintaining a variable equals to the Effective Mesh Size (MESH), using the classical CUT procedure.
 * <p>
 * Ref: https://link.springer.com/article/10.1023/A:1008129329289
 *
 * @author Dimitri Justeau-Allaire
 */
public class PropEffectiveMeshSize extends Propagator<Variable> {

    protected UndirectedGraphVar g;
    protected IntVar mesh;
    protected int landscapeArea;
    protected int precision;
    public ConnectivityFinderSpatialGraph connectivityFinderGUB;
    public ConnectivityFinderSpatialGraph connectivityFinderGLB;
    private final boolean maximize;

    private int multiplier;

    /**
     * @param g             The graph variable associated to the region for which the propagator will maintain MESH.
     * @param mesh          The integer variable equals to MESH, maintained by this propagator.
     * @param landscapeArea The total landscape area.
     */
    public PropEffectiveMeshSize(UndirectedGraphVar g, IntVar mesh, int landscapeArea, int precison, boolean maximize) {
        this(g, mesh, IntStream.range(0, g.getNbMaxNodes()).map(i -> 1).toArray(), landscapeArea, precison, maximize);
    }

    public PropEffectiveMeshSize(UndirectedGraphVar g, IntVar mesh, int[] cellsArea, int landscapeArea, int precison, boolean maximize) {
        super(new Variable[]{g, mesh}, PropagatorPriority.VERY_SLOW, false);
        this.g = g;
        this.mesh = mesh;
        this.landscapeArea = landscapeArea;
        this.precision = precison;
        this.multiplier = (int) Math.pow(10, precision);
        this.connectivityFinderGUB = new ConnectivityFinderSpatialGraph(g.getUB(), cellsArea);
        this.connectivityFinderGLB = new ConnectivityFinderSpatialGraph(g.getLB(), g.getUB(), cellsArea);
        this.maximize = maximize;
    }

    public PropEffectiveMeshSize(UndirectedGraphVar g, IntVar mesh, int landscapeArea, int precison) {
        this(g, mesh, landscapeArea, precison, false);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // LB
        if (!maximize || (g.getMandatoryNodes().size() == g.getPotentialNodes().size())) {
            int mesh_LB_round = getLB();
            mesh.updateLowerBound(mesh_LB_round, this);
        }

        // UB
        int mesh_UB_round = getUB();
        mesh.updateUpperBound(mesh_UB_round, this);
        if (mesh.getLB() == mesh_UB_round) {
            for (int i : g.getPotentialNodes()) {
                if (!g.getMandatoryNodes().contains(i)) {
                    g.enforceNode(i, this);
                }
            }
            mesh.instantiateTo(mesh_UB_round, this);
        } else {
            boolean filtered = false;
            if (!(g.getMandatoryNodes().size() == g.getPotentialNodes().size())) {
                for (int i = 0; i < connectivityFinderGUB.getNBCC(); i++) {
                    int s = connectivityFinderGUB.getAttributeCC()[i];
                    double d = (1.0 / landscapeArea) * ((s - 1) * (s - 1) - (s * s));
                    int delta = (int) Math.round(d * multiplier);
                    if (mesh_UB_round + delta < mesh.getLB()) {
                        filtered = true;
                        for (int j : connectivityFinderGUB.getCC(i)) {
                            if (!g.getMandatoryNodes().contains(j)) {
                                g.enforceNode(j, this);
                            }
                        }
                    }
                }
                if (filtered && (!maximize || (g.getMandatoryNodes().size() == g.getPotentialNodes().size()))) {
                    int mesh_LB_round = getLB();
                    mesh.updateLowerBound(mesh_LB_round, this);
                }
            }
        }
    }

    private int getLB() {
        double mesh_LB = 0;
        connectivityFinderGLB.findAllCC();
        for (int i = 0; i < connectivityFinderGLB.getNBCC(); i++) {
            int s = connectivityFinderGLB.getAttributeCC()[i];
            mesh_LB += 1.0 * s * s;
        }
        mesh_LB /= 1.0 * landscapeArea;
        int mesh_LB_round = (int) Math.round(mesh_LB * multiplier);
        return mesh_LB_round;
    }

    private int getUB() {
        double mesh_UB = 0;
        connectivityFinderGUB.findAllCC();
        for (int i = 0; i < connectivityFinderGUB.getNBCC(); i++) {
            int s = connectivityFinderGUB.getAttributeCC()[i];
            mesh_UB += 1.0 * s * s;
        }
        mesh_UB /= 1.0 * landscapeArea;
        int mesh_UB_round = (int) Math.round(mesh_UB * multiplier);
        return mesh_UB_round;
    }

    @Override
    public ESat isEntailed() {
        int mesh_LB_round = getLB();
        int mesh_UB_round = getUB();
        if (mesh_LB_round > mesh.getUB() || mesh_UB_round < mesh.getLB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
