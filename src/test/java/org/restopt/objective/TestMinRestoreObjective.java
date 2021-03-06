package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.objectives.MinRestoreObjective;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestMinRestoreObjective {

    @Test
    public void testMaximizeMinRestoreObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        List<RestoptSolution> sols = restoptProblem.maximizeMinRestore(10, 30, 0,true);
        double best_ref = -1;
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double best = Double.parseDouble(sol.getCharacteristics().get(MinRestoreObjective.KEY_MIN_RESTORE));
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
        }
    }

    @Test
    public void testMinimizeMinRestoreObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        List<RestoptSolution> sols = restoptProblem.minimizeMinRestore(10, 30, 0,true);
        double best_ref = -1;
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double best = Double.parseDouble(sol.getCharacteristics().get(MinRestoreObjective.KEY_MIN_RESTORE));
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
        }
    }
}
