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

package org.restopt.feature;

import org.restopt.feature.raster.BinaryRasterFeature;
import org.restopt.feature.raster.ProbabilisticRasterFeature;
import org.restopt.feature.raster.QuantitativeRasterFeature;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test for RasterFeature class.
 */
public class TestRasterFeature {

    @Test
    public void testBinary() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_binary.tif").getPath();
        try {
            BinaryFeature feature = new BinaryRasterFeature(fullPath);
            Assert.assertEquals(feature.getName(), "test_raster_binary.tif");
            int[] data = feature.getBinaryData();
            for (int d : data) {
                Assert.assertTrue(d == 0 || d == 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataProbabilistic() throws Exception {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_probabilistic.tif").getPath();
        try {
            ProbabilisticFeature feature = new ProbabilisticRasterFeature(fullPath, "probTest");
            Assert.assertEquals(feature.getName(), "probTest");
            double[] data = feature.getProbabilisticData();
            for (double d : data) {
                Assert.assertTrue(d >= 0 && d <= 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataQuantitative() {
        String fullPath = getClass().getClassLoader().getResource("raster/test_raster_quantitative.tif").getPath();
        try {
            QuantitativeFeature feature = new QuantitativeRasterFeature(fullPath);
            int[] data = feature.getQuantitativeData();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
