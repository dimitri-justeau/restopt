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
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import static org.restopt.choco.LandscapeIndicesUtils.distance;
import static org.restopt.choco.LandscapeIndicesUtils.minidisk;

/**
 *
 */
public class PropSmallestEnclosingCircleSpatialGraph extends Propagator<Variable> {

    private final UndirectedGraphVar g;
    private final double[][] coordinates;
    private final RealVar radius;
    private final RealVar centerX;
    private final RealVar centerY;
    private final ISet pointsSet;

    public PropSmallestEnclosingCircleSpatialGraph(UndirectedGraphVar g, double[][] coordinates, RealVar radius, RealVar centerX, RealVar centerY) {
        super(new Variable[]{g, radius, centerX, centerY},
                PropagatorPriority.LINEAR,
                false
        );
        this.g = g;
        this.coordinates = coordinates;
        this.radius = radius;
        this.centerX = centerX;
        this.centerY = centerY;
        this.pointsSet = SetFactory.makeBipartiteSet(0);
    }

    private ISet getKernelPoints() {
        return g.getMandatoryNodes();
    }

    private ISet getEnvelopePoints() {
        return g.getPotentialNodes();
    }

    private ISet getEnvelopeMinusKernelPoints() {
        ISet ker = getKernelPoints();
        ISet env = getEnvelopePoints();
        ISet envMinusKer = SetFactory.makeBipartiteSet(0);
        for (int i : env) {
            if (!ker.contains(i)) {
                envMinusKer.add(i);
            }
        }
        return envMinusKer;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISet ker = getKernelPoints();
        ISet env = getEnvelopePoints();
        // Empty region is only allowed in radius lower bound is 0
        if (env.size() == 0) {
            if (radius.getLB() > 0) {
                fails();
            }
            return;
        }
        if (ker.size() == env.size()) {
            double[] minidisk = minidisk(ker.toArray(), coordinates);
            double x = minidisk[0];
            double y = minidisk[1];
            double r = minidisk[2];
            if (x > centerX.getUB() + centerX.getPrecision() || x < centerX.getLB() - centerX.getPrecision() ||
                    y > centerY.getUB() + centerY.getPrecision() || y < centerY.getLB() - centerY.getPrecision() ||
                    r > radius.getUB() + radius.getPrecision() || r < radius.getLB() - radius.getPrecision()) {
                fails();
            }
            radius.updateBounds(minidisk[2], minidisk[2], this);
            centerX.updateBounds(minidisk[0], minidisk[0], this);
            centerY.updateBounds(minidisk[1], minidisk[1], this);
            return;
        }
        if (ker.size() > 0) {
            // Check Kernel
            int[] kerArray = ker.toArray();
            double[] minidisk = minidisk(kerArray, coordinates);
            double[] cker = new double[]{minidisk[0], minidisk[1]};
            double rker = minidisk[2];
            if (rker > (radius.getUB() + radius.getPrecision())) {
                fails();
            }
            // Check Enveloppe
            int[] envArray = env.toArray();
            double[] minidiskEnv = minidisk(envArray, coordinates);
            double[] cEnv = new double[]{minidiskEnv[0], minidiskEnv[1]};
            double rEnv = minidiskEnv[2];
            if (rEnv < (radius.getLB() - radius.getPrecision())) {
                fails();
            }
            // Filter
            int[] pointsArray = new int[ker.size() + 1];
            for (int i = 0; i < kerArray.length; i++) {
                pointsArray[i] = kerArray[i];
            }
            for (int i : getEnvelopeMinusKernelPoints()) {
                if (distance(cker, coordinates[i]) > rker) {
                    pointsArray[pointsArray.length - 1] = i;
                    double[] b_disk = minidisk(pointsArray, coordinates);
                    if (b_disk[2] > (radius.getUB() + radius.getPrecision()) || b_disk[2] < (radius.getLB() - radius.getPrecision())) {
                        g.removeNode(i, this);
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        ISet ker = getKernelPoints();
        ISet env = getEnvelopePoints();
        if (env.size() == 0) {
            if (radius.getLB() > 0) {
                return ESat.FALSE;
            }
            return ESat.UNDEFINED;
        }
        if (ker.size() == env.size()) {
            double[] minidisk = minidisk(ker.toArray(), coordinates);
            double x = minidisk[0];
            double y = minidisk[1];
            double r = minidisk[2];
            if (x > centerX.getUB() + centerX.getPrecision() || x < centerX.getLB() - centerX.getPrecision() ||
                    y > centerY.getUB() + centerY.getPrecision() || y < centerY.getLB() - centerY.getPrecision() ||
                    r > radius.getUB() + radius.getPrecision() || r < radius.getLB() - radius.getPrecision()) {
                return ESat.TRUE;
            }
            return ESat.TRUE;
        }
        double[] minidisk_LB = minidisk(ker.toArray(), coordinates);
        double[] minidisk_UB = minidisk(env.toArray(), coordinates);
        if (minidisk_UB.length > 0) {
            double x_ub = minidisk_UB[0];
            double y_ub = minidisk_UB[1];
            double r_ub = minidisk_UB[2];
            if (r_ub < (radius.getLB() - radius.getPrecision())
                    || x_ub < (centerX.getLB() - centerX.getPrecision())
                    || y_ub < (centerY.getLB() - centerY.getPrecision())) {
                return ESat.FALSE;
            }
            if (minidisk_LB.length > 0) {
                double x_lb = minidisk_LB[0];
                double y_lb = minidisk_LB[1];
                double r_lb = minidisk_LB[2];
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
