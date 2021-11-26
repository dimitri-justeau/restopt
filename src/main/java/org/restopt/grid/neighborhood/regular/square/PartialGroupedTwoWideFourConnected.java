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
import org.restopt.grid.regular.square.PartialRegularGroupedGrid;
import org.restopt.grid.regular.square.RegularSquareGrid;


/**
 * The four-connected neighborhood in a partial regular square org.flsgen.grid.
 */
public class PartialGroupedTwoWideFourConnected<T extends PartialRegularGroupedGrid> implements INeighborhood<T> {

    public int[] getNeighbors(T grid, int groupedIdx) {
        FourConnected four = Neighborhoods.FOUR_CONNECTED;
        RegularSquareGrid compGrid = new RegularSquareGrid(grid.getNbRows(), grid.getNbCols());
        if (groupedIdx < grid.getNbGroups()) {
            ISet nodes = grid.getGroup(groupedIdx);
            ISet fourNeigh = SetFactory.makeBipartiteSet(0);
            for (int i : nodes) {
                for (int j : four.getNeighbors(compGrid, grid.getCompleteIndex(i))) {
                    fourNeigh.add(j);
                }
            }
            ISet neighbors = SetFactory.makeBipartiteSet(0);
            for (int neigh : fourNeigh) {
                if (!grid.getDiscardSet().contains(neigh) && grid.getGroupIndexFromCompleteIndex(neigh) != groupedIdx) {
                    neighbors.add(grid.getGroupIndexFromCompleteIndex(neigh));
                }
                for (int nneigh : four.getNeighbors(compGrid, neigh)) {
                    if (!grid.getDiscardSet().contains(nneigh) && nneigh != neigh && grid.getGroupIndexFromCompleteIndex(nneigh) != groupedIdx) {
                        neighbors.add(grid.getGroupIndexFromCompleteIndex(nneigh));
                    }
                }
            }
            return neighbors.toArray();
        } else {
            int[] fourNeigh = four.getNeighbors(compGrid, grid.getUngroupedCompleteIndex(groupedIdx));
            ISet neighbors = SetFactory.makeBipartiteSet(0);
            for (int neigh : fourNeigh) {
                if (!grid.getDiscardSet().contains(neigh)) {
                    neighbors.add(grid.getGroupIndexFromCompleteIndex(neigh));
                }
                for (int nneigh : four.getNeighbors(compGrid, neigh)) {
                    if (!grid.getDiscardSet().contains(nneigh) && nneigh != neigh) {
                        neighbors.add(grid.getGroupIndexFromCompleteIndex(nneigh));
                    }
                }
            }
            return neighbors.toArray();
        }
    }
}
