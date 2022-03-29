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

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Utility class to compute landscape indices on static objects.
 * e.g. to get the initial value of a landscape before solving.
 */
public class LandscapeIndicesUtils {

    public static double effectiveMeshSize(UndirectedGraph g, int landscapeArea) {
        ConnectivityFinderSpatialGraph connFinder = new ConnectivityFinderSpatialGraph(g);
        connFinder.findAllCC();
        double mesh = 0;
        for (int i = 0; i < connFinder.getNBCC(); i++) {
            int s = connFinder.getSizeCC()[i];
            mesh += 1.0 * s * s;
        }
        mesh /= 1.0 * landscapeArea;
        return mesh;
    }

    public static double[] getSmallestEnclosingCircle(double[][] coordinates) {
        int[] points = IntStream.range(0, coordinates.length).toArray();
        return minidisk(points, coordinates);
    }

    // ------------------------------------------------------------------------- //
    // Welzl's O(n) minidisk algorithm                                           //
    // See: Emo Welzl, "Smallest enclosing disks (balls and ellispsoids)", 1991. //
    // ------------------------------------------------------------------------- //

    /**
     * @param pointsArray Indexes of the points.
     * @return The smallest enclosing circle as new double[] {cx, cy, radius}.
     */
    public static final double[] minidisk(int[] pointsArray, double[][] coordinates) {
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
        int[] pointsCopy = Arrays.copyOf(pointsArray, pointsArray.length);
        ArrayUtils.randomPermutations(pointsCopy, System.currentTimeMillis());
        // First circle (with the two first points)
        double[] p1 = coordinates[pointsCopy[0]];
        double[] p2 = coordinates[pointsCopy[1]];
        double[] center = midpoint(p1, p2);
        double r = (distance(p1, p2) / 2);
        // Check other points
        for (int i = 2; i < pointsCopy.length; i++) {
            double[] pi = coordinates[pointsCopy[i]];
            if (distance(center, pi) > r) {
                // Find circle with pi in the border
                double[] circle = b_minidisk_one(coordinates, pointsCopy, i);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[]{center[0], center[1], r};
    }

    private static final double[] b_minidisk_one(double[][] coordinates, int[] shuffled, int i) {
        double[] p1 = coordinates[shuffled[0]];
        double[] pi = coordinates[shuffled[i]];
        double[] center = midpoint(p1, pi);
        double r = (distance(p1, pi) / 2);
        // Check whether previous points are included in this new circle
        for (int j = 1; j < i; j++) {
            double[] pj = coordinates[shuffled[j]];
            if (distance(center, pj) > r) {
                double[] circle = b_minidisk_two(coordinates, shuffled, i, j);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[]{center[0], center[1], r};
    }

    private static final double[] b_minidisk_two(double[][] coordinates, int[] shuffled, int i, int j) {
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
    public static final double[] vector(double[] p1, double[] p2) {
        return new double[]{p1[0] + p2[0], p1[1] + p2[1]};
    }

    /**
     * @return The midpoint of the segment (p1, p2)
     */
    public static final double[] midpoint(double[] p1, double[] p2) {
        double[] v = vector(p1, p2);
        return new double[]{v[0] / 2, v[1] / 2};
    }

    /**
     * @return The distance between p1 and p2
     */
    public static final double distance(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
    }

    /**
     * @return The circumcircle of the triangle (a, b, c).
     */
    public static final double[] circumcircle(double[] a, double[] b, double[] c) {
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
