package org.restopt.grid.regular.square;

public abstract class GroupedGrid extends PartialRegularSquareGrid {

    public GroupedGrid(int nbRows, int nbCols, int[] toDiscard) {
        super(nbRows, nbCols, toDiscard);
    }

    public abstract int[] getSizeCells();

    public abstract int getNbGroups();

    public abstract int[] getUngroupedCompleteIndices(int[] pus);

    public int getNbUngroupedCells() {
        return super.getNbCells();
    }

    public abstract int getGroupIndexFromPartialIndex(int partialIndex);
}
