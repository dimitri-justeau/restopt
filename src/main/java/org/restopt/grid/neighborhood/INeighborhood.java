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

package org.restopt.grid.neighborhood;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.restopt.grid.Grid;

/**
 * Interface specifying a neighborhood definition in a org.flsgen.grid.
 */
public interface INeighborhood<T extends Grid> {

    /**
     * @param grid A org.flsgen.grid.
     * @param i    The index of a cell.
     * @return The neighbors of i in the org.flsgen.grid.
     */
    int[] getNeighbors(T grid, int i);

    /**
     * @param grid    A org.flsgen.grid.
     * @param model   The GraphModel to be associated with the graph.
     * @param setType The SetType to use for encoding the graph.
     * @return The full spatial graph associated to the org.flsgen.grid. Full means that there will be one node for each cell.
     */
    default UndirectedGraph getFullGraph(T grid, Model model, SetType setType) {
        int nbCells = grid.getNbCells();
        UndirectedGraph g = new UndirectedGraph(model, nbCells, setType, false);
        for (int i = 0; i < nbCells; i++) {
            g.addNode(i);
            int[] neighbors = getNeighbors(grid, i);
            for (int ii : neighbors) {
                g.addEdge(i, ii);
            }
        }
        return g;
    }

    default UndirectedGraph getPartialGraph(T grid, Model model, int[] cells, SetType nodeSetType, SetType edgeSetType) {
        int nbCells = grid.getNbCells();
        UndirectedGraph partialGraph = GraphFactory.makeStoredUndirectedGraph(model, nbCells, nodeSetType, edgeSetType);
        for (int i : cells) {
            partialGraph.addNode(i);
        }
        for (int i : cells) {
            int[] neighbors = getNeighbors(grid, i);
            for (int ii : neighbors) {
                if (partialGraph.getNodes().contains(ii)) {
                    partialGraph.addEdge(i, ii);
                }
            }
        }
        return partialGraph;
    }

    /**
     * @param grid    A org.flsgen.grid.
     * @param model   The GraphModel to be associated with the graph.
     * @param cells   The cells to be included in the graph.
     * @param setType The SetType to use for encoding the graph.
     * @return The partial graph associated to a subset of cells of the org.flsgen.grid.
     */
    default UndirectedGraph getPartialGraph(T grid, Model model, int[] cells, SetType setType) {
        return getPartialGraph(grid, model, cells, setType, setType);
    }

    default UndirectedGraph getPartialGraph(T grid, int[] cells, SetType nodeSetType, SetType edgeSetType) {
        int nbCells = grid.getNbCells();
        UndirectedGraph partialGraph = GraphFactory.makeUndirectedGraph(nbCells, nodeSetType, edgeSetType);
        for (int i : cells) {
            partialGraph.addNode(i);
        }
        for (int i : cells) {
            int[] neighbors = getNeighbors(grid, i);
            for (int ii : neighbors) {
                if (partialGraph.getNodes().contains(ii)) {
                    partialGraph.addEdge(i, ii);
                }
            }
        }
        return partialGraph;
    }

    default UndirectedGraph getPartialGraph(T grid, int[] cells, SetType setType) {
        return getPartialGraph(grid, cells, setType, setType);
    }

    default UndirectedGraph getPartialGraphUB(T grid, Model model, int[] cells, SetType setType) {
        return getPartialGraphUB(grid, model, cells, setType, false);
    }


    /**
     * @param grid    A org.flsgen.grid.
     * @param model   The GraphModel to be associated with the graph.
     * @param cells   The cells to be included in the graph.
     * @param setType The SetType to use for encoding the graph.
     * @return The partial graph associated to a subset of cells of the org.flsgen.grid.
     */
    default UndirectedGraph getPartialGraphUB(T grid, Model model, int[] cells, SetType setType, boolean decr) {
        int nbCells = grid.getNbCells();
        UndirectedGraph partialGraph;
        partialGraph = new UndirectedGraph(model, nbCells, setType, false);
        for (int i : cells) {
            partialGraph.addNode(i);
        }
        for (int i : cells) {
            int[] neighbors = getNeighbors(grid, i);
            for (int ii : neighbors) {
                if (partialGraph.getNodes().contains(ii)) {
                    partialGraph.addEdge(i, ii);
                }
            }
        }

        return partialGraph;
    }
}
