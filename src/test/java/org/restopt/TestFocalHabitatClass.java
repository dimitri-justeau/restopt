package org.restopt;

import org.restopt.exception.RestoptException;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestFocalHabitatClass {

    @Test
    public void testFocalHabitatClass() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/multizones/habitat.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, 1);
        FocalHabitatClass forest = new FocalHabitatClass(dataLoader, 0, 1, -1);
        FocalHabitatClass savanna = new FocalHabitatClass(dataLoader, 1, 1, -1);
    }

}
