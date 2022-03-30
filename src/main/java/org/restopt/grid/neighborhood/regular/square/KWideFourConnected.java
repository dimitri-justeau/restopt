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
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
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
        FourConnected four = Neighborhoods.FOUR_CONNECTED;
        int[] fourNeigh = four.getNeighbors(grid, i);
        ISet neighbors = SetFactory.makeRangeSet();
        ISet next = SetFactory.makeRangeSet();
        for (int n : fourNeigh) {
            neighbors.add(n);
            next.add(n);
        }
        for (int j = 1; j < k; j++) {
            int[] nextA = next.toArray();
            next.clear();
            for (int n : nextA) {
                for (int neigh : four.getNeighbors(grid, n)) {
                    neighbors.add(neigh);
                    next.add(neigh);
                }
            }
        }
        return neighbors.toArray();
    }

}
