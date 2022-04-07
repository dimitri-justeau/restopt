/*
 * Copyright (c) 2021, Dimitri Justeau-Allaire
 *
 * Institut Agronomique neo-Caledonien (IAC), 98800 Noumea, New Caledonia
 * AMAP, Univ Montpellier, CIRAD, CNRS, INRA, IRD, Montpellier, France
 *
 * This file is part of flsgen.
 *
 * flsgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * flsgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with flsgen.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.restopt.grid.neighborhood.regular.square;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.regular.square.RegularSquareGrid;

/**
 * The 2-wide four-connected neighborhood in a regular square org.flsgen.grid.
 */
public class KWideFourConnected<T extends RegularSquareGrid> implements INeighborhood<T> {

    private int k;

    public KWideFourConnected(int k) {
        this.k = k;
    }

    public int[] getNeighbors(T grid, int i) {
        int[] c = grid.getCoordinatesFromIndex(i);
        return discreteDisk(c[1], c[0], k, grid).toArray();
    }

    public static ISet discreteDisk(int x_centre, int y_centre, int k, RegularSquareGrid grid) {
        IntIterableRangeSet s = (IntIterableRangeSet) SetFactory.makeRangeSet();
        for (int i = 1; i <= k; i++) {
            s.addAll((IntIterableRangeSet) discreteCircle(x_centre, y_centre, i, grid));
        }
        return s;
    }

    private static boolean inGrid(int row, int col, RegularSquareGrid grid) {
        return row >= 0 && row < grid.getNbRows() && col >= 0 && col < grid.getNbCols();
    }

    /**
     * Andres algorithm: https://www.sciencedirect.com/science/article/pii/0097849394901643?via%3Dihub
     */
    public static ISet discreteCircle(int x_centre, int y_centre, int r, RegularSquareGrid grid) {
        ISet pixels = SetFactory.makeRangeSet();
        int x = 0;
        int y = r;
        int d = r - 1;
        while(y >= x) {
            int[][] points = new int[][] {
                    {y_centre + y, x_centre + x},
                    {y_centre + x, x_centre + y},
                    {y_centre + y, x_centre - x},
                    {y_centre + x, x_centre - y},
                    {y_centre - y, x_centre + x},
                    {y_centre - x, x_centre + y},
                    {y_centre - y, x_centre - x},
                    {y_centre - x, x_centre - y},
            };
            for (int[] p : points) {
                if (inGrid(p[0], p[1], grid)) {
                    pixels.add(grid.getIndexFromCoordinates(p[0], p[1]));
                }
            }
            if (d >= 2*x) {
                d -= 2*x + 1;
                x ++;
            } else if (d < 2 * (r-y)) {
                d += 2*y - 1;
                y --;
            } else {
                d += 2*(y - x - 1);
                y --;
                x ++;
            }
        }
        return pixels;
    }
}
