package org.restopt;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class for DataLoader
 */
public class TestDataLoader {

    @Test
    public void testDataLoader() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        Assert.assertEquals(dataLoader.getHabitatData().length, dataLoader.getHeight() * dataLoader.getWidth());
        Assert.assertEquals(dataLoader.getAccessibleData().length, dataLoader.getHeight() * dataLoader.getWidth());
        Assert.assertEquals(dataLoader.getRestorableData().length, dataLoader.getHeight() * dataLoader.getWidth());
    }
}
