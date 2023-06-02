package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.objectives.NbPatchesObjective;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestNbPatchesObjective {

    @Test
    public void testNbPatchesObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        List<RestoptSolution> sols = restoptProblem.minimizeNbPatches(10,30, 0,true, "");
        double best_ref = -1;
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double initial = Double.parseDouble(sol.getCharacteristics().get(NbPatchesObjective.KEY_NB_PATCHES_INITIAL));
            double best = Double.parseDouble(sol.getCharacteristics().get(NbPatchesObjective.KEY_NB_PATCHES));
            if (best_ref == -1) {
                best_ref = best;
            } else {
                Assert.assertEquals(best, best_ref);
            }
            Assert.assertTrue(initial > best);
        }
    }

    @Test
    public void testEffectiveMeshSizeObjectiveUnconstrained() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 0);
        RestoptSolution sol = restoptProblem.minimizeNbPatches(1,30, 0,true, "").get(0);
        sol.printSolutionInfos();
        double initial = Double.parseDouble(sol.getCharacteristics().get(NbPatchesObjective.KEY_NB_PATCHES_INITIAL));
        double best = Double.parseDouble(sol.getCharacteristics().get(NbPatchesObjective.KEY_NB_PATCHES));
        Assert.assertTrue(initial > best);
        Assert.assertTrue(best == 1);
    }
}
