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

package org.restopt.feature.array;

import org.restopt.feature.BinaryFeature;

import java.util.stream.IntStream;

/**
 * Binary feature loaded from an int[].
 */
public class BinaryArrayFeature extends ArrayFeature implements BinaryFeature {

    protected int[] data;

    public BinaryArrayFeature(String name, int[] data) {
        super(name);
        this.data = data;
    }

    @Override
    public double[] getData() {
        return IntStream.of(data).mapToDouble(i -> (double) i).toArray();
    }

    @Override
    public int[] getBinaryData() {
        return IntStream.of(data).map(i -> i >= 1 ? 1 : 0).toArray();
    }
}
