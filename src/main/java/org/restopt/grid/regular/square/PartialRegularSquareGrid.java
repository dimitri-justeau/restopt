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

package org.restopt.grid.regular.square;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import java.util.stream.IntStream;

/**
 * Partial Regular square org.flsgen.grid : subset of a nbRows x nbCols org.flsgen.grid.
 */
public class PartialRegularSquareGrid extends RegularSquareGrid {

    protected ISet discardSet;
    protected int[] partialToComplete;
    protected int[] completeToPartial;

    public PartialRegularSquareGrid(int nbRows, int nbCols, int[] toDiscard) {
        super(nbRows, nbCols);
        this.discardSet = SetFactory.makeRangeSet();
        IntStream.of(toDiscard).forEach(i -> discardSet.add(i));
        this.partialToComplete = new int[nbRows * nbCols - discardSet.size()];
        this.completeToPartial = new int[nbRows * nbCols];
        int j = 0;
        for (int i = 0; i < nbRows * nbCols; i++) {
            if (!discardSet.contains(i)) {
                partialToComplete[j] = i;
                completeToPartial[i] = j;
                j++;
            } else {
                completeToPartial[i] = -1;
            }
        }
    }

    @Override
    public int getNbCells() {
        return nbRows * nbCols - discardSet.size();
    }

    /**
     * @param row The row.
     * @param col The column.
     * @return The flattened index of a cell from its org.flsgen.grid coordinates.
     */
    public int getIndexFromCoordinates(int row, int col) {
        assert row >= 0;
        assert row < nbRows;
        assert col >= 0;
        assert col < nbCols;
        return getPartialIndex(getNbCols() * row + col);
    }

    /**
     * @param index The flattened index of a cell.
     * @return The org.flsgen.grid coordinates [row, col] from its flattened index.
     */
    public int[] getCoordinatesFromIndex(int index) {
        int completeIndex = getCompleteIndex(index);
        int row = Math.floorDiv(completeIndex, getNbCols());
        int col = completeIndex % getNbCols();
        return new int[]{row, col};
    }

    /**
     * @param partialIdx The partial org.flsgen.grid index.
     * @return The complete org.flsgen.grid index.
     */
    public int getCompleteIndex(int partialIdx) {
        return partialToComplete[partialIdx];
    }

    /**
     * @param completeIdx The complete org.flsgen.grid index.
     * @return The partial org.flsgen.grid index.
     */
    public int getPartialIndex(int completeIdx) {
        return completeToPartial[completeIdx];
    }

    public ISet getDiscardSet() {
        return discardSet;
    }

    /**
     * @return The number of rows.
     */
    public int getNbRows() {
        return nbRows;
    }

    /**
     * @return The number of columns.
     */
    public int getNbCols() {
        return nbCols;
    }

    /**
     * @return The cartesian coordinates of the pixels of the org.flsgen.grid.
     */
    public double[][] getCartesianCoordinates() {
        double[][] coords = new double[getNbCells()][];
        for (int i = 0; i < getNbCells(); i++) {
            int[] coord = getCoordinatesFromIndex(i);
            coords[i] = new double[]{coord[1], coord[0]};
        }
        return coords;
    }
}
