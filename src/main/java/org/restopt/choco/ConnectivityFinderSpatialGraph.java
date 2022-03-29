/*
 * Copyright (c) 2018, Dimitri Justeau-Allaire
 *
 * CIRAD, UMR AMAP, F-34398 Montpellier, France
 * Institut Agronomique neo-Caledonien (IAC), 98800 Noumea, New Caledonia
 * AMAP, Univ Montpellier, CIRAD, CNRS, INRA, IRD, Montpellier, France
 *
 * This file is part of Choco-reserve.
 *
 * Choco-reserve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Choco-reserve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Choco-reserve.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.restopt.choco;


import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Class containing algorithms to find all connected components by performing one dfs
 * it uses Tarjan algorithm in a non recursive way and can be performed in O(M+N) time c.f. Gondrand Minoux
 * <p>
 * SEE UGVarConnectivityHelper
 *
 * @author Jean-Guillaume Fages
 */
public class ConnectivityFinderSpatialGraph {

    //***********************************************************************************
    // CONNECTED COMPONENTS ONLY
    //***********************************************************************************

    private final int n;
    private final UndirectedGraph g;
    private int[] CCFirstNode;
    private int[] CCNextNode;
    private int[] nodeCC;
    private final int[] p;
    private final int[] fifo;
    private int[] sizeCC;
    private int[] attributeCC;
    private final int[] attributeCell;
    private final int[][] neighs;
    private int nbCC;
    private int sizeMinCC;
    private int sizeMaxCC;

    /**
     * Create an object that can compute Connected Components (CC) of a graph g
     * Can also quickly tell whether g is biconnected or not (only for undirected graph)
     */
    public ConnectivityFinderSpatialGraph(UndirectedGraph g, UndirectedGraph GUB) {
        this(g, GUB, IntStream.range(0, g.getNbMaxNodes()).map(i -> 1).toArray());
    }

    public ConnectivityFinderSpatialGraph(UndirectedGraph g) {
        this(g, g, IntStream.range(0, g.getNbMaxNodes()).map(i -> 1).toArray());
    }

    public ConnectivityFinderSpatialGraph(UndirectedGraph g, int[] attributeCell) {
        this(g, g, attributeCell);
    }

    public ConnectivityFinderSpatialGraph(UndirectedGraph g, UndirectedGraph GUB, int[] attributeCell) {
        this.g = g;
        this.n = g.getNbMaxNodes();
        p = new int[n];
        fifo = new int[n];
        this.attributeCell = attributeCell;
        this.neighs = new int[n][];
        for (int i : GUB.getNodes()) {
            neighs[i] = GUB.getNeighborsOf(i).toArray();
        }
    }

    /**
     * get the number of CC in g
     * Beware you should call method findAllCC() first
     *
     * @return nbCC the number of CC in g
     */
    public int getNBCC() {
        return nbCC;
    }

    /**
     * Get the size (number of nodes) of the smallest CC in g.
     * Beware you should call method findAllCC() first.
     *
     * @return sizeMinCC the size of the smallest CC in g.
     */
    public int getSizeMinCC() {
        return sizeMinCC;
    }

    /**
     * Get the size (number of nodes) of the largest CC in g.
     * Beware you should call method findAllCC() first.
     *
     * @return sizeMaxCC the size of the largest CC in g.
     */
    public int getSizeMaxCC() {
        return sizeMaxCC;
    }

    /**
     * @return The size of the CCs as an int array.
     */
    public int[] getSizeCC() {
        return sizeCC;
    }

    public int[] getAttributeCC() {
        return attributeCC;
    }

    public int[] getCCFirstNode() {
        return CCFirstNode;
    }

    public int[] getCCNextNode() {
        return CCNextNode;
    }

    public int[] getNodeCC() {
        return nodeCC;
    }

    /**
     * Find all connected components of graph by performing one dfs
     * Complexity : O(M+N) light and fast in practice
     */
    public void findAllCC() {
        if (nodeCC == null) {
            CCFirstNode = new int[n];
            CCNextNode = new int[n];
            nodeCC = new int[n];
            sizeCC = new int[n];
            attributeCC = new int[n];
        }
        sizeMinCC = 0;
        sizeMaxCC = 0;
        for (int i : g.getNodes()) {
            p[i] = -1;
        }
        for (int i = 0; i < CCFirstNode.length; i++) {
            CCFirstNode[i] = -1;
            sizeCC[i] = -1;
            attributeCC[i] = -1;
        }
        int cc = 0;
        for (int i : g.getNodes()) {
            if (p[i] == -1) {
                findCC(i, cc);
                if (sizeMinCC == 0 || sizeMinCC > sizeCC[cc]) {
                    sizeMinCC = sizeCC[cc];
                }
                if (sizeMaxCC < sizeCC[cc]) {
                    sizeMaxCC = sizeCC[cc];
                }
                cc++;
            }
        }
        nbCC = cc;
    }

    private void findCC(int start, int cc) {
        int first = 0;
        int last = 0;
        int size = 1;
        int attribute = attributeCell[start];
        fifo[last++] = start;
        p[start] = start;
        add(start, cc);
        while (first < last) {
            int i = fifo[first++];
            for (int j : neighs[i]) {
                if (p[j] == -1) {
                    p[j] = i;
                    add(j, cc);
                    size += 1;
                    attribute += attributeCell[j];
                    fifo[last++] = j;
                }
            }
        }
        attributeCC[cc] = attribute;
        sizeCC[cc] = size;
    }

    private void add(int node, int cc) {
        nodeCC[node] = cc;
        CCNextNode[node] = CCFirstNode[cc];
        CCFirstNode[cc] = node;
    }

    public int[] getCC(int ccIndex) {
        int[] cc = new int[sizeCC[ccIndex]];
        int j = 0;
        int i = getCCFirstNode()[ccIndex];
        while (i != -1) {
            cc[j++] = i;
            i = getCCNextNode()[i];
        }
        return cc;
    }

    public int[] getCC(int ccIndex, ISet exclude) {
        int[] cc = new int[getSizeCC()[ccIndex]];
        int excluded = 0;
        int j = 0;
        int i = getCCFirstNode()[ccIndex];
        while (i != -1) {
            if (!exclude.contains(i)) {
                cc[j++] = i;
            } else {
                excluded++;
            }
            i = getCCNextNode()[i];
        }
        return Arrays.copyOfRange(cc, 0, cc.length - excluded);
    }
}
