package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.objectives.NbPlanningUnitsObjective;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestNbPlanningUnitsObjective {

    @Test
    public void testNbPlanningUnitsObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        List<RestoptSolution> sols = restoptProblem.maximizeNbPUS(30, 10, true);
        int best_ref = -1;
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            int initial = Integer.parseInt(sol.getCharacteristics().get(NbPlanningUnitsObjective.KEY_NB_PUS_INITIAL));
            int best = Integer.parseInt(sol.getCharacteristics().get(NbPlanningUnitsObjective.KEY_NB_PUS_BEST));
            Assert.assertTrue(initial < best);
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
        }
    }

    @Test
    public void testNbPlanningUnitsNoLimit() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 1);
        RestoptSolution sol = restoptProblem.maximizeNbPUS(30, true);
        sol.printSolutionInfos();
        Assert.assertEquals(sol.getRestorationPlanningUnits().length, restoptProblem.getAvailablePlanningUnits().length);
    }
}
