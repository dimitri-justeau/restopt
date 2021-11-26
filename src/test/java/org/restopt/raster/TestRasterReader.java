/*
 * Copyright (c) 2018, Dimitri Justeau-Allaire
 *
 * CIRAD, UMR AMAP, F-34398 Montpellier, France
 * Institut Agronomique neo-Caledonien (IAC), 98800 Noumea, New Caledonia
 * AMAP, Univ Montpellier, CIRAD, CNRS, INRA, IRD, Montpellier, Franc
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

package org.restopt.raster;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test case for RasterReader.
 */
public class TestRasterReader {

    @Test
    public void testReadMetaData() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_binary.tif").getPath();
        try {
            RasterReader reader = new RasterReader(fullPath);
            int width = reader.getWidth();
            int height = reader.getHeight();
            Assert.assertEquals(width, 75);
            Assert.assertEquals(height, 46);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testWrongPath() {
        String fullPath = "Wrong path";
        try {
            new RasterReader(fullPath);
            Assert.fail();
        } catch (IOException e) {
        }
    }

    @Test
    public void testReadAsDoubleMatrix() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_probabilistic.tif").getPath();
        try {
            RasterReader reader = new RasterReader(fullPath);
            double[][] data = reader.readAsDoubleMatrix();
            Assert.assertEquals(data.length, 46);
            Assert.assertEquals(data[0].length, 75);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testReadAsDoubleArray() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_probabilistic.tif").getPath();
        try {
            RasterReader reader = new RasterReader(fullPath);
            double[] data = reader.readAsDoubleArray();
            Assert.assertEquals(data.length, 46 * 75);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testReadAsIntMatrix() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_binary.tif").getPath();
        try {
            RasterReader reader = new RasterReader(fullPath);
            int[][] data = reader.readAsIntMatrix();
            Assert.assertEquals(data.length, 46);
            Assert.assertEquals(data[0].length, 75);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testReadAsIntArray() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_binary.tif").getPath();
        try {
            RasterReader reader = new RasterReader(fullPath);
            int[] data = reader.readAsIntArray();
            Assert.assertEquals(data.length, 46 * 75);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
