package org.restopt.constraints;

import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestEffectiveMeshSizeConstraint {

    @Test
    public void testEffectiveMeshSizeConstraint() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postNbComponentsConstraint(1, 1);
        restoptProblem.postCompactnessConstraint(6);
        restoptProblem.postRestorableConstraint(90 * 11, 110 * 11, 0.7);
        restoptProblem.postMinMeshConstraint(652, 3);
        List<RestoptSolution> sols = restoptProblem.findSolutions(10, 30, true, "INPUT_ORDER_LB");
        for (RestoptSolution sol : sols) {
            sol.printSolutionInfos();
            double mesh = Double.parseDouble(sol.getCharacteristics().get(EffectiveMeshSizeConstraint.KEY_MESH));
            Assert.assertTrue(mesh >= 650);
        }
    }


}
