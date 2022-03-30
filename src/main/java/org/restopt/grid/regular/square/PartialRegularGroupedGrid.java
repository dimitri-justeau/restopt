package org.restopt.grid.regular.square;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.restopt.choco.ConnectivityFinderSpatialGraph;
import org.restopt.exception.RestoptException;

import java.util.HashMap;
import java.util.Map;

public class PartialRegularGroupedGrid extends PartialRegularSquareGrid {

    private final int[] sizeCells;
    private final int nbGroups;
    private final ISet[] groups;
    private final ISet[] groupBorders;
    private final int nbGroupedCells;
    private final int[] unGroupedId;
    private final Map<Integer, Integer> gridIdToGroupedId;

    public PartialRegularGroupedGrid(int nbRows, int nbCols, int[] toDiscard, UndirectedGraph groupGraph) {
        this(nbRows, nbCols, toDiscard, groupGraph, 4);
    }

    public PartialRegularGroupedGrid(int nbRows, int nbCols, int[] toDiscard, UndirectedGraph groupGraph, int maxNeighCard) {
        super(nbRows, nbCols, toDiscard);
        this.nbGroupedCells = groupGraph.getNodes().size();
        ConnectivityFinderSpatialGraph connectivityFinder = new ConnectivityFinderSpatialGraph(groupGraph);
        connectivityFinder.findAllCC();
        this.nbGroups = connectivityFinder.getNBCC();
        this.sizeCells = new int[super.getNbCells() - nbGroupedCells + nbGroups];
        this.groups = new ISet[nbGroups];
        this.groupBorders = new ISet[nbGroups];
        for (int cc = 0; cc < nbGroups; cc++) {
            int sizeCC = connectivityFinder.getSizeCC()[cc];
            sizeCells[cc] = sizeCC;
            int[] g = connectivityFinder.getCC(cc);
            groupBorders[cc] = SetFactory.makeRangeSet();
            for (int i = 0; i < sizeCC; i++) {
                if (groupGraph.getNeighborsOf(g[i]).size() < maxNeighCard) {
                    groupBorders[cc].add(getPartialIndex(g[i]));
                }
                g[i] = getPartialIndex(g[i]);
            }
            groups[cc] = SetFactory.makeConstantSet(g);
//            groups[cc] = SetFactory.makeRangeSet();
//            int i = connectivityFinder.getCCFirstNode()[cc];
//            while (i != -1) {
//                groups[cc].add(getPartialIndex(i));
//                i = connectivityFinder.getCCNextNode()[i];
//            }
        }
        this.gridIdToGroupedId = new HashMap<>();
        for (int cc = 0; cc < nbGroups; cc++) {
            for (int i : groups[cc]) {
                gridIdToGroupedId.put(i, cc);
            }
        }
        this.unGroupedId = new int[getNbCells() - nbGroups];
        int nbNotGrouped = 0;
        for (int i = 0; i < super.getNbCells(); i++) {
            if (!gridIdToGroupedId.containsKey(i)) {
                gridIdToGroupedId.put(i, nbNotGrouped + nbGroups);
                unGroupedId[nbNotGrouped] = i;
                sizeCells[nbNotGrouped + nbGroups] = 1;
                nbNotGrouped++;
            }
        }
    }

    @Override
    public int getNbCells() {
        return super.getNbCells() - nbGroupedCells + nbGroups;
    }

    public int getNbUngroupedCells() {
        return super.getNbCells();
    }

    public ISet getGroup(int groupId) {
        return groups[groupId];
    }

    public ISet getGroupBorders(int group) {
        return groupBorders[group];
    }

    public int[] getSizeCells() {
        return sizeCells;
    }

    public int getNbGroups() {
        return nbGroups;
    }

    public int getUngroupedPartialIndex(int groupedIdx) {
        return unGroupedId[groupedIdx - nbGroups];
    }

    public int getUngroupedCompleteIndex(int groupedIdx) {
        return getCompleteIndex(unGroupedId[groupedIdx - nbGroups]);
    }

    public int getGroupIndexFromCompleteIndex(int completeIndex) {
        int partialIndex = getPartialIndex(completeIndex);
        return gridIdToGroupedId.get(partialIndex);
    }

    public int getGroupIndexFromPartialIndex(int partialIndex) {
        return gridIdToGroupedId.get(partialIndex);
    }

    /**
     * @return The cartesian coordinates of the pixels of the org.flsgen.grid.
     */
    public double[][] getCartesianCoordinates() {
        double[][] coords = new double[super.getNbCells()][];
        for (int i = 0; i < super.getNbCells(); i++) {
            int[] coord = super.getCoordinatesFromIndex(i);
            coords[i] = new double[]{coord[1], coord[0]};
        }
        return coords;
    }

    public double[] getCartesianCoordinates(int partialGroupedIndex) throws RestoptException {
        if (partialGroupedIndex < getNbGroups()) {
            throw new RestoptException("Cannot associate cartesian coordinates to a grouped cell");
        }
        int[] c = super.getCoordinatesFromIndex(getUngroupedPartialIndex(partialGroupedIndex));
        return new double[] {c[1], c[0]};
    }

    public double[] getCartesianCoordinatesFromPartialIndex(int partialIndex) {
        int[] c = super.getCoordinatesFromIndex(partialIndex);
        return new double[] {c[1], c[0]};
    }
}
