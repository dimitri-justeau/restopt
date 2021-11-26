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

package org.restopt.feature.raster;

import org.restopt.feature.BinaryFeature;

import java.io.IOException;

/**
 * Binary feature loaded from a org.restopt.raster file.
 */
public class BinaryRasterFeature extends RasterFeature implements BinaryFeature {

    public BinaryRasterFeature(String rasterFilePath, String name) throws IOException {
        super(rasterFilePath, name);
    }

    public BinaryRasterFeature(String rasterFilePath) throws IOException {
        super(rasterFilePath);
    }

    @Override
    public int[] getBinaryData() throws IOException {
        return rasterReader.readAsIntArray();
    }
}
