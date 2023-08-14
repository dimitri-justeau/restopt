package org.restopt.constraints;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.objectives.IntegralIndexOfConnectivityObjective;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestIntegralIndexOfConnectivityConstraint {

    @Test
    public void testIICConstraint() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        restoptProblem.postMinIICConstraint(0.195, 1, 3);
        List<RestoptSolution> sols = restoptProblem.findSolutions(15, 30, true, "MIN_DOM_LB");
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double iic = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityConstraint.KEY_IIC));
            Assert.assertTrue(iic >= 0.195);
        }
    }

}
