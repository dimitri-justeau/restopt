package org.restopt.constraints;

import org.restopt.RasterDataLoader;
import org.restopt.RestoptProblem;
import org.restopt.RestoptSolution;
import org.restopt.exception.RestoptException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestRestorableAreaConstraint {

    @Test
    public void testMaxRestorableArea() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postRestorableConstraint(0, 110 * 11, 1);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int restoreArea = sol.getMinRestoreArea();
        int totalRestorable = sol.getTotalRestorableArea();
        Assert.assertEquals(restoreArea, totalRestorable);
        Assert.assertTrue(restoreArea <= 110 * 11);
        int sum = 0;
        int[] pus = sol.getRestorationPlanningUnits();
        for (int i : pus) {
            //int completeUngroupedIndex = restoptProblem.getGrid().getUngroupedCompleteIndex(i);
            sum += restoptProblem.getRestorableArea(i);
        }
        Assert.assertEquals(sum, restoreArea);
    }

    @Test
    public void testMinMaxRestorableArea() throws IOException, RestoptException {
        String habitat = getClass().getClassLoader().getResource("example_data/habitat.tif").getPath();
        String restorable = getClass().getClassLoader().getResource("example_data/restorable.tif").getPath();
        String accessible = getClass().getClassLoader().getResource("example_data/accessible.tif").getPath();
        String cell_area = getClass().getClassLoader().getResource("example_data/cell_area.tif").getPath();
        RasterDataLoader dataLoader = new RasterDataLoader(habitat, accessible, restorable, cell_area);
        RestoptProblem restoptProblem = new RestoptProblem(dataLoader, 2);
        restoptProblem.postRestorableConstraint(120 * 11, 200 * 11, 0.7);
        RestoptSolution sol = restoptProblem.findSolution(0, true);
        sol.printSolutionInfos();
        int restoreArea = sol.getMinRestoreArea();
        int totalRestorable = sol.getTotalRestorableArea();
        Assert.assertTrue(restoreArea != totalRestorable);
        Assert.assertTrue(restoreArea <= 200 * 11);
        Assert.assertTrue(restoreArea >= 120 * 11);
        int sumRest = 0;
        int sumTotal = 0;
        int[] pus = sol.getRestorationPlanningUnits();
        for (int i : pus) {
            //int completeUngroupedIndex = restoptProblem.getGrid().getUngroupedCompleteIndex(i);
            int rest = restoptProblem.getRestorableArea(i);
            sumTotal += rest;
            int cArea = restoptProblem.getCellArea(i);
            int threshold = (int) Math.ceil(cArea * (1 - 0.7));
            sumRest += rest <= threshold ? 0 : rest - threshold;
        }
        Assert.assertEquals(sumTotal, totalRestorable);
        Assert.assertEquals(sumRest, restoreArea);
    }
}
