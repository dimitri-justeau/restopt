package org.restopt;

import org.restopt.DataLoader;
import org.restopt.exception.RestoptException;
import org.restopt.raster.RasterReader;

import java.io.IOException;

/**
 * Class for loading and accessing problem's data.
 */
public class RasterDataLoader extends DataLoader {

    private final String habitatRasterPath;

    public RasterDataLoader(String habitatRasterPath, String accessibleRasterPath, String restorableRasterPath,
                            String cellAreaRasterPath) throws IOException, RestoptException {
        super(
                new RasterReader(habitatRasterPath).readAsIntArray(),
                new RasterReader(accessibleRasterPath).readAsIntArray(),
                new RasterReader(restorableRasterPath).readAsDoubleArray(),
                new RasterReader(cellAreaRasterPath).readAsIntArray()
        );
        this.habitatRasterPath = habitatRasterPath;
        RasterReader rasterHabitat = new RasterReader(habitatRasterPath);
        this.width = rasterHabitat.getWidth();
        this.height = rasterHabitat.getHeight();
        this.noDataHabitat = rasterHabitat.getNoDataValue();
        if (width * height != habitatData.length) {
            throw new RestoptException("Input width and height do not correspond to dataset size");
        }
    }

    public String getHabitatRasterPath() {
        return habitatRasterPath;
    }
}
