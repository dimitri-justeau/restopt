package org.restopt.grid.regular.square;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.restopt.choco.ConnectivityFinderSpatialGraph;
import org.restopt.exception.RestoptException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PartialRegularGroupedAggGrid extends GroupedGrid {

    private int[] sizeCells;
    private final int nbGroups;
    private final ISet[] groups;
    private ISet[] aggregates;
    private int nbAggregates;
    private final int nbGroupedCells;
    private final ISet groupedCells;
    private final Map<Integer, Integer> gridIdToGroupedId;
    private final int aggregationFactor;

    public PartialRegularGroupedAggGrid(int nbRows, int nbCols, int[] toDiscard, UndirectedGraph groupGraph, int aggregationFactor) {
        super(nbRows, nbCols, toDiscard);
        this.aggregationFactor = aggregationFactor;
        this.nbGroupedCells = groupGraph.getNodes().size();
        this.groupedCells = groupGraph.getNodes();
        ConnectivityFinderSpatialGraph connectivityFinder = new ConnectivityFinderSpatialGraph(groupGraph);
        connectivityFinder.findAllCC();
        this.nbGroups = connectivityFinder.getNBCC();
        this.sizeCells = new int[super.getNbCells() - nbGroupedCells + nbGroups];
        this.groups = new ISet[nbGroups];
        this.gridIdToGroupedId = new HashMap<>();

        // Compute aggregates
        int nbMaxAggregates = (int) (Math.ceil(1.0 * nbCols / aggregationFactor) * Math.ceil(1.0 * nbRows / aggregationFactor));
        this.nbAggregates = 0;
        this.aggregates = new ISet[nbMaxAggregates];
        for (int i = 0; i < nbMaxAggregates; i++) {
            int[] agg = IntStream.of(getMaxAggregate(i)).filter(v -> !isGrouped(getCompleteIndex(v))).toArray();
            if (agg.length > 0) {
                aggregates[nbAggregates] = SetFactory.makeConstantSet(agg);
                for (int v : agg) {
                    gridIdToGroupedId.put(v, nbAggregates + nbGroups);
                }
                sizeCells[nbAggregates + nbGroups] = agg.length;
                nbAggregates++;
            }
        }
        this.aggregates = Arrays.copyOfRange(aggregates, 0, nbAggregates);

        for (int cc = 0; cc < nbGroups; cc++) {
            int sizeCC = connectivityFinder.getSizeCC()[cc];
            sizeCells[cc] = sizeCC;
            int[] g = connectivityFinder.getCC(cc);
            for (int i = 0; i < sizeCC; i++) {
                g[i] = getPartialIndex(g[i]);
            }
            groups[cc] = SetFactory.makeConstantSet(g);
        }

        this.sizeCells = Arrays.copyOfRange(sizeCells, 0, nbGroups + nbAggregates);

        for (int cc = 0; cc < nbGroups; cc++) {
            for (int i : groups[cc]) {
                gridIdToGroupedId.put(i, cc);
            }
        }
    }

    private boolean isGrouped(int completeIndex) {
        return groupedCells.contains(completeIndex);
    }

    private int[] getMaxAggregate(int idx) {
        int[] agg = new int[aggregationFactor * aggregationFactor];
        int i = 0;
        int nWidth = (int) Math.ceil(1.0 * nbCols / aggregationFactor);
        int offsetRow = Math.floorDiv(idx, nWidth) * aggregationFactor;
        int offsetCol = (idx % nWidth) * aggregationFactor;
        for (int row = 0; row < aggregationFactor; row++) {
            for (int col = 0; col < aggregationFactor; col++) {
                if ((row + offsetRow) < getNbRows() && (col + offsetCol) < getNbCols()) {
                    int compIdx = getCompleteIndexFromCoordinates((row + offsetRow), col + offsetCol);
                    if (!getDiscardSet().contains(compIdx)) {
                        agg[i] = getPartialIndex(compIdx);
                        i++;
                    }
                }
            }
        }
        return Arrays.copyOfRange(agg, 0, i);
    }

    @Override
    public int getNbCells() {
        return nbGroups + nbAggregates;
    }

    public int getNbUngroupedCells() {
        return super.getNbCells();
    }

    public ISet getGroup(int groupId) {
        return groups[groupId];
    }

    public int[] getSizeCells() {
        return sizeCells;
    }

    public int getNbGroups() {
        return nbGroups;
    }

    public int[] getAggregatePartialIndices(int aggregateIdx) {
        if (aggregateIdx < nbGroups) {
            throw new RuntimeException("Wrong aggregate index");
        }
        return aggregates[aggregateIdx - nbGroups].toArray();
    }

/*    public int getUngroupedPartialIndex(int groupedIdx) {
        if (groupedIdx < nbGroups) {
            throw new RuntimeException("A grouped cell is not associated to a complete index");
        }
        return unGroupedId[groupedIdx - nbGroups];
    }*/

    public int[] getAggregateCompleteIndices(int aggregateIdx) {
        if (aggregateIdx < nbGroups) {
            throw new RuntimeException("Wrong aggregate index");
        }
        return IntStream.of(aggregates[aggregateIdx - nbGroups].toArray())
                .map(i -> getCompleteIndex(i))
                .toArray();
    }

    @Override
    public int[] getUngroupedCompleteIndices(int[] pus) {
        ISet indices = SetFactory.makeRangeSet();
        for (int pu : pus) {
            for (int i : getAggregateCompleteIndices(pu)) {
                indices.add(i);
            }
        }
        return indices.toArray();
    }

/*    public int getUngroupedCompleteIndex(int groupedIdx) {
        if (groupedIdx < nbGroups) {
            throw new RuntimeException("A grouped cell is not associated to a complete index");
        }
        return getCompleteIndex(unGroupedId[groupedIdx - nbGroups]);
    }*/

    public int getGroupIndexFromCompleteIndex(int completeIndex) {
        int partialIndex = getPartialIndex(completeIndex);
        return gridIdToGroupedId.get(partialIndex);
    }

    public int getGroupIndexFromPartialIndex(int partialIndex) {
        if (!gridIdToGroupedId.containsKey(partialIndex)) {
            System.out.println(partialIndex);
            System.out.println(groupedCells.contains(getCompleteIndex(partialIndex)));
        }
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

    public double[] getCartesianCoordinates(int aggregateIdx) throws RestoptException {
        if (aggregateIdx < getNbGroups()) {
            throw new RestoptException("Cannot associate cartesian coordinates to a grouped cell");
        }
        int[] agg = aggregates[aggregateIdx - nbGroups].toArray();
        double[] coords = new double[] {0, 0};
        for (int i = 0; i < agg.length; i++) {
            double[] c = getCartesianCoordinatesFromPartialIndex(agg[i]);
            coords[0] += c[0];
            coords[1] += c[1];
        }
        coords[0] /= agg.length;
        coords[1] /= agg.length;
        return coords;
    }

    public double[] getCartesianCoordinatesFromPartialIndex(int partialIndex) {
        int[] c = super.getCoordinatesFromIndex(partialIndex);
        return new double[] {c[1], c[0]};
    }
}
