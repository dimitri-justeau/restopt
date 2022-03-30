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

package org.restopt.grid.neighborhood.regular.square.partial.grouped;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.restopt.grid.neighborhood.INeighborhood;
import org.restopt.grid.neighborhood.Neighborhoods;
import org.restopt.grid.neighborhood.regular.square.FourConnected;
import org.restopt.grid.neighborhood.regular.square.KWideFourConnected;
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;


/**
 * The k-wide four-connected neighborhood in a partial grouped regular square grid
 */
public class PartialGroupedKWideFourConnected<T extends PartialRegularGroupedGrid> implements INeighborhood<T> {

    private int k;

    public PartialGroupedKWideFourConnected(int k) {
        this.k = k;
    }

    public int[] getNeighbors(T grid, int groupedIdx) {
        FourConnected four = Neighborhoods.FOUR_CONNECTED;
        KWideFourConnected kFour = Neighborhoods.K_WIDE_FOUR_CONNECTED(k - 1);
        RegularSquareGrid compGrid = new RegularSquareGrid(grid.getNbRows(), grid.getNbCols());
        if (groupedIdx < grid.getNbGroups()) {
            ISet nodes = grid.getGroup(groupedIdx);
            ISet fourNeigh = SetFactory.makeRangeSet();
            for (int i : nodes) {
                for (int j : four.getNeighbors(compGrid, grid.getCompleteIndex(i))) {
                    if (grid.getDiscardSet().contains(j) || grid.getGroupIndexFromCompleteIndex(j) != groupedIdx) {
                        fourNeigh.add(j);
                    }
                }
            }
            ISet neighbors = SetFactory.makeRangeSet();
            for (int neigh : fourNeigh) {
                if (!grid.getDiscardSet().contains(neigh) && grid.getGroupIndexFromCompleteIndex(neigh) != groupedIdx) {
                    neighbors.add(grid.getGroupIndexFromCompleteIndex(neigh));
                }
                for (int nneigh : kFour.getNeighbors(compGrid, neigh)) {
                    if (!grid.getDiscardSet().contains(nneigh) && nneigh != neigh && grid.getGroupIndexFromCompleteIndex(nneigh) != groupedIdx) {
                        neighbors.add(grid.getGroupIndexFromCompleteIndex(nneigh));
                    }
                }
            }
            return neighbors.toArray();
        } else {
            int[] fourNeigh = four.getNeighbors(compGrid, grid.getUngroupedCompleteIndex(groupedIdx));
            ISet neighbors = SetFactory.makeRangeSet();
            for (int neigh : fourNeigh) {
                if (!grid.getDiscardSet().contains(neigh)) {
                    neighbors.add(grid.getGroupIndexFromCompleteIndex(neigh));
                }
                for (int nneigh : kFour.getNeighbors(compGrid, neigh)) {
                    if (!grid.getDiscardSet().contains(nneigh) && nneigh != neigh) {
                        neighbors.add(grid.getGroupIndexFromCompleteIndex(nneigh));
                    }
                }
            }
            return neighbors.toArray();
        }
    }
}
