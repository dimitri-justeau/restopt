package org.restopt.objective;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.objectives.EffectiveMeshSizeObjective;
import org.restopt.objectives.IntegralIndexOfConnectivityObjective;
import org.restopt.objectives.MinRestoreObjective;
import org.restopt.objectives.NbPlanningUnitsObjective;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestOptimalityGap {

    @Test
    public void testMeshOptimalityGap() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        double optGap = 0.005;
        List<RestoptSolution> sols = restoptProblem.maximizeMESH(10, 3, 30, optGap,true);
        double best_ref = Double.parseDouble(sols.get(0).getCharacteristics().get(EffectiveMeshSizeObjective.KEY_MESH_BEST));
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double initial = Double.parseDouble(sol.getCharacteristics().get(EffectiveMeshSizeObjective.KEY_MESH_INITIAL));
            double best = Double.parseDouble(sol.getCharacteristics().get(EffectiveMeshSizeObjective.KEY_MESH));
            Assert.assertTrue(best >= best_ref * (1 - optGap));
            Assert.assertTrue(initial < best);
        }
    }

    @Test
    public void testIICOptimalityGap() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        double optGap = 0.005;
        List<RestoptSolution> sols = restoptProblem.maximizeIIC(5, 4, 1, 30, 0.005,true);
        double best_ref = Double.parseDouble(sols.get(0).getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_BEST));
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double initial = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC_INITIAL));
            double best = Double.parseDouble(sol.getCharacteristics().get(IntegralIndexOfConnectivityObjective.KEY_IIC));
            Assert.assertTrue(initial < best);
            Assert.assertTrue(best >= best_ref * (1 - optGap));
        }
    }

    @Test
    public void testMinRestoreOptimalityGap() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        double optGap = 0.005;
        List<RestoptSolution> sols = restoptProblem.maximizeMinRestore(10, 30, optGap,true);
        double best_ref = Double.parseDouble(sols.get(0).getCharacteristics().get(MinRestoreObjective.KEY_MIN_RESTORE_BEST));
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double best = Double.parseDouble(sol.getCharacteristics().get(MinRestoreObjective.KEY_MIN_RESTORE));
            Assert.assertTrue(best >= best_ref * (1 - optGap));
        }
    }

    @Test
    public void testNbPlanningUnitsObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        double optGap = 0.3;
        List<RestoptSolution> sols = restoptProblem.minimizeNbPUS(10, 30, optGap,true);
        double best_ref = Double.parseDouble(sols.get(0).getCharacteristics().get(NbPlanningUnitsObjective.KEY_NB_PUS_BEST));
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            int best = Integer.parseInt(sol.getCharacteristics().get(sol.KEY_NB_PUS));
            Assert.assertTrue(best <= best_ref * (1 + optGap));
        }
    }
}
