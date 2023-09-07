/*
 * Copyright (c) 2018, Dimitri Justeau-Allaire
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
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.subgraph.SubGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import org.chocosolver.util.tools.ArrayUtils;


import static org.restopt.choco.LandscapeIndicesUtils.*;

/**
 *
 */
public class PropSmallestEnclosingCircleSpatialGraph extends Propagator<Variable> {

    private final SubGraphVar g;
    private final double[][] coordinates;
    private final RealVar radius;
    private final RealVar centerX;
    private final RealVar centerY;

    //private ISet envMinusKer;

    private final SmallestEnclosingCircle minidiskLB;

    private final boolean ignoreLB;

    public PropSmallestEnclosingCircleSpatialGraph(SubGraphVar g, double[][] coordinates, RealVar radius, RealVar centerX, RealVar centerY) {
        this(g, coordinates, radius, centerX, centerY, true);
    }

    public PropSmallestEnclosingCircleSpatialGraph(SubGraphVar g, double[][] coordinates, RealVar radius, RealVar centerX, RealVar centerY, boolean ignoreLB) {
        super(ArrayUtils.append(g.getNodeVars(), new Variable[] {radius, centerX, centerY}),
                PropagatorPriority.LINEAR,
                false
        );
        this.g = g;
        this.coordinates = coordinates;
        this.radius = radius;
        this.centerX = centerX;
        this.centerY = centerY;
        this.ignoreLB = ignoreLB;
        this.minidiskLB = new SmallestEnclosingCircle(getKernelPoints(), coordinates);
        //this.envMinusKer = new SetDifference(getModel(), g.getPotentialNodes(), g.getMandatoryNodes(), SetType.BIPARTITESET, 0);
    }

    private int[] getKernelPoints() {
        int[] ker = new int[g.getNbMandatoryNodes()];
        for (int i = 0; i < g.getNbMandatoryNodes(); i++) {
            ker[i] = g.getMandatoryNode(i);
        }
        return ker;
    }

    private int[] getEnvelopePoints() {
        int[] env = new int[g.getNbPotentialNodes()];
        for (int i = 0; i < g.getNbPotentialNodes(); i++) {
            env[i] = g.getPotentialNode(i);
        }
        return env;
    }

    private int[] getEnvelopeMinusKernelPoints() {
        int[] envMinusKer = new int[g.getNbPotentialNodes() - g.getNbMandatoryNodes()];
        int k = 0;
        for (int i = 0; i < g.getNbPotentialNodes(); i++) {
            int node = g.getPotentialNode(i);
            if (!g.getConnectedComponentsLB().containsNode(node)) {
                envMinusKer[k] = node;
                k++;
            }
            if (k == envMinusKer.length) {
                break;
            }
        }
        return envMinusKer;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        g.getConnectedComponentsLB().initSearchLBAndUB(g.getConnectedComponentsUB());
        int[] ker = getKernelPoints();
        int[] env = getEnvelopePoints();
        // Empty region is only allowed in radius lower bound is 0
        if (env.length == 0) {
            if (radius.getLB() > 0) {
                fails();
            }
            return;
        }
        if (ker.length == env.length) {
            minidiskLB.init(ker);
            double x = minidiskLB.centerX;
            double y = minidiskLB.centerY;
            double r = minidiskLB.radius;
            if (x > centerX.getUB() + centerX.getPrecision() || x < centerX.getLB() - centerX.getPrecision() ||
                    y > centerY.getUB() + centerY.getPrecision() || y < centerY.getLB() - centerY.getPrecision() ||
                    r > radius.getUB() + radius.getPrecision() || r < radius.getLB() - radius.getPrecision()) {
                fails();
            }
            radius.updateBounds(r, r, this);
            centerX.updateBounds(x, x, this);
            centerY.updateBounds(y, y, this);
            return;
        }
        if (ker.length > 0) {
            // Check Kernel
            minidiskLB.init(ker);
            double[] cKer = minidiskLB.getCenter();
            double rKer = minidiskLB.radius;
            if (rKer > (radius.getUB() + radius.getPrecision())) {
                fails();
            }
            // Check Enveloppe
            if (!ignoreLB) {
                int[] envArray = env;
                SmallestEnclosingCircle minidiskEnv = new SmallestEnclosingCircle(envArray, coordinates);
                double rEnv = minidiskEnv.radius;
                if (rEnv < (radius.getLB() - radius.getPrecision())) {
                    fails();
                }
            }

            // Filter

            ISet remove = SetFactory.makeRangeSet();
            for (int i : getEnvelopeMinusKernelPoints()) {
                double d = distance(cKer, coordinates[i]);
                if (d > radius.getUB() + radius.getPrecision()) {
                    if ((d > rKer + 2 * (radius.getUB() + radius.getPrecision()))) {
                        remove.add(i);
                    } else if (ker.length > 1) {
                        double[] b_disk = minidiskLB.addPoint(coordinates[i]);
                        if (b_disk[2] > (radius.getUB() + radius.getPrecision()) || b_disk[2] < (radius.getLB() - radius.getPrecision())) {
                            remove.add(i);
                        }
                    }
                }
            }
            for (int i : remove) {
                g.removeNode(i, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        g.getConnectedComponentsLB().initSearchLBAndUB(g.getConnectedComponentsUB());
        int[] ker = getKernelPoints();
        int[] env = getEnvelopePoints();
        if (env.length == 0) {
            if (radius.getLB() > 0) {
                return ESat.FALSE;
            }
            return ESat.UNDEFINED;
        }
        if (ker.length == env.length) {
            minidiskLB.init(ker);
            double x = minidiskLB.centerX;
            double y = minidiskLB.centerY;
            double r = minidiskLB.radius;
            if (x > centerX.getUB() + centerX.getPrecision() || x < centerX.getLB() - centerX.getPrecision() ||
                    y > centerY.getUB() + centerY.getPrecision() || y < centerY.getLB() - centerY.getPrecision() ||
                    r > radius.getUB() + radius.getPrecision() || r < radius.getLB() - radius.getPrecision()) {
                return ESat.TRUE;
            }
            return ESat.TRUE;
        }
        minidiskLB.init(ker);
        SmallestEnclosingCircle minidisk_UB = new SmallestEnclosingCircle(env, coordinates);
        if (minidisk_UB.pointsArray.length > 0) {
            double x_ub = minidisk_UB.centerX;
            double y_ub = minidisk_UB.centerY;
            double r_ub = minidisk_UB.radius;
            if (r_ub < (radius.getLB() - radius.getPrecision())
                    || x_ub < (centerX.getLB() - centerX.getPrecision())
                    || y_ub < (centerY.getLB() - centerY.getPrecision())) {
                return ESat.FALSE;
            }
            if (minidiskLB.pointsArray.length > 0) {
                double x_lb = minidiskLB.centerX;
                double y_lb = minidiskLB.centerY;
                double r_lb = minidiskLB.radius;
                if (r_lb > (radius.getUB() + radius.getPrecision())
                        || x_lb > (centerX.getUB() + centerX.getPrecision())
                        || y_lb > (centerY.getUB() + centerY.getPrecision())) {
                    return ESat.FALSE;
                }
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }
}
