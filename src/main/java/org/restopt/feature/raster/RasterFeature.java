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

import org.restopt.raster.RasterReader;
import org.restopt.feature.Feature;

import java.io.File;
import java.io.IOException;

/**
 * Feature based on a org.restopt.raster file.
 */
public abstract class RasterFeature implements Feature {

    protected String name;
    protected String rasterFilePath;
    protected RasterReader rasterReader;

    public RasterFeature(String rasterFilePath, String name) throws IOException {
        this.name = name;
        this.rasterFilePath = rasterFilePath;
        this.rasterReader = new RasterReader(rasterFilePath);
    }

    public RasterFeature(String rasterFilePath) throws IOException {
        this(rasterFilePath, new File(rasterFilePath).getName());
    }

    public double[] getData() throws IOException {
        return rasterReader.readAsDoubleArray();
    }

    @Override
    public String getName() {
        return name;
    }
}
