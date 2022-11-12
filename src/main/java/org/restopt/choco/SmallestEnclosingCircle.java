package org.restopt.choco;

import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

public class SmallestEnclosingCircle {

    // ------------------------------------------------------------------------- //
    // Welzl's O(n) minidisk algorithm                                           //
    // See: Emo Welzl, "Smallest enclosing disks (balls and ellispsoids)", 1991. //
    // ------------------------------------------------------------------------- //

    int[] pointsArray;
    double[][] coordinates;
    int[] shuffledPoints;

    double centerX;
    double centerY;
    double radius;

    public SmallestEnclosingCircle(int[] pointsArray, double[][] coordinates) {
        this.coordinates = coordinates;
        this.pointsArray = pointsArray;
        this.shuffledPoints = Arrays.copyOf(pointsArray, pointsArray.length);
        ArrayUtils.randomPermutations(shuffledPoints, System.currentTimeMillis());
        double[] minidisk = minidisk(shuffledPoints, coordinates);
        if (minidisk.length > 0) {
            this.centerX = minidisk[0];
            this.centerY = minidisk[1];
            this.radius = minidisk[2];
        }
    }

    public double[] addPoint(double[] pi) {
        double[] p1 = coordinates[shuffledPoints[0]];
        double[] center = midpoint(p1, pi);
        double r = (distance(p1, pi) / 2);
        for (int j = 1; j < shuffledPoints.length; j++) {
            double[] pj = coordinates[shuffledPoints[j]];
            if (distance(center, pj) > r) {
                double[] circle = b_minidisk_two(coordinates, shuffledPoints, pi, j);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[] { center[0], center[1], r };
    }

    /**
     * @param pointsArray Indexes of the points.
     * @return The smallest enclosing circle as new double[] {cx, cy, radius}.
     */
    private static final double[] minidisk(int[] pointsArray, double[][] coordinates) {
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
                double[] circle = b_minidisk_one(coordinates, pointsArray, i);
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
                double[] circle = b_minidisk_two(coordinates, shuffled, pi, j);
                center[0] = circle[0];
                center[1] = circle[1];
                r = circle[2];
            }
        }
        return new double[]{center[0], center[1], r};
    }

    private static final double[] b_minidisk_two(double[][] coordinates, int[] shuffled, double[] pi, int j) {
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
