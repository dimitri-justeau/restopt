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

package org.restopt.raster;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;

import java.io.File;
import java.io.IOException;

/**
 * Raster file reader. For now, only GeoTIFF files are accepted.
 */
public class RasterReader {

    /**
     * Raster file path
     */
    private String filePath;

    /**
     * Metadata of the raster
     */
    private int width;
    private int height;

    /**
     * Constructor.
     *
     * @param rasterFilePath The path to the raster file.
     */
    public RasterReader(String rasterFilePath) throws IOException {
        this.filePath = rasterFilePath;
        loadMetaData();
    }

    /**
     * Loads the metadata of the org.restopt.raster.
     */
    private void loadMetaData() throws IOException {
        File file = new File(filePath);
        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D grid = reader.read(null);
        width = grid.getRenderedImage().getWidth();
        height = grid.getRenderedImage().getHeight();
        reader.dispose();
    }

    /**
     * @return The width (in pixels) of the org.restopt.raster.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The height (in pixels) of the org.restopt.raster.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return The values of the org.restopt.raster as a double matrix ([height][width]).
     * @throws IOException
     */
    public double[][] readAsDoubleMatrix() throws IOException {
        File file = new File(filePath);
        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D grid = reader.read(null);
        int height = grid.getRenderedImage().getHeight();
        int width = grid.getRenderedImage().getWidth();
        double[][] values = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                values[i][j] = grid.getRenderedImage().getData().getSampleDouble(j, i, 0);
            }
        }
        reader.dispose();
        return values;
    }

    /**
     * @return The values of the org.restopt.raster as a flattened double matrix.
     * @throws IOException
     */
    public double[] readAsDoubleArray() throws IOException {
        File file = new File(filePath);
        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D grid = reader.read(null);
        int height = grid.getRenderedImage().getHeight();
        int width = grid.getRenderedImage().getWidth();
        double[] values = new double[height * width];
        reader.dispose();
        return grid.getRenderedImage().getData().getSamples(0, 0, width, height, 0, values);
    }

    /**
     * @return The values of the org.restopt.raster as an int matrix ([height][width]).
     * @throws IOException
     */
    public int[][] readAsIntMatrix() throws IOException {
        File file = new File(filePath);
        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D grid = reader.read(null);
        int height = grid.getRenderedImage().getHeight();
        int width = grid.getRenderedImage().getWidth();
        int[][] values = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                values[i][j] = grid.getRenderedImage().getData().getSample(j, i, 0);
            }
        }
        reader.dispose();
        return values;
    }

    /**
     * @return The values of the org.restopt.raster as a flattened int array.
     * @throws IOException
     */
    public int[] readAsIntArray() throws IOException {
        File file = new File(filePath);
        GeoTiffReader reader = new GeoTiffReader(file);
        GridCoverage2D grid = reader.read(null);
        int height = grid.getRenderedImage().getHeight();
        int width = grid.getRenderedImage().getWidth();
        int[] values = new int[height * width];
        reader.dispose();
        return grid.getRenderedImage().getData().getSamples(0, 0, width, height, 0, values);
    }

}
