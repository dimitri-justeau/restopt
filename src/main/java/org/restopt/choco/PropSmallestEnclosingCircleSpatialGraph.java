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
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 *
 */
public class PropSmallestEnclosingCircleSpatialGraph extends Propagator<Variable> {

    private UndirectedGraphVar g;
    private double[][] coordinates;
    private RealVar radius;
    private RealVar centerX;
    private RealVar centerY;
    private ISet pointsSet;

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
            double[] minidisk = minidisk(ker.toArray());
            double x = minidisk[0] - centerX.getLB();
            double y = minidisk[1] - centerY.getLB();
            double r = minidisk[2] - radius.getLB();
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
            int[] kerArray = ker.toArray();
            double[] minidisk = minidisk(kerArray);
            double[] cker = new double[]{minidisk[0], minidisk[1]};
            double rker = minidisk[2];
            if (rker > (radius.getUB() + radius.getPrecision()) || rker < (radius.getLB() - radius.getPrecision())) {
                fails();
            }
            int[] pointsArray = new int[ker.size() + 1];
            for (int i = 0; i < kerArray.length; i++) {
                pointsArray[i] = kerArray[i];
            }
            for (int i : getEnvelopeMinusKernelPoints()) {
                if (distance(cker, coordinates[i]) > rker) {
                    pointsArray[pointsArray.length - 1] = i;
                    double[] b_disk = minidisk(pointsArray);
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
            double[] minidisk = minidisk(ker.toArray());
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
        double[] minidisk_LB = minidisk(ker.toArray());
        double[] minidisk_UB = minidisk(env.toArray());
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

    // ------------------------------------------------------------------------- //
    // Welzl's O(n) minidisk algorithm                                           //
    // See: Emo Welzl, "Smallest enclosing disks (balls and ellispsoids)", 1991. //
    // ------------------------------------------------------------------------- //

    /**
     * @param pointsArray Indexes of the points.
     * @return The smallest enclosing circle as new double[] {cx, cy, radius}.
     */
    private double[] minidisk(int[] pointsArray) {
        int nbPoints = pointsArray.length;
        if (nbPoints == 0) {
            return new double[]{};
        }
        if (nbPoints == 1) {
            double[] center = coordinates[pointsArray[0]];
            return new double[]{center[0], center[1], 0};
        }
        if (nbPoints == 2) {
            int i = pointsArray[0];
            int j = pointsArray[1];
            double[] p1 = coordinates[i];
            double[] p2 = coordinates[j];
            double[] center = midpoint(p1, p2);
            return new double[]{center[0], center[1], (distance(p1, p2) / 2)};
        }
        // Shuffle the points
        ArrayUtils.randomPermutations(pointsArray, System.currentTimeMillis());
//        List<Integer> shuffled = IntStream.of(points.toArray()).boxed().collect(Collectors.toList());
//        Collections.shuffle(shuffled);
        // First circle (with the two first points)
        double[] p1 = coordinates[pointsArray[0]];
        double[] p2 = coordinates[pointsArray[1]];
        double[] center = midpoint(p1, p2);
        double r = (distance(p1, p2) / 2);
        // Check other points
        for (int i = 2; i < pointsArray.length; i++) {
            double[] pi = coordinates[pointsArray[i]];
            if (distance(center, pi) > r) {
                // Find circle with pi in the border
                double[] circle = b_minidisk_one(pointsArray, i);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[]{center[0], center[1], r};
    }

    private double[] b_minidisk_one(int[] shuffled, int i) {
        double[] p1 = coordinates[shuffled[0]];
        double[] pi = coordinates[shuffled[i]];
        double[] center = midpoint(p1, pi);
        double r = (distance(p1, pi) / 2);
        // Check whether previous points are included in this new circle
        for (int j = 1; j < i; j++) {
            double[] pj = coordinates[shuffled[j]];
            if (distance(center, pj) > r) {
                double[] circle = b_minidisk_two(shuffled, i, j);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[]{center[0], center[1], r};
    }

    private double[] b_minidisk_two(int[] shuffled, int i, int j) {
        double[] pi = coordinates[shuffled[i]];
        double[] pj = coordinates[shuffled[j]];
        double[] center = midpoint(pi, pj);
        double r = (distance(pi, pj) / 2);
        // Check whether previous points are included in this new circle
        for (int k = 0; k < j; k++) {
            double[] pk = coordinates[shuffled[k]];
            if (distance(center, pk) > r) {
                double[] circle = circumcircle(pi, pj, pk);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[]{center[0], center[1], r};
    }

    /**
     * @return The coordinates of the vector (p1, p2)
     */
    private static double[] vector(double[] p1, double[] p2) {
        return new double[]{p1[0] + p2[0], p1[1] + p2[1]};
    }

    /**
     * @return The midpoint of the segment (p1, p2)
     */
    private static double[] midpoint(double[] p1, double[] p2) {
        double[] v = vector(p1, p2);
        return new double[]{v[0] / 2, v[1] / 2};
    }

    /**
     * @return The distance between p1 and p2
     */
    private static double distance(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
    }

    /**
     * @return The circumcircle of the triangle (a, b, c).
     */
    private static double[] circumcircle(double[] a, double[] b, double[] c) {
        double d = 2 * (a[0] * (b[1] - c[1]) + b[0] * (c[1] - a[1]) + c[0] * (a[1] - b[1]));
        double cx = ((Math.pow(a[0], 2) + Math.pow(a[1], 2)) * (b[1] - c[1])
                + (Math.pow(b[0], 2) + Math.pow(b[1], 2)) * (c[1] - a[1])
                + (Math.pow(c[0], 2) + Math.pow(c[1], 2)) * (a[1] - b[1])) / d;
        double cy = ((Math.pow(a[0], 2) + Math.pow(a[1], 2)) * (c[0] - b[0])
                + (Math.pow(b[0], 2) + Math.pow(b[1], 2)) * (a[0] - c[0])
                + (Math.pow(c[0], 2) + Math.pow(c[1], 2)) * (b[0] - a[0])) / d;
        double cr = distance(new double[]{cx, cy}, a);
        return new double[]{cx, cy, cr};
    }

}
