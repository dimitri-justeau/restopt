package org.restopt.objective;

import org.restopt.DataLoader;
import org.restopt.RestoptProblem;
import org.restopt.objectives.RestoptSolution;
import org.restopt.raster.RasterReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestNbPlanningUnitsObjective {

    @Test
    public void testNbPlanningUnitsObjective() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterReader r = new RasterReader(cell_area);
        int[] cellArea = r.readAsIntArray();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postRestorableConstraint(90, 110, cellArea, 0.7);
        RestoptSolution sol = restoptProblem.maximizeNbPUS(30, true);
        sol.printSolutionInfos();
    }

    @Test
    public void testNbPlanningUnitsNoLimit() throws Exception {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 1);
        RestoptSolution sol = restoptProblem.maximizeNbPUS(30, true);
        sol.printSolutionInfos();
        Assert.assertEquals(sol.getRestorationPlanningUnits().length, restoptProblem.getAvailablePlanningUnits().length);
    }
}
