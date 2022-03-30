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

package org.restopt.grid.neighborhood.regular.square.partial;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.regular.square.PartialRegularSquareGrid;

/**
 * The 2-wide height-connected neighborhood in a regular square grid
 */
public class PartialTwoWideHeightConnected<T extends PartialRegularSquareGrid> implements INeighborhood<T> {

    public int[] getNeighbors(T grid, int i) {
        PartialHeightConnected height = Neighborhoods.PARTIAL_HEIGHT_CONNECTED;
        int[] heightneigh = height.getNeighbors(grid, i);
        ISet neighbors = SetFactory.makeBitSet(0);
        for (int neigh : heightneigh) {
            neighbors.add(neigh);
            for (int nneigh : height.getNeighbors(grid, neigh)) {
                neighbors.add(nneigh);
            }
        }
        return neighbors.toArray();
    }

}
