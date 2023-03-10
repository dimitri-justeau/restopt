package org.restopt;

import org.restopt.exception.RestoptException;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Class for loading and accessing problem's data.
 */
public class DataLoader {

    protected int[] habitatData;
    protected double[] restorableData;
    protected int[] accessibleData;
    protected int[] cellAreaData;

    protected int width;
    protected int height;

    protected double noDataHabitat;

    public DataLoader(int[] habitatData, int[] accessibleData, double[] restorableData, int[] cellAreaData) throws RestoptException {
        this.habitatData = habitatData;
        this.accessibleData = accessibleData;
        this.restorableData = restorableData;
        this.cellAreaData = cellAreaData;
        int n = habitatData.length;
        if (accessibleData.length != n || restorableData.length != n || cellAreaData.length != n) {
            throw new RestoptException("All input datasets must have the same size");
        }
    }

    public DataLoader(int[] habitatData, int[] accessibleData, double[] restorableData, int[] cellAreaData, int width,
                      int height, double noDataHabitat) throws RestoptException {
        this(habitatData, accessibleData, restorableData, cellAreaData);
        this.width = width;
        this.height = height;
        this.noDataHabitat = noDataHabitat;
        if (width * height != habitatData.length) {
            throw new RestoptException("Input width and height do not correspond to dataset size");
        }
    }

    public DataLoader(int[] habitatData, int accessibleVal, double noDataHabitat, int width, int height) throws RestoptException {
        this(habitatData,
             IntStream.generate(() -> accessibleVal).limit(habitatData.length).toArray(),
             DoubleStream.generate(() -> 1).limit(habitatData.length).toArray(),
             IntStream.generate(() -> 1).limit(habitatData.length).toArray(),
             width, height, noDataHabitat);
    }

    public int[] getHabitatData() {
        return habitatData;
    }

    public double[] getRestorableData() {
        return restorableData;
    }

    public int[] getAccessibleData() {
        return accessibleData;
    }

    public int[] getCellAreaData() {
        return cellAreaData;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getNoDataValue() {
        return noDataHabitat;
    }
}
