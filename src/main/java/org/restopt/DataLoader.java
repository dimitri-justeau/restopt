package org.restopt;

import org.restopt.feature.raster.QuantitativeRasterFeature;
import org.restopt.raster.RasterReader;

import java.io.IOException;

/**
 * Class for loading and accessing problem's data.
 */
public class DataLoader {

    private int[] habitatData;
    private int[] restorableData;
    private int[] accessibleData;

    private int width;
    private int height;

    private String habitatRasterPath;
    private String accessibleRasterPath;
    ;
    private String restorableRasterPath;

    public DataLoader(String habitatRasterPath, String accessibleRasterPath, String restorableRasterPath) throws IOException {
        this.habitatRasterPath = habitatRasterPath;
        this.accessibleRasterPath = accessibleRasterPath;
        this.restorableRasterPath = restorableRasterPath;
        // Check rasters dimensions
        RasterReader rasterHabitat = new RasterReader(habitatRasterPath);
        RasterReader rasterRestorable = new RasterReader(restorableRasterPath);
        RasterReader rasterAccessible = new RasterReader(accessibleRasterPath);
        height = rasterHabitat.getHeight();
        width = rasterHabitat.getWidth();
        if (height != rasterRestorable.getHeight() || height != rasterAccessible.getHeight()
                || width != rasterRestorable.getWidth() || width != rasterAccessible.getWidth()) {
            throw new IOException("All input rasters must have the same dimension");
        }
        // Load data
        QuantitativeRasterFeature habitat = new QuantitativeRasterFeature(habitatRasterPath);
        QuantitativeRasterFeature restorable = new QuantitativeRasterFeature(restorableRasterPath);
        QuantitativeRasterFeature accessible = new QuantitativeRasterFeature(accessibleRasterPath);
        habitatData = habitat.getQuantitativeData();
        restorableData = restorable.getQuantitativeData();
        accessibleData = accessible.getQuantitativeData();
    }

    public int[] getHabitatData() {
        return habitatData;
    }

    public int[] getRestorableData() {
        return restorableData;
    }

    public int[] getAccessibleData() {
        return accessibleData;
    }

    public String getHabitatRasterPath() {
        return habitatRasterPath;
    }

    public String getAccessibleRasterPath() {
        return accessibleRasterPath;
    }

    public String getRestorableRasterPath() {
        return restorableRasterPath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
