package org.restopt.constraints;

import org.restopt.BaseProblem;
import org.restopt.DataLoader;
import org.restopt.objectives.RestoptSolution;
import org.restopt.raster.RasterReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestRestorableAreaConstraint {

    @Test
    public void testMaxRestorableArea() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterReader r = new RasterReader(cell_area);
        int[] cellArea = r.readAsIntArray();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postRestorableConstraint(0, 110, cellArea, 1);
        RestoptSolution sol = baseProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int restoreArea = sol.getMinRestoreArea();
        int totalRestorable = sol.getTotalRestorableArea();
        Assert.assertEquals(restoreArea, totalRestorable);
        Assert.assertTrue(restoreArea <= 110);
        int sum = 0;
        int[] pus = sol.getRestorationPlanningUnits();
        for (int i : pus) {
            int completeUngroupedIndex = baseProblem.getGrid().getUngroupedCompleteIndex(i);
            sum += (int) Math.round(dataLoader.getRestorableData()[completeUngroupedIndex]);
        }
        Assert.assertEquals(sum, restoreArea);
    }

    @Test
    public void testMinMaxRestorableArea() throws IOException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterReader r = new RasterReader(cell_area);
        int[] cellArea = r.readAsIntArray();
        DataLoader dataLoader = new DataLoader(habitat, accessible, restorable);
        BaseProblem baseProblem = new BaseProblem(dataLoader, 2);
        baseProblem.postRestorableConstraint(120, 200, cellArea, 0.7);
        RestoptSolution sol = baseProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int restoreArea = sol.getMinRestoreArea();
        int totalRestorable = sol.getTotalRestorableArea();
        Assert.assertTrue(restoreArea != totalRestorable);
        Assert.assertTrue(restoreArea <= 200);
        Assert.assertTrue(restoreArea >= 120);
        int sumRest = 0;
        int sumTotal = 0;
        int[] pus = sol.getRestorationPlanningUnits();
        for (int i : pus) {
            int completeUngroupedIndex = baseProblem.getGrid().getUngroupedCompleteIndex(i);
            int rest = (int) Math.round(dataLoader.getRestorableData()[completeUngroupedIndex]);
            sumTotal += rest;
            int cArea = cellArea[completeUngroupedIndex];
            int threshold = (int) Math.ceil(cArea * (1 - 0.7));
            sumRest += rest <= threshold ? 0 : rest - threshold;
        }
        Assert.assertEquals(sumTotal, totalRestorable);
        Assert.assertEquals(sumRest, restoreArea);
    }
}
