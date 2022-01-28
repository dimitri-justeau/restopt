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

/**
 * Utility class to compute landscape indices on static objects.
 * e.g. to get the initial value of a landscape before solving.
 */
public class LandscapeIndicesUtils {

    public static double effectiveMeshSize(UndirectedGraph g, int landscapeArea) {
        ConnectivityFinderSpatialGraph connFinder = new ConnectivityFinderSpatialGraph(g);
        connFinder.findAllCC();
        double mesh = 0;
        for (int i = 0; i < connFinder.getNBCC(); i++) {
            int s = connFinder.getSizeCC()[i];
            mesh += 1.0 * s * s;
        }
        mesh /= 1.0 * landscapeArea;
        return mesh;
    }

}
