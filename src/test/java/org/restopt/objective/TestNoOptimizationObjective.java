package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestNoOptimizationObjective {

    @Test
    public void testNoOptimizationObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        RestoptSolution sol = restoptProblem.findSolution(30, true);
        sol.printSolutionInfos();
        int minRestore = Integer.parseInt(sol.getCharacteristics().get(sol.KEY_MIN_RESTORE));
        Assert.assertTrue(minRestore <= 110 * 11);
        Assert.assertTrue(minRestore >= 90 * 11);
        Assert.assertTrue(sol.getDiameter() <= 6);
        Assert.assertTrue(sol.getNbComponents() == 1);
    }
}
