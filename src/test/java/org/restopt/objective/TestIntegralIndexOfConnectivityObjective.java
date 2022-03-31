package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.objectives.IntegralIndexOfConnectivityObjective;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestIntegralIndexOfConnectivityObjective {

    @Test
    public void testIICObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        List<RestoptSolution> sols = restoptProblem.maximizeIIC(10, 3, 30, true);
        double best_ref = -1;
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double initial = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_INITIAL));
            double best = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_BEST));
            Assert.assertTrue(initial < best);
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
        }
    }

    @Test
    public void testIICObjectiveCustomDistanceThreshold() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        List<RestoptSolution> sols = restoptProblem.maximizeIIC(10, 3, 10, 30, true);
        double best_ref = -1;
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double initial = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_INITIAL));
            double best = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_BEST));
            Assert.assertTrue(initial < best);
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
        }
    }

    @Test
    public void testIICObjectiveUnconstrained() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 0);
        RestoptSolution sol = restoptProblem.maximizeIIC(3, 30, true);
        sol.printSolutionInfos();
        double initial = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_INITIAL));
        double best = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_BEST));
        Assert.assertTrue(initial < best);
        Assert.assertTrue(best == 1);
    }
}
