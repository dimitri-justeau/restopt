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
import org.restopt.grid.Grid;
import org.restopt.grid.neighborhood.INeighborhood;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to compute connectivity indices on static objects.
 * e.g. to get the initial value of a landscape before solving.
 */
public class ConnectivityIndices {

    public static float getIIC(UndirectedGraph g, Grid grid, INeighborhood threshold) {
        ConnectivityFinderSpatialGraph connectivityFinder = new ConnectivityFinderSpatialGraph(g);
        connectivityFinder.findAllCC();
        // Construct inter-patch graph
        int[][] neigh = new int[connectivityFinder.getNBCC()][];
        int[] nodeCC = connectivityFinder.getNodeCC();
        for (int i = 0; i < connectivityFinder.getNBCC(); i++) {
            Set<Integer> conn = new HashSet<>();
            int[] cc = connectivityFinder.getCC(i);
            for (int node : cc) {
                int[] n = threshold.getNeighbors(grid, node);
                for (int j : n) {
                    if (nodeCC[j] != i && g.getNodes().contains(j)) {
                        conn.add(nodeCC[j]);
                    }
                }
            }
            int[] adj = conn.stream().mapToInt(v -> v).toArray();
            Arrays.sort(adj);
            neigh[i] = adj;
        }
        // Compute IIC
        int[] sizeCC = connectivityFinder.getSizeCC();
        float iic_UB = 0;
        for (int i = 0; i < neigh.length; i++) {
            int[] dists = bfs(i, neigh);
            for (int j = 0; j < neigh.length; j++) {
                if (dists[j] >= 0) {
                    iic_UB +=  (sizeCC[i] * sizeCC[j]) / (1 + dists[j]);
                }
            }
        }
        return iic_UB  / (grid.getNbCells() * grid.getNbCells());
    }

    private static int[] bfs(int source, int[][] adj) {
        int n = adj.length;
        boolean[] visited = new boolean[n];
        int[] queue = new int[n];
        int front = 0;
        int rear = 0;
        int[] dist = new int[n];
        for (int i = 0; i < n; i++) {
            dist[i] = - 1;
        }
        int current;
        visited[source] = true;
        queue[front] = source;
        rear++;
        dist[source] = 0;
        while (front != rear) {
            current = queue[front++];
            for (int i : adj[current]) {
                if (!visited[i]) {
                    dist[i] = dist[current] + 1;
                    queue[rear++] = i;
                    visited[i] = true;
                }
            }
        }
        return dist;
    }
}
